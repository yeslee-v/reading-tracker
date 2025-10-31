package io.reading_tracker.request;

public record UpdateBookRequest(Long id, Integer currentPage, String state) {}
