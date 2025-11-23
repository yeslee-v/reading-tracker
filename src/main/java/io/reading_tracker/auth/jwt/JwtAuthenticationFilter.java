package io.reading_tracker.auth.jwt;

import io.reading_tracker.auth.PrincipalDetails;
import io.reading_tracker.domain.user.User;
import io.reading_tracker.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private final JwtTokenProvider jwtTokenProvider;
  private final UserRepository userRepository;

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    String requestURI = request.getRequestURI();
    log.debug("JwtAuthenticationFilter 실행: 요청 URL = {}", requestURI);

    String token = resolveTokenFromCookie(request);

    if (token != null && jwtTokenProvider.validateToken(token)) {
      log.debug("--> 토큰 발견! 검증 시작. Token: {}...", token.substring(0, Math.min(token.length(), 10)));

      Long userId = jwtTokenProvider.getUserId(token);
      log.debug("--> 토큰 유효함! User ID: {}", userId);

      User user = userRepository.findById(userId).orElse(null);

      if (user != null) {

        PrincipalDetails principalDetails = new PrincipalDetails(user, null);
        Authentication authentication =
            new UsernamePasswordAuthenticationToken(
                principalDetails, null, principalDetails.getAuthorities());

        SecurityContextHolder.getContext().setAuthentication(authentication);
        log.debug("Security Context에 '{}' 인증 정보 저장", user.getEmail());
      } else {
        log.warn("--> 토큰은 유효하지만 DB에 유저가 없습니다. ID: {}", userId);
      }
    } else {
      if (token == null) {
        log.debug("--> 쿠키에 'rt_token'이 없습니다.");
      } else {
        log.warn("--> token이 유효하지 않습니다.");
      }
    }

    filterChain.doFilter(request, response);
  }

  private String resolveTokenFromCookie(HttpServletRequest request) {
    Cookie[] cookies = request.getCookies();

    if (cookies == null) return null;

    return Arrays.stream(cookies)
        .filter(cookie -> "rt_token".equals(cookie.getName()))
        .map(Cookie::getValue)
        .findFirst()
        .orElse(null);
  }
}
