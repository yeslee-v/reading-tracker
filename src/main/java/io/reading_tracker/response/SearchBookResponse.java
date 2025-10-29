package io.reading_tracker.response;

import java.util.List;

public record SearchBookResponse(
    int total,
    int display,
    List<BookItem> items
) {

  public record BookItem(
      String isbn,
      String title,
      String author,
      String publisher,
      String link
  ) {
  }
}
