package io.reading_tracker.request;

public record UpdateUserBookRequest(Long id, Integer currentPage, String state) {}
