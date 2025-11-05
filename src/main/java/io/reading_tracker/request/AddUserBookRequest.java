package io.reading_tracker.request;

public record AddUserBookRequest(
    String isbn, String title, String author, String publisher, Integer totalPages) {}
