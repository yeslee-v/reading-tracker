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
import io.reading_tracker.request.AddBookRequest;
import io.reading_tracker.response.AddBookResponse;
import io.reading_tracker.response.GetBookListResponse;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
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
  @DisplayName("로그인 성공하면 읽던 책 목록을 생성일 역순으로 반환한다")
  void getBookList_returnsInProgressBooks_sortedByCreated() {
    // given
    User user = userRepository.save(new User("tester", "tester@example.com", "local", "local-1"));

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

    // when
    int page = 0;
    GetBookListResponse response = bookService.getBookList(user.getId(), State.IN_PROGRESS, page);

    List<Long> returnedIds =
        response.books().stream().map(GetBookListResponse.BookItem::id).toList();

    // then
    assertThat(response.books()).hasSize(2);

    assertThat(returnedIds)
        .containsExactly(secondReading.getId(), firstReading.getId())
        .doesNotContain(latestReading.getId());
  }

  @Test
  @DisplayName("읽고 있는 책이 없다면 빈 배열을 반환한다")
  void getBookList_returnsEmptyList() {
    // given 로그인을 했는데
    User user = userRepository.save(new User("tester", "tester@example.com", "local", "local-1"));

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
    int page = 0;
    GetBookListResponse response1 = bookService.getBookList(user.getId(), State.IN_PROGRESS, page);

    // then 빈 배열을 반환한다
    assertThat(response1.books()).hasSize(0);
  }

  /** 도서 검색에 외부 API를 사용하므로 Controller에서 테스트 진행 */
  //  @Test
  //  @DisplayName("키워드로 검색하면 관련 도서 정보가 반환된다")
  //  void searchBook_returnsMatchingBookList_ByKeyword() {
  //    // given 키워드(도서명, ISBN 등)이 주어지면
  //    // when 검색했을 때
  //    // then 관련 도서 목록이 반환된다
  //  }

  @Test
  @DisplayName("선택한 도서를 추가하면 IN_PROGRESS 상태인 새 도서 정보를 반환한다")
  void addBook_returnsNewBookInformation() {
    // given 선택한 도서를
    User user = userRepository.save(new User("tester", "tester@example.com", "local", "local-1"));

    AddBookRequest request =
        new AddBookRequest("1234567890123", "테스트 도서", "테스트 저자", "테스트 출판사", 300);

    // when 추가하면
    AddBookResponse response = bookService.addBookToUserLibrary(user, request);

    // then IN_PROGRESS 상태인 새 도서 정보를 반환한다
    assertThat(response).isNotNull();
    assertThat(response.state()).isEqualTo(State.IN_PROGRESS);
    assertThat(response.title()).isEqualTo("테스트 도서");

    UserBook savedBook = userBookRepository.findById(response.id()).orElseThrow();
    assertThat(savedBook.getUser().getId()).isEqualTo(user.getId());
    assertThat(savedBook.getBook().getTitle()).isEqualTo("테스트 도서");
  }

  @Test
  @DisplayName("이미 추가된 도서는 내 도서 목록에 재추가할 수 없다")
  void addAlreadyExistBook_throwsError() {
    // given 이미 도서 목록(UserBookByUserId)에 있는 책을
    User user = userRepository.save(new User("tester", "tester@example.com", "local", "local-1"));

    AddBookRequest request1 =
        new AddBookRequest("1234567890123", "테스트 도서", "테스트 저자", "테스트 출판사", 300);

    AddBookResponse response1 = bookService.addBookToUserLibrary(user, request1);

    // when 재추가하려고 하면
    AddBookRequest request2 =
        new AddBookRequest("1234567890123", "테스트 도서", "테스트 저자", "테스트 출판사", 300);

    // then 이미 존재하는 책은 재추가할 수 없다고 에러를 반환한다
    assertThatThrownBy(() -> bookService.addBookToUserLibrary(user, request2))
        .isInstanceOf(IllegalStateException.class);
  }

  @Test
  @DisplayName("읽은 페이지를 업데이트하면 독서 진행률이 수정된다")
  void updateBook_withCurrentPage_returnsRateCalculated() {
    // given 읽은 페이지를
    // when 업데이트하면
    // then 자동으로 독서 진행률이 수정된다
  }

  @Test
  @DisplayName("읽은 페이지가 전체 페이지 값보다 크다면 에러를 반환한다")
  void updateBook_withCurrentPageGreaterThanTotalPages_throwsError() {
    // given 전체 페이지보다 값이 큰 읽은 페이지 값을
    // when 업데이트하면
    // then 읽은 페이지는 전체 페이지 값보다 작거나 같아야한다는 에러를 반환한다
  }

  @Test
  @DisplayName("읽은 페이지가 음수이면 에러를 반환한다")
  void updateBook_withNegativeCurrentPage_throwsError() {
    // given 음수인 읽은 페이지 값을
    // when 업데이트하면
    // then 읽은 페이지는 전체 페이지 값보다 작거나 같아야한다는 에러를 반환한다
  }

  @Test
  @DisplayName("읽은 페이지 값이 전체 페이지 값과 같다면 상태는 COMPLETE로 바뀐다")
  void updateBook_withCurrentPageSameAsTotalPages_returnsStateCompleteUpdated() {
    // given 읽은 페이지 값이
    // when 전체 페이지 값과 같다면
    // then 상태가 COMPLETED로 자동으로 바뀐다
  }

  @Test
  @DisplayName("COMPLETE 상태에서 읽은 페이지 값을 수정하면 상태는 IN_PROGRESS로 바뀐다")
  void updateBook_withCurrentPageModifiedNotSameAsTotalPages_returnsStateInProgressUpdated() {
    // given COMPLETE 상태에서 읽은 페이지 값
    // when 전체 페이지 값과 다르게 수정하면
    // then 상태가 IN_PROGRESS로 바뀐다
  }
}
