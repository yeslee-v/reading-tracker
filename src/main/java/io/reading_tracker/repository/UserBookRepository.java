package io.reading_tracker.repository;

import io.reading_tracker.domain.book.State;
import io.reading_tracker.domain.userbook.UserBook;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserBookRepository extends JpaRepository<UserBook, Long> {

  @EntityGraph(attributePaths = "book")
  List<UserBook> findByUserIdAndState(Long userId, State state, Sort sort);

  int countByUserIdAndState(Long userId, State state);

  Optional<UserBook> findByUserIdAndBookId(Long userId, Long bookId);
}
