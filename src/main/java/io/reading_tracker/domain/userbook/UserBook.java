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
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(
    name = "user_book",
    uniqueConstraints =
        @UniqueConstraint(name = "uk_user_book_user_id_book_id", columnNames = {"user_id", "book_id"}))
public class UserBook extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "book_id", nullable = false)
  private Book book;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private State state = State.PLANNED;

  @Column(name = "total_pages", nullable = false)
  private Integer totalPages;

  @Column(name = "current_page", nullable = false)
  private Integer currentPage = 1;

  public UserBook(User user, Book book, State state, Integer totalPages, Integer currentPage) {
    setUser(user);
    setBook(book);
    this.totalPages = totalPages;
    this.currentPage = currentPage == null ? 1 : currentPage;
    validateProgress(this.totalPages, this.currentPage);
    this.state = State.PLANNED;
    updateStateAfterProgress(state, this.totalPages, this.currentPage, false, true);
  }

  public void updateProgress(State state, Integer totalPages, Integer currentPage) {
    Integer newTotalPages = totalPages != null ? totalPages : this.totalPages;
    Integer newCurrentPage = currentPage != null ? currentPage : this.currentPage;

    validateProgress(newTotalPages, newCurrentPage);

    boolean currentPageChanged = !newCurrentPage.equals(this.currentPage);

    this.totalPages = newTotalPages;
    this.currentPage = newCurrentPage;

    updateStateAfterProgress(state, newTotalPages, newCurrentPage, currentPageChanged, false);
  }

  public void setUser(User user) {
    this.user = user;
  }

  public void setBook(Book book) {
    this.book = book;
  }

  private void validateProgress(Integer totalPages, Integer currentPage) {
    if (currentPage == null || currentPage < 1) {
      throw new IllegalArgumentException("현재 페이지는 1 이상이어야 합니다.");
    }

    if (totalPages != null && currentPage > totalPages) {
      throw new IllegalArgumentException("현재 페이지는 전체 페이지를 초과할 수 없습니다.");
    }
  }

  private void updateStateAfterProgress(
    State requestedState,
    Integer totalPages,
    Integer currentPage,
    boolean currentPageChanged,
    boolean initialRegistration) {

    switch (requestedState) {
      case State.PLANNED:
        this.state = State.PLANNED;
        return;
      case State.IN_PROGRESS:
        this.state = State.IN_PROGRESS;
        return;
      case State.COMPLETED:
        this.state = State.COMPLETED;
        return;
      case State.ARCHIVED:
        this.state = State.ARCHIVED;
        return;
    }

    if (initialRegistration) {
      this.state = State.PLANNED;
      return;
    }

    if (currentPage.equals(totalPages)) {
      this.state = State.COMPLETED;
      return;
    }

    if (currentPageChanged) {
      this.state = State.IN_PROGRESS;
      return;
    }

    if (this.state == State.PLANNED && currentPage > 1) {
      this.state = State.IN_PROGRESS;
    }
  }

  @PrePersist
  private void ensureCurrentPage() {
    if (currentPage < 1) {
      currentPage = 1;
    }
  }
}
