package io.reading_tracker.controller;

import io.reading_tracker.auth.PrincipalDetails;
import io.reading_tracker.domain.book.State;
import io.reading_tracker.request.AddBookRequest;
import io.reading_tracker.request.UpdateUserBookRequest;
import io.reading_tracker.response.AddBookResponse;
import io.reading_tracker.response.ApiResponse;
import io.reading_tracker.response.GetBookListResponse;
import io.reading_tracker.response.SearchBookResponse;
import io.reading_tracker.response.UpdateUserBookResponse;
import io.reading_tracker.service.BookSearchService;
import io.reading_tracker.service.BookService;
import java.util.Collections;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
public class BookController {

  private static final int DEFAULT_PAGE = 1;

  private final BookService bookService;
  private final BookSearchService bookSearchService;

  @GetMapping
  public ApiResponse<GetBookListResponse> listBooks(
      @AuthenticationPrincipal PrincipalDetails principalDetails,
      @RequestParam(name = "state", required = false) String state,
      @RequestParam(name = "page", defaultValue = "" + DEFAULT_PAGE) int page) {
    if (principalDetails == null) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
    }

    if (page < 1) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "page는 1 이상이어야 합니다.");
    }

    State stateFilter = state == null ? State.IN_PROGRESS : State.from(state);

    GetBookListResponse response =
        bookService.getBookList(principalDetails.getUserId(), stateFilter, page - 1);

    return ApiResponse.success(response);
  }

  @GetMapping("/search")
  public ApiResponse<SearchBookResponse> searchBooks(
      @AuthenticationPrincipal PrincipalDetails principalDetails,
      @RequestParam(name = "query") String query) {
    if (principalDetails == null) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
    }

    String trimmedQuery = query == null ? "" : query.trim();

    if (trimmedQuery.isEmpty()) {
      SearchBookResponse easterEgg = new SearchBookResponse(0, 0, Collections.emptyList());
      return ApiResponse.success(easterEgg);
    }

    SearchBookResponse response = bookSearchService.search(trimmedQuery);

    return ApiResponse.success(response);
  }

  @PostMapping
  public ApiResponse<AddBookResponse> addBook(
      @AuthenticationPrincipal PrincipalDetails principalDetails,
      @RequestBody AddBookRequest request) {
    if (principalDetails == null) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다");
    }

    if (request == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "추가할 책 정보가 없습니다.");
    }

    AddBookResponse response =
        bookService.addBookToUserLibrary(principalDetails.getUser(), request);

    return ApiResponse.success(response);
  }

  @PatchMapping
  public ApiResponse<UpdateUserBookResponse> updateBook(
      @AuthenticationPrincipal PrincipalDetails principalDetails,
      @RequestBody UpdateUserBookRequest request) {
    if (principalDetails == null) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다");
    }

    if (request == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "수정할 책 정보가 없습니다.");
    }

    UpdateUserBookResponse response =
        bookService.updateUserBookProgress(principalDetails.getUser(), request);
    return ApiResponse.success(response);
  }
}
