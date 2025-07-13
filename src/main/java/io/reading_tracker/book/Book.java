package io.reading_tracker.book;

import io.reading_tracker.user.State;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Book {
  Long id;
  String name;
  String author;
  State state;
  int fullPageNumber;
  int currentPageNumber;
  Date createAt;
  Date updateAt;

  public Book(String name, String author, State state, int fullPageNumber, int currentPageNumber) {
    this.name = name;
    this.author = author;
    this.state = state;
    this.fullPageNumber = fullPageNumber;
    this.currentPageNumber = currentPageNumber;
  }
}
