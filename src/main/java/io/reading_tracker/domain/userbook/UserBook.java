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

  @Column(name = "full_page")
  private Integer fullPage;

  @Column(name = "current_page", nullable = false)
  private Integer currentPage = 1;

  @Version
  private int version;

  public UserBook(User user, Book book, State state, Integer fullPage, Integer currentPage) {
    setUser(user);
    setBook(book);
    this.fullPage = fullPage;
    this.currentPage = currentPage == null ? 1 : currentPage;
    validateProgress(this.fullPage, this.currentPage);
    this.state = State.PLANNED;
    updateStateAfterProgress(state, this.fullPage, this.currentPage, false, true);
  }

  public void updateProgress(State state, Integer fullPage, Integer currentPage) {
    Integer newFullPage = fullPage != null ? fullPage : this.fullPage;
    Integer newCurrentPage = currentPage != null ? currentPage : this.currentPage;

    validateProgress(newFullPage, newCurrentPage);

    boolean currentPageChanged = !newCurrentPage.equals(this.currentPage);

    this.fullPage = newFullPage;
    this.currentPage = newCurrentPage;

    updateStateAfterProgress(state, newFullPage, newCurrentPage, currentPageChanged, false);
  }

  public void setUser(User user) {
    this.user = user;
  }

  public void setBook(Book book) {
    this.book = book;
  }

  private void validateProgress(Integer fullPage, Integer currentPage) {
    if (currentPage == null || currentPage < 1) {
      throw new IllegalArgumentException("현재 페이지는 1 이상이어야 합니다.");
    }

    if (fullPage != null && currentPage > fullPage) {
      throw new IllegalArgumentException("현재 페이지는 전체 페이지를 초과할 수 없습니다.");
    }
  }

  private void updateStateAfterProgress(
      State requestedState,
      Integer fullPage,
      Integer currentPage,
      boolean currentPageChanged,
      boolean initialRegistration) {

    if (requestedState != null) {
      if (requestedState == State.ARCHIVED) {
        this.state = State.ARCHIVED;
        return;
      }

      if (requestedState == State.COMPLETED) {
        this.state = State.COMPLETED;
        return;
      }

      if (requestedState == State.IN_PROGRESS) {
        this.state = State.IN_PROGRESS;
        return;
      }

      if (requestedState == State.PLANNED) {
        this.state = State.PLANNED;
        return;
      }
    }

    if (initialRegistration) {
      this.state = State.PLANNED;
      return;
    }

    if (fullPage != null && currentPage != null && currentPage.equals(fullPage)) {
      this.state = State.COMPLETED;
      return;
    }

    if (currentPageChanged) {
      this.state = State.IN_PROGRESS;
      return;
    }

    if (this.state == State.PLANNED && currentPage != null && currentPage > 1) {
      this.state = State.IN_PROGRESS;
    }
  }

  @PrePersist
  private void ensureCurrentPage() {
    if (currentPage == null || currentPage < 1) {
      currentPage = 1;
    }
  }
}
