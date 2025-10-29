package io.reading_tracker.service;

import io.reading_tracker.response.SearchBookResponse;

public interface BookSearchService {

  SearchBookResponse search(String query);
}
