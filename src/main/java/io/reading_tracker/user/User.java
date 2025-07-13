package io.reading_tracker.user;

import java.util.Date;
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

  public User(String nickname, String email) {
    this.nickname = nickname;
    this.email = email;
    this.createAt = new Date();
    this.updateAt = new Date();
  }
}
