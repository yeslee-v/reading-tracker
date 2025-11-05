package io.reading_tracker.service;

import io.reading_tracker.domain.book.State;
import io.reading_tracker.domain.user.User;
import io.reading_tracker.request.AddBookRequest;
import io.reading_tracker.request.UpdateUserBookRequest;
import io.reading_tracker.response.AddBookResponse;
import io.reading_tracker.response.GetBookListResponse;
import io.reading_tracker.response.UpdateUserBookResponse;

public interface BookService {

  GetBookListResponse getBookList(Long userId, State stateFilter, int page);

  AddBookResponse addBookToUserLibrary(User user, AddBookRequest request);

  UpdateUserBookResponse updateUserBookProgress(User user, UpdateUserBookRequest request);
}
