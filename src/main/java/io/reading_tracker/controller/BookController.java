package io.reading_tracker.controller;

import io.reading_tracker.auth.PrincipalDetails;
import io.reading_tracker.domain.book.State;
import io.reading_tracker.request.SaveBookRequest;
import io.reading_tracker.response.ApiResponse;
import io.reading_tracker.response.GetBookListResponse;
import io.reading_tracker.response.SaveBookResponse;
import io.reading_tracker.response.SearchBookResponse;
import io.reading_tracker.service.BookSearchService;
import io.reading_tracker.service.BookService;
import java.util.Collections;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
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

    State stateFilter = state == null ? State.IN_PROGRESS : resolveState(state);

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
  public ApiResponse<SaveBookResponse> saveBook(
      @AuthenticationPrincipal PrincipalDetails principalDetails, SaveBookRequest request) {
    if (principalDetails == null) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다");
    }

    SaveBookResponse response = bookService.saveBook(request);

    return ApiResponse.success(response);
  }

  private State resolveState(String state) {
    String normalized = state.trim().toUpperCase(Locale.ROOT);

    return switch (normalized) {
      case "PLANNED" -> State.PLANNED;
      case "IN_PROGRESS" -> State.IN_PROGRESS;
      case "COMPLETED" -> State.COMPLETED;
      case "ARCHIVED" -> State.ARCHIVED;
      default ->
          throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "지원하지 않는 상태입니다: " + state);
    };
  }
}
