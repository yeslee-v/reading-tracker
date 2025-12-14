package io.reading_tracker.auth.oauth;

import io.reading_tracker.auth.PrincipalDetails;
import io.reading_tracker.auth.jwt.JwtTokenProvider;
import io.reading_tracker.domain.user.Auth;
import io.reading_tracker.domain.user.User;
import io.reading_tracker.repository.AuthRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

  private final AuthRepository authRepository;
  private final RedisTemplate<String, String> redisTemplate;
  private final OAuth2AuthorizedClientService oAuth2AuthorizedClientService;
  private final JwtTokenProvider jwtTokenProvider;

  @Override
  public void onAuthenticationSuccess(
      HttpServletRequest request, HttpServletResponse response, Authentication authentication)
      throws IOException {

    PrincipalDetails principal = (PrincipalDetails) authentication.getPrincipal();
    User user = principal.getUser();

    OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
    OAuth2AuthorizedClient client =
        oAuth2AuthorizedClientService.loadAuthorizedClient(
            oauthToken.getAuthorizedClientRegistrationId(), oauthToken.getName());

    String accessToken = client.getAccessToken().getTokenValue();
    Instant accessExpiresAt = client.getAccessToken().getExpiresAt();

    OAuth2RefreshToken oauthRefreshToken = client.getRefreshToken();
    String refreshToken = null;
    Instant refreshExpiresAt = null;

    if (oauthRefreshToken == null) {
      log.warn(
          "OAuth가 Refresh Token 주지 않음: userId={}, provider={}",
          user.getId(),
          oauthToken.getAuthorizedClientRegistrationId());
    } else {
      refreshToken = oauthRefreshToken.getTokenValue();
      refreshExpiresAt = oauthRefreshToken.getExpiresAt();
    }

    log.debug("OAuth 로그인 성공: AT={} RT={}", accessToken, refreshToken);

    String redisKey = "at:" + user.getId();
    Duration atTtl = Duration.between(Instant.now(), accessExpiresAt);

    try {
      redisTemplate.opsForValue().set(redisKey, accessToken, atTtl);

      log.info("Redis에 Access Token 저장: key={}, ttl={}", redisKey, atTtl.toSeconds());
    } catch (Exception e) {
      // access token을 db에 임시 백업하면 코드 관리가 어려워지므로  로그만 남기기
      log.error(
          "Redis 장애 발생 - OAuth Access Token 저장 실패: key={}, error={}", redisKey, e.getMessage());
    }

    // 로그인 진행
    Auth auth =
        authRepository
            .findByUserId(user.getId())
            .orElseThrow(() -> new IllegalStateException("Auth 정보가 없습니다"));

    if (refreshToken == null) {
      if (auth.getRefreshToken() == null) {
        log.error("저장된 Refresh Token 없음: userId={}", user.getId());
      }
    } else {
      auth.updateRefreshToken(refreshToken, refreshExpiresAt);
      authRepository.save(auth);
      log.info("Refresh Token DB 암호화 저장 완료: userId={}", user.getId());
    }

    String rtToken = jwtTokenProvider.createToken(user.getId());

    ResponseCookie cookie =
        ResponseCookie.from("rt_token", rtToken)
            .path("/")
            .sameSite("None")
            .httpOnly(true)
            .secure(true)
            .maxAge(Duration.ofDays(7))
            .build();

    response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

    getRedirectStrategy().sendRedirect(request, response, "http://localhost:3000");
  }
}
