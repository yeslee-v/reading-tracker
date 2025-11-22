package io.reading_tracker.service;

import static org.assertj.core.api.Assertions.*;

import io.reading_tracker.domain.book.State;
import io.reading_tracker.domain.user.User;
import io.reading_tracker.repository.BookRepository;
import io.reading_tracker.repository.UserBookRepository;
import io.reading_tracker.repository.UserRepository;
import io.reading_tracker.request.AddUserBookRequest;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@Import(BookServiceImpl.class)
@ActiveProfiles("test")
public class BookServiceConcurrencyTest {

  @Autowired private BookService bookService;
  @Autowired private UserRepository userRepository;
  @Autowired private BookRepository bookRepository;
  @Autowired private UserBookRepository userBookRepository;

  private User user;

  @BeforeEach
  void setUp() {
    userBookRepository.deleteAll();
    bookRepository.deleteAll();
    userRepository.deleteAll();

    user = userRepository.save(new User("tester", "tester@example.com"));
  }

  @AfterEach
  void tearDown() {
    userBookRepository.deleteAll();
    bookRepository.deleteAll();
    userRepository.deleteAll();
  }

  @Test
  @DisplayName("30개의 요청이 동시에 들어오더라도 1개만 성공한다")
  void concurrencyTest() throws InterruptedException {
    // given 요청 30개가
    AddUserBookRequest request =
        new AddUserBookRequest("1234567890123", "테스트 도서", "테스트 저자", "테스트 출판사", 300);

    int threadCount = 30;
    ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
    CountDownLatch readyLatch = new CountDownLatch(threadCount);
    CountDownLatch finishLatch = new CountDownLatch(threadCount);

    AtomicInteger successCount = new AtomicInteger();
    AtomicInteger failCount = new AtomicInteger();

    // when 동시에 들어와도
    for (int i = 0; i < threadCount; i++) {
      int requestNumber = i + 1;

      executorService.submit(
          () -> {
            try {
              readyLatch.countDown();
              readyLatch.await();

              bookService.addBookToUserLibrary(user, request);
              successCount.incrementAndGet();
            } catch (InterruptedException e) {
              System.out.println("InterruptedException: " + e.getMessage());
            } catch (Exception e) {
              System.out.println("이미 처리 중인 " + requestNumber + "번째 요청입니다: " + e.getMessage());
              failCount.incrementAndGet();
            } finally {
              finishLatch.countDown();
            }
          });
    }

    // then 1개의 요청만 수행한다
    finishLatch.await();
    executorService.shutdown();

    System.out.println("successCount = " + successCount.get());
    System.out.println("failCount = " + failCount.get());

    assertThat(successCount.get()).isEqualTo(1);
    assertThat(failCount.get()).isEqualTo(threadCount - 1);

    long actualCount = userBookRepository.countByUserIdAndState(user.getId(), State.IN_PROGRESS);
    assertThat(actualCount).isEqualTo(1L);
  }
}
