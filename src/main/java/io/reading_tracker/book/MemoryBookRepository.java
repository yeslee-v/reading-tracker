package io.reading_tracker.book;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MemoryBookRepository implements BookRepository{
  private Map<Long, Book> books = new HashMap<>();

  @Override
  public List<Book> findByUserId(Long userId) {
    List<Book> bookList = books.values().stream()
        .filter(book -> book.getUser() != null && book.getUser().getId().equals(userId)).collect(
            Collectors.toList());

    return bookList;
  }

  @Override
  public Book findById(Long id) {
    return books.get(id);
  }

  @Override
  public void save(Book book) {
    books.put(book.getId(), book);
  }
}
