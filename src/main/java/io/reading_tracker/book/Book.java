package io.reading_tracker.book;

import io.reading_tracker.user.User;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Book {
  private Long id;
  private String title;
  private String author;
  private State state;
  private int fullPage;
  private int currentPage;
  private Date createAt;
  private Date updateAt;

  private User user;

  public Book(User user, String title, String author, State state, int fullPage, int currentPage) {
    this.id = null; // ID can be set later if managed externally
    this.title = title;
    this.author = author;
    this.state = state;
    this.fullPage = fullPage;
    this.currentPage = currentPage;
    this.createAt = new Date();
    this.updateAt = new Date();
    this.user = user;
  }
}
