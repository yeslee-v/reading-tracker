package io.reading_tracker.controller;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.reading_tracker.auth.PrincipalDetails;
import io.reading_tracker.auth.PrincipalDetailsService;
import io.reading_tracker.auth.oauth.CustomOAuth2UserService;
import io.reading_tracker.auth.oauth.OAuth2LoginSuccessHandler;
import io.reading_tracker.config.SecurityConfig;
import io.reading_tracker.domain.book.State;
import io.reading_tracker.domain.user.User;
import io.reading_tracker.response.GetBookListResponse;
import io.reading_tracker.response.GetBookListResponse.BookItem;
import io.reading_tracker.response.GetBookListResponse.Summary;
import io.reading_tracker.service.BookSearchService;
import io.reading_tracker.service.BookService;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.web.cors.CorsConfigurationSource;

@WebMvcTest(BookController.class)
@Import(SecurityConfig.class)
@ActiveProfiles("test")
class BookControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private BookService bookService;

  @MockitoBean private BookSearchService bookSearchService;

  @MockitoBean private PrincipalDetailsService principalDetailsService;

  @MockitoBean private CorsConfigurationSource corsConfigurationSource;

  @MockitoBean private CustomOAuth2UserService customOAuth2UserService;

  @MockitoBean private OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;

  @Autowired private ObjectMapper objectMapper;

  @Test
  @DisplayName("GET /api/books: 인자 없이 도서 목록 불러오기를 성공하면 IN_PROGRESS 상태로 200 OK를 반환한다")
  void getBookList_withoutParameter_return200OK() throws Exception {
    // given state 인자 없이
    User fakeUser = new User("tester", "test@email.com"); // id: null
    ReflectionTestUtils.setField(fakeUser, "id", 1L);

    UserDetails fakePrincipal = new PrincipalDetails(fakeUser);

    GetBookListResponse.Summary summary = new Summary(1, 1, 0);
    GetBookListResponse fakeResponse =
        new GetBookListResponse(
            summary,
            List.of(new BookItem(1L, "리팩토링 2판", "마틴 파울러", "한빛미디어", 1, 324, 0, State.IN_PROGRESS)));

    given(bookService.getBookList(eq(1L), eq(State.IN_PROGRESS))).willReturn(fakeResponse);

    // when getBookList를 호출하면
    ResultActions result =
        mockMvc.perform(
            get("/api/books").contentType(MediaType.APPLICATION_JSON).with(user(fakePrincipal)));

    // then IN_PROGRESS 상태의 200 OK를 반환한다
    result.andExpect(status().isOk());

    result.andExpect(jsonPath("$.books[0].title").value("리팩토링 2판"));
    result.andExpect(jsonPath("$.summary.inProgress").value(1));
  }

  @Test
  @DisplayName("GET /api/books: State가 COMPLETE인 도서 목록 불러오기를 성공하면 200 OK를 반환한다")
  void getBookList_withCOMPLETEStateParameter_return200OK() throws Exception {
    // given state로
    User fakeUser = new User("tester", "test@email.com");
    ReflectionTestUtils.setField(fakeUser, "id", 1L);

    UserDetails fakePrincipal = new PrincipalDetails(fakeUser);

    GetBookListResponse.Summary summary = new Summary(1, 1, 0);
    GetBookListResponse fakeResponse =
        new GetBookListResponse(
            summary,
            List.of(new BookItem(1L, "클린 코드", "로버트 C.마틴", "인사이트", 518, 518, 0, State.COMPLETED)));

    given(bookService.getBookList(eq(1L), eq(State.COMPLETED))).willReturn(fakeResponse);

    // when getBookList를 호출하면
    ResultActions result =
        mockMvc.perform(
            get("/api/books")
                .param("state", "COMPLETED")
                .contentType(MediaType.APPLICATION_JSON)
                .with(user(fakePrincipal)));

    // then 200 OK를 반환한다
    result.andExpect(status().isOk());

    result.andExpect(jsonPath("$.books[0].title").value("클린 코드"));
    result.andExpect(jsonPath("$.summary.completed").value(1));
  }

  @Test
  @WithAnonymousUser
  @DisplayName("GET /api/books: 로그인에 실패하면 401 Unauthorized를 반환한다")
  void getBookList_withInvalidUser_return401Unauthorized() throws Exception {
    // given 미로그인 유저가

    // when getBookList를 호출하면
    ResultActions result =
        mockMvc.perform(get("/api/books").contentType(MediaType.APPLICATION_JSON));

    // then '로그인이 필요합니다'라는 메시지와 401 Unauthorized를 반환한다
    result.andExpect(status().isUnauthorized());
    result.andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
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
  @WithAnonymousUser
  @DisplayName("GET /api/books/search: 로그인을 하지 않은 유저가 도서를 검색하면 401 Unauthorized를 반환한다")
  void searchBooks_withInvalidUser_return401Unauthorized() throws Exception {
    // given 로그인을 하지 않은 유저가

    // when searchBooks를 호출하면
    ResultActions result =
        mockMvc.perform(
            get("/api/books/search")
                .param("query", "클린 코드")
                .contentType(MediaType.APPLICATION_JSON));

    // then 401 Unauthorized를 반환한다
    result.andExpect(status().isUnauthorized());
    result.andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
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
  @WithAnonymousUser
  @DisplayName("POST /api/books: 로그인을 하지 않은 유저가 도서를 추가하면 401 Unauthorized를 반환한다")
  void addBook_withInvalidUser_return401Unauthorized() throws Exception {
    // given 로그인을 하지 않은 유저가

    // when addBook을 호출하면
    ResultActions result =
        mockMvc.perform(post("/api/books").contentType(MediaType.APPLICATION_JSON));

    // then 401 Unauthorized를 반환한다
    result.andExpect(status().isUnauthorized());
    result.andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
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
  @WithAnonymousUser
  @DisplayName("PATCH /api/books: 로그인을 하지 않은 유저가 도서를 수정하면 401 Unauthorized를 반환한다")
  void updateBook_withInvalidUser_return401Unauthorized() throws Exception {
    // given 로그인을 하지 않은 유저가

    // when updateBook을 호출하면
    ResultActions result =
        mockMvc.perform(patch("/api/books").contentType(MediaType.APPLICATION_JSON));

    // then 401 Unauthorized를 반환한다
    result.andExpect(status().isUnauthorized());
    result.andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
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
