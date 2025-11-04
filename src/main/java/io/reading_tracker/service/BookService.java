package io.reading_tracker.service;

import io.reading_tracker.domain.book.State;
import io.reading_tracker.domain.user.User;
import io.reading_tracker.request.AddBookRequest;
import io.reading_tracker.request.UpdateBookRequest;
import io.reading_tracker.response.AddBookResponse;
import io.reading_tracker.response.GetBookListResponse;
import io.reading_tracker.response.UpdateBookResponse;

public interface BookService {

  GetBookListResponse getBookList(Long userId, State stateFilter, int page);

  AddBookResponse addBookToUserLibrary(User user, AddBookRequest request);

  UpdateBookResponse updateBook(UpdateBookRequest request);
}
