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
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "users")
public class User extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 50)
  private String nickname;

  @Column(nullable = false, length = 255)
  private String email;

  @Column(nullable = false, length = 20)
  private String provider;

  @Column(name = "provider_id", nullable = false, length = 100)
  private String providerId;

  @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
  private Set<UserBook> userBooks = new LinkedHashSet<>();

  public User(String nickname, String email) {
    this.nickname = nickname;
    this.email = email;
  }

  public User(String nickname, String email, String provider, String providerId) {
    this.nickname = nickname;
    this.email = email;
    this.provider = provider;
    this.providerId = providerId;
  }

  public void updateProfile(String nickname, String email) {
    this.nickname = nickname;
    this.email = email;
  }

  public void changeNickname(String nickname) {
    if (nickname == null || nickname.isBlank()) {
      throw new IllegalArgumentException("닉네임은 비어 있을 수 없습니다.");
    }

    this.nickname = nickname;
  }

  public void addUserBook(UserBook userBook) {
    Objects.requireNonNull(userBook, "userBook은 null일 수 없습니다.");
    userBooks.add(userBook);
    userBook.setUser(this);
  }

  public void removeUserBook(UserBook userBook) {
    if (userBook == null) {
      return;
    }

    if (userBooks.remove(userBook)) {
      userBook.setUser(null);
    }
  }
}
