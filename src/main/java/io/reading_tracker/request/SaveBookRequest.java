package io.reading_tracker.request;

public record SaveBookRequest(
    Long id, String isbn, String title, String author, String publisher, Integer totalPages) {}
