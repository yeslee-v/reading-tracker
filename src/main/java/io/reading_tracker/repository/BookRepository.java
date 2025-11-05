package io.reading_tracker.repository;

import io.reading_tracker.domain.book.Book;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {
  Optional<Book> findBookByIsbn(String isbn);
}
