package io.reading_tracker.book;

import java.util.List;

public interface BookService {
  List<Book> findBooksByUser(String email);

  void addBook(String email, String title, String author, int fullPage);

  void updateBook(Long bookId, String title, String author, State state, int currentPage);
}
