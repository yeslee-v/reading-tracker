package io.reading_tracker.domain.user;

import io.reading_tracker.domain.book.Book;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class User {
  private Long id;
  private String nickname;
  private String email;
  private Date createAt;
  private Date updateAt;

  private Set<Book> books = new HashSet<>();

  public User(String nickname, String email) {
    this.nickname = nickname;
    this.email = email;
    this.createAt = new Date();
    this.updateAt = new Date();
  }
}
