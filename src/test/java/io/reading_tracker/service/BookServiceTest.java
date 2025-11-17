package io.reading_tracker.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.reading_tracker.domain.book.Book;
import io.reading_tracker.domain.book.State;
import io.reading_tracker.domain.user.User;
import io.reading_tracker.domain.userbook.UserBook;
import io.reading_tracker.repository.BookRepository;
import io.reading_tracker.repository.UserBookRepository;
import io.reading_tracker.repository.UserRepository;
import io.reading_tracker.request.AddUserBookRequest;
import io.reading_tracker.request.UpdateUserBookRequest;
import io.reading_tracker.response.AddUserBookResponse;
import io.reading_tracker.response.GetBookListResponse;
import io.reading_tracker.response.UpdateUserBookResponse;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

@DataJpaTest
@Import(BookServiceImpl.class)
@ActiveProfiles("test")
@Transactional
class BookServiceTest {

  @Autowired private BookService bookService;

  @Autowired private BookRepository bookRepository;

  @Autowired private UserBookRepository userBookRepository;

  @Autowired private UserRepository userRepository;

  @Autowired private EntityManager entityManager;

  @Test
  @DisplayName("로그인 성공하면 도서 목록을 생성일 역순으로 반환한다")
  void getBookList_returnsInProgressBooks_sortedByCreated() {
    // given 유저가
    User user = userRepository.save(new User("tester", "tester@example.com"));

    Book first = bookRepository.save(new Book("책 A", "저자 A", "출판사 A", "isbn-1"));
    Book second = bookRepository.save(new Book("책 B", "저자 B", "출판사 B", "isbn-2"));
    Book third = bookRepository.save(new Book("책 C", "저자 C", "출판사 C", "isbn-3"));

    UserBook firstReading =
        userBookRepository.save(new UserBook(user, first, State.IN_PROGRESS, 300, 120));
    UserBook secondReading =
        userBookRepository.save(new UserBook(user, second, State.IN_PROGRESS, 320, 160));
    UserBook latestReading =
        userBookRepository.save(new UserBook(user, third, State.COMPLETED, 280, 280));

    LocalDateTime base = LocalDateTime.now();
    ReflectionTestUtils.setField(firstReading, "createdAt", base.minusMinutes(10));
    ReflectionTestUtils.setField(secondReading, "createdAt", base.minusMinutes(5));
    ReflectionTestUtils.setField(latestReading, "createdAt", base);

    entityManager.flush();
    entityManager.clear();

    // when 로그인을 성공하면
    GetBookListResponse response = bookService.getBookList(user.getId(), State.IN_PROGRESS);

    List<Long> returnedIds =
        response.books().stream().map(GetBookListResponse.BookItem::id).toList();

    // then 도서 목록을 생성일 역순으로 반환한다
    assertThat(response.books()).hasSize(2);

    assertThat(returnedIds)
        .containsExactly(secondReading.getId(), firstReading.getId())
        .doesNotContain(latestReading.getId());
  }

  @Test
  @DisplayName("읽고 있는 책이 없다면 빈 배열을 반환한다")
  void getBookList_returnsEmptyList() {
    // given 로그인을 했는데
    User user = userRepository.save(new User("tester", "tester@example.com"));

    Book first = bookRepository.save(new Book("책 A", "저자 A", "출판사 A", "isbn-1"));
    Book second = bookRepository.save(new Book("책 B", "저자 B", "출판사 B", "isbn-2"));
    Book third = bookRepository.save(new Book("책 C", "저자 C", "출판사 C", "isbn-3"));

    UserBook firstReading =
        userBookRepository.save(new UserBook(user, first, State.COMPLETED, 300, 120));
    UserBook secondReading =
        userBookRepository.save(new UserBook(user, second, State.COMPLETED, 320, 160));
    UserBook latestReading =
        userBookRepository.save(new UserBook(user, third, State.ARCHIVED, 280, 280));

    LocalDateTime base = LocalDateTime.now();
    ReflectionTestUtils.setField(firstReading, "createdAt", base.minusMinutes(10));
    ReflectionTestUtils.setField(secondReading, "createdAt", base.minusMinutes(5));
    ReflectionTestUtils.setField(latestReading, "createdAt", base);

    entityManager.flush();
    entityManager.clear();

    // when IN_PROGRESS 상태의 책이 없다면
    GetBookListResponse response1 = bookService.getBookList(user.getId(), State.IN_PROGRESS);

    // then 빈 배열을 반환한다
    assertThat(response1.books()).hasSize(0);
  }

  @Test
  @DisplayName("선택한 도서를 추가하면 IN_PROGRESS 상태인 새 도서 정보를 반환한다")
  void addBook_returnsNewBookInformation() {
    // given 선택한 도서를
    User user = userRepository.save(new User("tester", "tester@example.com"));

    AddUserBookRequest request =
        new AddUserBookRequest("1234567890123", "테스트 도서", "테스트 저자", "테스트 출판사", 300);

    // when 추가하면
    AddUserBookResponse response = bookService.addBookToUserLibrary(user, request);

    // then IN_PROGRESS 상태인 새 도서 정보를 반환한다
    assertThat(response).isNotNull();
    assertThat(response.state()).isEqualTo(State.IN_PROGRESS);
    assertThat(response.title()).isEqualTo("테스트 도서");

    UserBook savedBook = userBookRepository.findById(response.id()).orElseThrow();
    assertThat(savedBook.getUser().getId()).isEqualTo(user.getId());
    assertThat(savedBook.getBook().getTitle()).isEqualTo("테스트 도서");
  }

  @Test
  @DisplayName("이미 추가된 도서는 사용자의 도서 목록에 재추가할 수 없다")
  void addAlreadyExistBook_throwsError() {
    // given 이미 도서 목록(UserBookByUserId)에 있는 책을
    AddUserBookRequest request =
        new AddUserBookRequest("1234567890123", "테스트 도서", "테스트 저자", "테스트 출판사", 300);

    UserBook userBook = givenInitialUserBook(request);
    User user = userBook.getUser();

    // when 재추가하려고 하면
    // then 이미 존재하는 책은 재추가할 수 없다고 에러를 반환한다
    assertThatThrownBy(() -> bookService.addBookToUserLibrary(user, request))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("이미 사용자의 도서 목록에 추가되어 있습니다.");
  }

  @Test
  @DisplayName("읽은 페이지를 업데이트하면 독서 진행률이 수정된다")
  void updateBook_withCurrentPage_returnsRateCalculated() {
    // given 읽은 페이지를
    UserBook userBook = givenInitialUserBook();
    User user = userBook.getUser();

    Long userBookId = userBook.getId();
    State state = userBook.getState();
    Integer totalPages = userBook.getTotalPages();

    // when 업데이트하면
    Integer targetCurrentPage = 100;
    UpdateUserBookRequest updatedRequest =
        new UpdateUserBookRequest(userBookId, targetCurrentPage, state);

    UpdateUserBookResponse updatedResponse =
        bookService.updateUserBookProgress(user, updatedRequest);

    // then 자동으로 독서 진행률이 수정된다
    Assertions.assertThat(updatedResponse.currentPage()).isEqualTo(targetCurrentPage);

    Integer expectedRate =
        (int) Math.floor(updatedResponse.currentPage() / (double) totalPages * 100);
    Assertions.assertThat(updatedResponse.progress()).isEqualTo(expectedRate);
  }

  @Test
  @DisplayName("유저 도서 목록에 존재하지 않는 도서는 업데이트할 수 없다")
  void updateBook_withNotExistBook_ThrowsError() {
    // given 유저 도서 목록에 존재하지 않는 도서를
    UserBook userBook = givenInitialUserBook();
    User user = userBook.getUser();

    State state = userBook.getState();

    // when 업데이트하면
    Integer targetCurrentPage = 301;
    UpdateUserBookRequest updatedRequest = new UpdateUserBookRequest(2L, targetCurrentPage, state);

    // then 해당 도서는 사용자 목록 내 존재하지 않습니다는 에러를 반환한다
    assertThatThrownBy(() -> bookService.updateUserBookProgress(user, updatedRequest))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("해당 도서는 사용자 목록 내 존재하지 않습니다.");
  }

  @Test
  @DisplayName("읽은 페이지가 전체 페이지 값보다 크다면 에러를 반환한다")
  void updateBook_withCurrentPageGreaterThanTotalPages_throwsError() {
    // given 전체 페이지보다 값이 큰 읽은 페이지 값을
    UserBook userBook = givenInitialUserBook();
    User user = userBook.getUser();

    Long userBookId = userBook.getId();
    State state = userBook.getState();

    // when 업데이트하면
    Integer targetCurrentPage = 301;
    UpdateUserBookRequest updatedRequest =
        new UpdateUserBookRequest(userBookId, targetCurrentPage, state);

    // then 읽은 페이지는 전체 페이지 값보다 작거나 같아야한다는 에러를 반환한다
    assertThatThrownBy(() -> bookService.updateUserBookProgress(user, updatedRequest))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("현재 페이지는 전체 페이지를 초과할 수 없습니다.");
  }

  @Test
  @DisplayName("읽은 페이지가 음수이면 에러를 반환한다")
  void updateBook_withNegativeCurrentPage_throwsError() {
    // given 음수인 읽은 페이지 값을
    UserBook userBook = givenInitialUserBook();
    User user = userBook.getUser();

    Long userBookId = userBook.getId();
    State state = userBook.getState();

    // when 업데이트하면
    Integer targetCurrentPage = -1;
    UpdateUserBookRequest updatedRequest =
        new UpdateUserBookRequest(userBookId, targetCurrentPage, state);

    // then 읽은 페이지는 1 이상이어야 한다는 에러를 반환한다
    assertThatThrownBy(() -> bookService.updateUserBookProgress(user, updatedRequest))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("현재 페이지는 1 이상이어야 합니다.");
  }

  @Test
  @DisplayName("상태를 직접 수정하면 읽은 페이지, 진행률 값과 관련없이 독서 상태만 수정한다")
  void updateBook_withState_returnsStateUpdated() {
    // given 변경하려고 하는 상태 값이
    UserBook userBook = givenInitialUserBook();
    User user = userBook.getUser();

    Long userBookId = userBook.getId();

    // when 직접 주어지면
    UpdateUserBookRequest updatedRequest =
        new UpdateUserBookRequest(userBookId, null, State.ARCHIVED);

    UpdateUserBookResponse updatedResponse =
        bookService.updateUserBookProgress(user, updatedRequest);

    // then 상태가 바뀐다
    Assertions.assertThat(updatedResponse.state()).isEqualTo(State.ARCHIVED);
    Assertions.assertThat(updatedResponse.progress()).isEqualTo(0);
  }

  @Test
  @DisplayName("읽은 페이지 값이 전체 페이지 값과 같다면 상태는 COMPLETE로 바뀐다")
  void updateBook_withCurrentPageSameAsTotalPages_returnsStateCompleteUpdated() {
    // given 읽은 페이지 값이
    UserBook userBook = givenInitialUserBook();
    User user = userBook.getUser();

    Long userBookId = userBook.getId();

    // when 전체 페이지 값과 같다면
    Integer targetCurrentPage = 300;
    UpdateUserBookRequest updatedRequest =
        new UpdateUserBookRequest(userBookId, targetCurrentPage, null);

    UpdateUserBookResponse updatedResponse =
        bookService.updateUserBookProgress(user, updatedRequest);

    // then 상태는 COMPLETED로 자동으로 바뀐다
    Assertions.assertThat(updatedResponse.state()).isEqualTo(State.COMPLETED);
    Assertions.assertThat(updatedResponse.progress()).isEqualTo(100);
  }

  @Test
  @DisplayName("COMPLETE 상태에서 읽은 페이지 값을 수정하면 상태는 IN_PROGRESS로 바뀐다")
  void updateBook_withCurrentPageModifiedNotSameAsTotalPages_returnsStateInProgressUpdated() {
    // given COMPLETE 상태에서 읽은 페이지 값
    UserBook userBook = givenInitialUserBook();
    User user = userBook.getUser();

    Long userBookId = userBook.getId();
    Integer totalPages = userBook.getTotalPages();

    Integer targetCurrentPage1 = 300;
    UpdateUserBookRequest updatedRequest1 =
        new UpdateUserBookRequest(userBookId, targetCurrentPage1, null);

    UpdateUserBookResponse updatedResponse1 =
        bookService.updateUserBookProgress(user, updatedRequest1);

    Assertions.assertThat(updatedResponse1.state()).isEqualTo(State.COMPLETED);
    Assertions.assertThat(updatedResponse1.progress()).isEqualTo(100);

    // when 읽은 페이지 값을 전체 페이지 값과 다르게 수정하면
    Integer targetCurrentPage2 = 200;
    UpdateUserBookRequest updatedRequest2 =
        new UpdateUserBookRequest(userBookId, targetCurrentPage2, null);

    UpdateUserBookResponse updatedResponse2 =
        bookService.updateUserBookProgress(user, updatedRequest2);

    // then 상태가 IN_PROGRESS로 바뀐다
    Assertions.assertThat(updatedResponse2.state()).isEqualTo(State.IN_PROGRESS);

    Integer expectedRate =
        (int) Math.floor(updatedResponse2.currentPage() / (double) totalPages * 100);

    Assertions.assertThat(updatedResponse2.progress()).isEqualTo(expectedRate);
  }

  private UserBook givenInitialUserBook() {
    User user = userRepository.save(new User("tester", "tester@example.com"));

    AddUserBookRequest request =
        new AddUserBookRequest("1234567890123", "테스트 도서", "테스트 저자", "테스트 출판사", 300);

    AddUserBookResponse response = bookService.addBookToUserLibrary(user, request);

    return userBookRepository.findById(response.id()).orElseThrow();
  }

  private UserBook givenInitialUserBook(AddUserBookRequest request) {
    User user = userRepository.save(new User("tester", "tester@example.com"));

    AddUserBookResponse response = bookService.addBookToUserLibrary(user, request);

    return userBookRepository.findById(response.id()).orElseThrow();
  }
}
