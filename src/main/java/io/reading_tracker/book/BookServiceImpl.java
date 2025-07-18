package io.reading_tracker.book;

import io.reading_tracker.user.User;
import io.reading_tracker.user.UserService;
import io.reading_tracker.user.UserServiceImpl;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class BookServiceImpl implements BookService {
  private final BookRepository bookRepository;
  private final UserService userService;

  Map<Long, Book> books = new HashMap<>();

  public BookServiceImpl(BookRepository bookRepository, UserService userService) {
    this.bookRepository = bookRepository;
    this.userService = userService;
  }

  @Override
  public List<Book> findBooksByUser(String email) {
    Optional<User> user = userService.findUser(email);

    if (user.isEmpty()) {
      return List.of();
    }

    List<Book> books = bookRepository.findByUserId(user.get().getId());

    return books;
  }

  @Override
  public void addBook(String email, String title, String author, int fullPage) {
    User user =
        userService.findUser(email)
            .orElseThrow(() -> new IllegalArgumentException("User not found!"));

    // 책은 1페이지부터 시작
    Book book = new Book(user, title, author, State.IN_PROGRESS, fullPage, 1);

    bookRepository.save(book);
  }

  @Override
  // 일단 클라이언트에서는 전부 다 보내주는걸로
  public void updateBook(Long bookId, String title, String author, State state, int currentPage) {
    Book book = bookRepository.findById(bookId);

    if (!title.isEmpty()) {
      book.setTitle(title);
    }

    if (!author.isEmpty()) {
      book.setAuthor(author);
    }

    book.setState(state);
    book.setCurrentPage(currentPage);
    book.setUpdateAt(new Date());

    bookRepository.save(book);
  }
}
