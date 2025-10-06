package io.reading_tracker;

import io.reading_tracker.domain.book.BookRepository;
import io.reading_tracker.domain.book.BookService;
import io.reading_tracker.domain.book.BookServiceImpl;
import io.reading_tracker.domain.book.MemoryBookRepository;
import io.reading_tracker.domain.user.MemoryUserRepository;
import io.reading_tracker.domain.user.UserRepository;
import io.reading_tracker.domain.user.UserService;
import io.reading_tracker.domain.user.UserServiceImpl;
import org.springframework.context.annotation.Bean;

public class AppConfig {
  @Bean
  public BookService bookService() {
    return new BookServiceImpl(bookRepository(), userService());
  }

  @Bean
  public BookRepository bookRepository() {
    return new MemoryBookRepository();
  }

  @Bean
  public UserService userService() {
    return new UserServiceImpl(userRepository());
  }

  @Bean
  public UserRepository userRepository() {
    return new MemoryUserRepository();
  }
}
