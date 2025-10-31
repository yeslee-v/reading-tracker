package io.reading_tracker.service;

import io.reading_tracker.domain.book.State;
import io.reading_tracker.request.SaveBookRequest;
import io.reading_tracker.request.UpdateBookRequest;
import io.reading_tracker.response.GetBookListResponse;
import io.reading_tracker.response.SaveBookResponse;
import io.reading_tracker.response.UpdateBookResponse;

public interface BookService {

  GetBookListResponse getBookList(Long userId, State stateFilter, int page);

  SaveBookResponse saveBook(SaveBookRequest request);

  UpdateBookResponse updateBook(UpdateBookRequest request);
}
