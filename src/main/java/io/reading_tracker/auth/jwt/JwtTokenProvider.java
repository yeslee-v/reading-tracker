package io.reading_tracker.auth.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

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
    } catch (Exception e) {
      return false;
    }
  }

  private Claims getClaims(String token) {
    return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
  }
}
