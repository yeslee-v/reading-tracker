package io.reading_tracker.controller;

import io.reading_tracker.auth.PrincipalDetails;
import io.reading_tracker.domain.book.State;
import io.reading_tracker.response.ApiResponse;
import io.reading_tracker.response.GetBookListResponse;
import io.reading_tracker.service.BookService;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
public class BookController {

  private static final int DEFAULT_PAGE = 1;
  private static final int DEFAULT_SIZE = 10;

  private final BookService bookService;

  @GetMapping
  public ApiResponse<GetBookListResponse> listBooks(
      @AuthenticationPrincipal PrincipalDetails principalDetails,
      @RequestParam(name = "state", required = false) String state,
      @RequestParam(name = "page", defaultValue = "" + DEFAULT_PAGE) int page,
      @RequestParam(name = "size", defaultValue = "" + DEFAULT_SIZE) int size
  ) {
    if (principalDetails == null) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
    }

    if (page < 1) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "page는 1 이상이어야 합니다.");
    }

    if (size < 1) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "size는 1 이상이어야 합니다.");
    }

    State stateFilter = resolveState(state);

    GetBookListResponse response = bookService.getBookList(
        principalDetails.getUserId(),
        stateFilter,
        page - 1
    );

    return ApiResponse.success(response);
  }

  private State resolveState(String state) {
    String normalized = state.trim().toUpperCase(Locale.ROOT);

    return switch (normalized) {
      case "PLANNED" -> State.PLANNED;
      case "IN_PROGRESS" -> State.IN_PROGRESS;
      case "COMPLETED" -> State.COMPLETED;
      case "ARCHIVED" -> State.ARCHIVED;
      default -> throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST,
          "지원하지 않는 상태입니다: " + state
      );
    };
  }
}

