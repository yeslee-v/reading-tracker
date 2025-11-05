package io.reading_tracker.domain.userbook;

import io.reading_tracker.domain.BaseEntity;
import io.reading_tracker.domain.book.Book;
import io.reading_tracker.domain.book.State;
import io.reading_tracker.domain.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(
    name = "user_book",
    uniqueConstraints =
        @UniqueConstraint(
            name = "uk_user_book_user_id_book_id",
            columnNames = {"user_id", "book_id"}))
public class UserBook extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Setter
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Setter
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "book_id", nullable = false)
  private Book book;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private State state = State.IN_PROGRESS;

  @Column(name = "total_pages", nullable = false)
  private Integer totalPages;

  @Column(name = "current_page")
  private Integer currentPage = 1;

  public UserBook(User user, Book book, State state, Integer totalPages, Integer currentPage) {
    setUser(user);
    setBook(book);

    this.totalPages = totalPages;
    this.currentPage = currentPage;

    validateProgress(this.totalPages, this.currentPage);

    this.state = State.IN_PROGRESS;

    updateStateAfterProgress(state, this.currentPage);
  }

  public void updateProgress(State state, Integer totalPages, Integer currentPage) {
    Integer newCurrentPage = currentPage != null ? currentPage : this.currentPage;

    validateProgress(totalPages, newCurrentPage);

    this.currentPage = newCurrentPage;

    updateStateAfterProgress(state, newCurrentPage);
  }

  private void validateProgress(Integer totalPages, Integer currentPage) {
    if (totalPages < 1) {
      throw new IllegalArgumentException("전체 페이지는 1 이상이어야 합니다.");
    }

    if (currentPage < 1) {
      throw new IllegalArgumentException("현재 페이지는 1 이상이어야 합니다.");
    }

    if (totalPages < currentPage) {
      throw new IllegalArgumentException("현재 페이지는 전체 페이지를 초과할 수 없습니다.");
    }
  }

  private void updateStateAfterProgress(State requestedState, Integer currentPage) {

    if (requestedState != null) {
      this.state = requestedState;
      return;
    }

    this.state = currentPage.equals(totalPages) ? State.COMPLETED : State.IN_PROGRESS;
  }
}
