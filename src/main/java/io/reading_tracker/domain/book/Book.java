package io.reading_tracker.domain.book;

import io.reading_tracker.domain.BaseEntity;
import io.reading_tracker.domain.userbook.UserBook;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "books", indexes = @Index(name = "uk_books_isbn", columnList = "isbn", unique = true))
public class Book extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "name", nullable = false, length = 200)
  private String name;

  @Column(nullable = false, length = 100)
  private String author;

  @Column(length = 20)
  private String isbn;

  @OneToMany(mappedBy = "book", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
  private Set<UserBook> userBooks = new LinkedHashSet<>();

  public Book(String name, String author, String isbn) {
    this.name = name;
    this.author = author;
    this.isbn = isbn;
  }

  public void updateMetadata(String name, String author, String isbn) {
    this.name = name;
    this.author = author;
    this.isbn = isbn;
  }

  public void addUserBook(UserBook userBook) {
    Objects.requireNonNull(userBook, "userBook은 null일 수 없습니다.");
    userBooks.add(userBook);
    userBook.setBook(this);
  }

  public void removeUserBook(UserBook userBook) {
    if (userBook == null) {
      return;
    }

    if (userBooks.remove(userBook)) {
      userBook.setBook(null);
    }
  }
}
