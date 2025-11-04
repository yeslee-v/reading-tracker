package io.reading_tracker.response;

import io.reading_tracker.domain.book.State;

public record AddBookResponse(
    Long id,
    String title,
    String author,
    String publisher,
    State state,
    Integer currentPage,
    Integer totalPages) {}
