package io.reading_tracker.response;

import io.reading_tracker.domain.book.State;
import java.util.List;

public record GetBookListResponse(
    Summary summary,
    List<BookItem> books,
    Pagination pagination
) {

  public record Summary(int planned, int inProgress, int completed, int archived) {}

  public record BookItem(
      Long id,
      String title,
      String author,
      String publisher,
      Integer currentPage,
      Integer totalPages,
      int progress,
      State state
  ) {}

  public record Pagination(
      int currentPage,
      int totalPages,
      long totalElements,
      boolean hasPrevious,
      boolean hasNext
  ) {}
}

