package io.reading_tracker.service;

import static org.assertj.core.api.Assertions.assertThat;

import io.reading_tracker.domain.book.Book;
import io.reading_tracker.domain.book.State;
import io.reading_tracker.domain.user.User;
import io.reading_tracker.domain.userbook.UserBook;
import io.reading_tracker.repository.BookRepository;
import io.reading_tracker.repository.UserBookRepository;
import io.reading_tracker.repository.UserRepository;
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
}
