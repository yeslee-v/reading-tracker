package io.reading_tracker.book;

import java.util.List;

public interface BookRepository {
  List<Book> findByUserId(Long userId);

  Book findById(Long id);

  void save(Book book); // POST, UPDATE

}
