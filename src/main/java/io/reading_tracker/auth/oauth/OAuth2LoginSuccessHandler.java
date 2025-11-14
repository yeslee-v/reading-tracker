package io.reading_tracker.auth.oauth;

import io.reading_tracker.auth.PrincipalDetails;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

  private final AuthRepository authRepository;
  private final RedisTemplate<String, String> redisTemplate;
  private final OAuth2AuthorizedClientService oAuth2AuthorizedClientService;
  private final PasswordEncoder passwordEncoder;

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
    Instant acessExpiresAt = client.getAccessToken().getExpiresAt();

    String refreshToken = client.getRefreshToken().getTokenValue();
    Instant refreshExpiresAt = client.getRefreshToken().getExpiresAt();

    log.debug("OAuth sucess: AT={} RT={}", accessToken, refreshToken);

    String redisKey = "at:" + user.getId();
    Duration atTtl = Duration.between(Instant.now(), acessExpiresAt);

    redisTemplate.opsForValue().set(redisKey, accessToken, atTtl);

    log.info("Redis에 Access Token 저장: key={}, ttl={}", redisKey, atTtl.toSeconds());

    Auth auth =
        authRepository
            .findByUserId(user.getId())
            .orElseThrow(() -> new IllegalStateException("Auth 정보가 없습니다"));

    String encryptedRefreshToken = passwordEncoder.encode(refreshToken);
    auth.updateRefreshToken(encryptedRefreshToken, refreshExpiresAt);
    authRepository.save(auth);

    log.info("Refresh Token DB 암호화 저장 완료: userId={}", user.getId());

    getRedirectStrategy().sendRedirect(request, response, "/");
  }
}
