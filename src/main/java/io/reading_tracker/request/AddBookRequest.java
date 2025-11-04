package io.reading_tracker.request;

public record AddBookRequest(
    String isbn, String title, String author, String publisher, Integer totalPages) {}
