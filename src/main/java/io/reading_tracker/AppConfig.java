package io.reading_tracker;

import io.reading_tracker.book.BookRepository;
import io.reading_tracker.book.BookService;
import io.reading_tracker.book.BookServiceImpl;
import io.reading_tracker.book.MemoryBookRepository;
import io.reading_tracker.user.MemoryUserRepository;
import io.reading_tracker.user.UserRepository;
import io.reading_tracker.user.UserService;
import io.reading_tracker.user.UserServiceImpl;
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
