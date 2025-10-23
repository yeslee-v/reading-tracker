package io.reading_tracker.service;

import io.reading_tracker.domain.book.State;
import io.reading_tracker.response.GetBookListResponse;

public interface BookService {

  GetBookListResponse getBookList(Long userId, State stateFilter, int page);
}
