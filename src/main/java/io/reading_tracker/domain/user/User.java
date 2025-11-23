package io.reading_tracker.domain.user;

import io.reading_tracker.domain.BaseEntity;
import io.reading_tracker.domain.userbook.UserBook;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.LinkedHashSet;
import java.util.Set;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(
    name = "users",
    uniqueConstraints = {@UniqueConstraint(name = "uk_user_email", columnNames = "email")})
public class User extends BaseEntity {

  @OneToMany(
      mappedBy = "user",
      cascade = CascadeType.ALL,
      orphanRemoval = true,
      fetch = FetchType.LAZY)
  private final Set<UserBook> userBooks = new LinkedHashSet<>();

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(nullable = false)
  private Long id;

  @Column(nullable = false, length = 50)
  private String nickname;

  @Column(nullable = false, length = 255)
  private String email;

  public User(String nickname, String email) {
    this.nickname = nickname;
    this.email = email;
  }

  public void updateProfile(String nickname, String email) {
    this.nickname = nickname;
    this.email = email;
  }

  public void updateNickname(String nickname) {
    if (nickname == null || nickname.isBlank()) {
      throw new IllegalArgumentException("닉네임은 비어 있을 수 없습니다.");
    }

    if (nickname.length() > 20) {
      throw new IllegalArgumentException("닉네임의 길이는 20자 이하여야 합니다");
    }

    this.nickname = nickname;
  }
}
