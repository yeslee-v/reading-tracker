package io.reading_tracker.repository;

import io.reading_tracker.domain.book.State;
import io.reading_tracker.domain.userbook.UserBook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserBookRepository extends JpaRepository<UserBook, Long> {

  @EntityGraph(attributePaths = "book")
  Page<UserBook> findByUserIdAndState(Long userId, State state, Pageable pageable);

  int countByUserIdAndState(Long userId, State state);
}
