package io.reading_tracker.domain.user;

import io.reading_tracker.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
    name = "auth",
    uniqueConstraints = {
      @UniqueConstraint(name = "uk_auth_user_id", columnNames = "user_id"),
      @UniqueConstraint(
          name = "uk_auth_provider_provider_id",
          columnNames = {"provider", "provider_id"})
    })
public class Auth extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(nullable = false)
  private Long id;

  /** 2차 개발: OneToMany로 변경, uk_auth_user_id 제거 */
  @OneToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Column(nullable = false, length = 20)
  private String provider;

  @Column(name = "provider_id", nullable = false, length = 100)
  private String providerId;

  @Column(name = "refresh_token", nullable = false, columnDefinition = "TEXT")
  private String refreshToken;

  /** refresh token 만료 시간, Access token 만료 시간은 Redis에서 관리 */
  @Column(name = "expired_at", nullable = false)
  private Instant expiredAt;

  public Auth(
      User user, String provider, String providerId, String refreshToken, Instant expiredAt) {
    this.user = user;
    this.provider = provider;
    this.providerId = providerId;
    this.refreshToken = refreshToken;
    this.expiredAt = expiredAt;
  }
}
