package io.reading_tracker.response;

public record UpdateBookResponse(Long id, Integer currentPage, String state) {}
