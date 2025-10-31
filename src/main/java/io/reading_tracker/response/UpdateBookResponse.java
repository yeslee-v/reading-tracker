package io.reading_tracker.response;

import io.reading_tracker.domain.book.State;

public record UpdateBookResponse(Long id, Integer progress, Integer currentPage, State state) {}
