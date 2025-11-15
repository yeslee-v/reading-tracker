package io.reading_tracker.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.reading_tracker.service.BookSearchService;
import io.reading_tracker.service.BookService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(BookController.class)
@ActiveProfiles("test")
class BookControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private BookService bookService;

  @MockitoBean private BookSearchService bookSearchService;

  @Autowired private ObjectMapper objectMapper;

  @Test
  @WithMockUser
  @DisplayName("GET /api/books: 도서 목록 불러오기를 성공하면 200 OK를 반환한다")
  void getBookList_return200OK() {
    // given state(null이면 IN_PROGRESS)로

    // when getBookList를 호출하면

    // then 200 OK를 반환한다
  }

  @Test
  @DisplayName("GET /api/books: 로그인에 실패하면 401 Unauthorized를 반환한다")
  void getBookList_withInvalidUser_return401Unauthorized() {
    // given 미로그인 유저가

    // when getBookList를 호출하면

    // then '로그인이 필요합니다'라는 메시지와 401 Unauthorized를 반환한다
  }

  @Test
  @WithMockUser
  @DisplayName("GET /api/books: 존재하지 않는 state로 도서 목록을 불러오면 400 Bad Request를 반환한다")
  void getBookList_withInvalidState_return400BadRequest() {
    // given 존재하지 않는 state로

    // when getBookList를 호출하면

    // then 400 Bad Request를 반환한다
  }

  @Test
  @WithMockUser
  @DisplayName("GET /api/books: 도서 목록을 불러오는 중 서버가 터지면 500 Internal Server Error를 반환한다")
  void getBookList_return500InternalServerError() {
    // given getBookList를 호출하는데

    // when 서버가 터지면

    // then 500 Internal Server Error를 반환한다
  }

  @Test
  @WithMockUser
  @DisplayName("GET /api/books/search: 키워드로 검색하면 200 OK를 반환한다")
  void searchBooks_withKeyword_return200OK() {
    // given 키워드(도서명, ISBN 등)로

    // when searchBooks를 호출하면

    // then 200 OK를 반환한다
  }

  @Test
  @DisplayName("GET /api/books/search: 로그인을 하지 않은 유저가 도서를 검색하면 401 Unauthorized를 반환한다")
  void searchBooks_withInvalidUser_return401Unauthorized() {
    // given 로그인을 하지 않은 유저가

    // when searchBooks를 호출하면

    // then 401 Unauthorized를 반환한다
  }

  @Test
  @WithMockUser
  @DisplayName("POST /api/books: 도서를 추가하면 201 CREATED를 반환한다")
  void addBook_withBookRequest_returns201Created() {
    // given 추가할 도서 정보로
    // when addBook을 호출하면
    // then 201 CREATED를 반환한다
  }

  @Test
  @DisplayName("POST /api/books: 로그인을 하지 않은 유저가 도서를 추가하면 401 Unauthorized를 반환한다")
  void addBook_withInvalidUser_return401Unauthorized() {
    // given 로그인을 하지 않은 유저가

    // when addBook을 호출하면

    // then 401 Unauthorized를 반환한다
  }

  @Test
  @WithMockUser
  @DisplayName("POST /api/books: 도서 정보가 없는 도서를 추가하면 400 Bad Request를 반환한다")
  void addBook_withInvalidRequest_return400BadRequest() {
    // given 도서 정보가 존재하지 않는 도서로

    // when addBook을 호출하면

    // then 400 Bad Request를 반환한다
  }

  @Test
  @WithMockUser
  @DisplayName("PATCH /api/books: 도서의 현재 페이지 또는 상태를 수정하면 200 OK를 반환한다")
  void updateBook_withCurrentPageOrState_return200OK() {
    // given 도서의 현재 페이지 또는 상태로
    // when updateBook을 호출하면
    // then 200 OK를 반환한다
  }

  @Test
  @DisplayName("PATCH /api/books: 로그인을 하지 않은 유저가 도서를 수정하면 401 Unauthorized를 반환한다")
  void updateBook_withInvalidUser_return401Unauthorized() {
    // given 로그인을 하지 않은 유저가

    // when updateBook을 호출하면

    // then 401 Unauthorized를 반환한다
  }

  @Test
  @WithMockUser
  @DisplayName("PATCH /api/books: 전체 페이지 값보다 큰 변경할 페이지 값으로 도서를 수정하면 400 Bad Request를 반환한다")
  void updateBook_withCurrentPageBiggerThanTotalPages_return400BadRequest() {
    // given 전체 페이지 값보다 큰 변경할 페이지로

    // when updateBook을 호출하면

    // then 400 Bad Request를 반환한다
  }

  @Test
  @WithMockUser
  @DisplayName("PATCH /api/books: 존재하지 않는 state 값으로 도서를 수정하면 400 Bad Request를 반환한다")
  void updateBook_withInvalidState_return400BadRequest() {
    // given 존재하지 않는 state로

    // when updateBook을 호출하면

    // then 400 Bad Request를 반환한다
  }
}
