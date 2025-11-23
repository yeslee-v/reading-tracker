package io.reading_tracker.auth.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.util.Date;
import javax.crypto.SecretKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class JwtTokenProvider {

  private final SecretKey key;
  private final Long validityInSeconds;

  public JwtTokenProvider(
      @Value("${jwt.secret-key}") String secretKey,
      @Value("${jwt.expiration-time}") long validityInSeconds) {
    byte[] keyBytes = Decoders.BASE64.decode(secretKey);
    this.key = Keys.hmacShaKeyFor(keyBytes);
    this.validityInSeconds = validityInSeconds;
  }

  public String createToken(Long userId) {
    Date now = new Date();
    Date validity = new Date(now.getTime() + validityInSeconds);

    return Jwts.builder()
        .subject(String.valueOf(userId))
        .issuedAt(now)
        .expiration(validity)
        .signWith(key)
        .compact();
  }

  public Long getUserId(String token) {
    return Long.parseLong(getClaims(token).getSubject());
  }

  public boolean validateToken(String token) {
    try {
      getClaims(token);
      return true;
    } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
      log.info("잘못된 JWT 서명입니다");
    } catch (ExpiredJwtException e) {
      log.info("만료된 JWT 토큰입니다");
    } catch (UnsupportedJwtException e) {
      log.info("지원되지 않는 JWT 토큰입니다");
    } catch (IllegalArgumentException e) {
      log.info("JWT 토큰이 잘못되었습니다");
    }
    return false;
  }

  private Claims getClaims(String token) {
    return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
  }
}
