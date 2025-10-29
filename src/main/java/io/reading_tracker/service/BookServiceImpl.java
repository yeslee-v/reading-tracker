package io.reading_tracker.service;

import io.reading_tracker.auth.PrincipalDetails;
import io.reading_tracker.domain.book.Book;
import io.reading_tracker.domain.book.State;
import io.reading_tracker.domain.user.User;
import io.reading_tracker.domain.userbook.UserBook;
import io.reading_tracker.repository.UserBookRepository;
import io.reading_tracker.request.SaveBookRequest;
import io.reading_tracker.response.GetBookListResponse;
import io.reading_tracker.response.SaveBookResponse;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {

  private static final Sort UPDATED_AT_DESC = Sort.by(Sort.Direction.DESC, "updatedAt");
  private static final int DEFAULT_PAGE_SIZE = 10;

  private final UserBookRepository userBookRepository;

  @PersistenceContext private EntityManager entityManager;

  @Override
  public GetBookListResponse getBookList(Long userId, State stateFilter, int page) {
    Pageable pageable = PageRequest.of(page, DEFAULT_PAGE_SIZE, UPDATED_AT_DESC);

    Page<UserBook> userBooks =
        userBookRepository.findByUserIdAndState(userId, stateFilter, pageable);

    List<GetBookListResponse.BookItem> bookItems =
        userBooks.getContent().stream().map(this::toBookItem).toList();

    GetBookListResponse.Summary summary =
        new GetBookListResponse.Summary(
            toCount(userBookRepository.countByUserIdAndState(userId, State.PLANNED)),
            toCount(userBookRepository.countByUserIdAndState(userId, State.IN_PROGRESS)),
            toCount(userBookRepository.countByUserIdAndState(userId, State.COMPLETED)),
            toCount(userBookRepository.countByUserIdAndState(userId, State.ARCHIVED)));

    GetBookListResponse.Pagination pagination =
        new GetBookListResponse.Pagination(
            userBooks.getNumber() + 1,
            userBooks.getTotalPages(),
            userBooks.getTotalElements(),
            userBooks.hasPrevious(),
            userBooks.hasNext());

    // 캐싱

    return new GetBookListResponse(summary, bookItems, pagination);
  }

  private GetBookListResponse.BookItem toBookItem(UserBook userBook) {
    Integer totalPages = userBook.getTotalPages();
    Integer currentPage = userBook.getCurrentPage();

    return new GetBookListResponse.BookItem(
        userBook.getId(),
        userBook.getBook().getName(),
        userBook.getBook().getAuthor(),
        userBook.getBook().getPublisher(),
        currentPage,
        totalPages,
        calculateProgress(currentPage, totalPages),
        userBook.getState());
  }

  private int calculateProgress(Integer currentPage, Integer totalPages) {
    if (totalPages == null || totalPages == 0) {
      return 0;
    }

    double progress = (double) currentPage / totalPages * 100.0;
    return (int) Math.floor(progress);
  }

  private int toCount(long count) {
    return Math.toIntExact(count);
  }

  @Override
  @Transactional
  public SaveBookResponse saveBook(SaveBookRequest request) {
    String title = normalize(request.title());
    String author = normalize(request.author());
    String publisher = normalize(request.publisher());
    String isbn = normalize(request.isbn());
    Integer totalPages = request.totalPages();

    if (title == null) {
      throw new IllegalArgumentException("책 제목이 필요합니다.");
    }

    if (author == null) {
      throw new IllegalArgumentException("저자 정보가 필요합니다.");
    }

    if (isbn == null) {
      throw new IllegalArgumentException("ISBN이 필요합니다.");
    }

    if (totalPages == null || totalPages < 1) {
      throw new IllegalArgumentException("전체 페이지 수는 1 이상이어야 합니다.");
    }

    User user = getCurrentUser();

    if (isBookAlreadyRegistered(user.getId(), isbn)) {
      throw new IllegalStateException("이미 추가된 책입니다.");
    }

    Book book = findOrCreateBook(isbn, title, author, publisher);

    UserBook userBook = new UserBook(user, book, State.PLANNED, totalPages, 1);
    UserBook savedUserBook = userBookRepository.save(userBook);

    return new SaveBookResponse(
        savedUserBook.getId(),
        savedUserBook.getBook().getName(),
        savedUserBook.getBook().getAuthor(),
        savedUserBook.getBook().getPublisher(),
        savedUserBook.getState(),
        savedUserBook.getCurrentPage(),
        savedUserBook.getTotalPages());
  }

  private User getCurrentUser() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    if (authentication == null
        || !(authentication.getPrincipal() instanceof PrincipalDetails principal)) {
      throw new IllegalStateException("인증된 사용자가 존재하지 않습니다.");
    }

    User user = entityManager.find(User.class, principal.getUserId());

    if (user == null) {
      throw new IllegalStateException("사용자를 찾을 수 없습니다.");
    }

    return user;
  }

  private Book findOrCreateBook(String isbn, String title, String author, String publisher) {
    TypedQuery<Book> query =
        entityManager
            .createQuery("SELECT b FROM Book b WHERE b.isbn = :isbn", Book.class)
            .setParameter("isbn", isbn)
            .setMaxResults(1);

    List<Book> books = query.getResultList();

    if (!books.isEmpty()) {
      return books.get(0);
    }

    Book newBook = new Book(title, author, publisher, isbn);
    entityManager.persist(newBook);
    return newBook;
  }

  private boolean isBookAlreadyRegistered(Long userId, String isbn) {
    Long count =
        entityManager
            .createQuery(
                "SELECT COUNT(ub) FROM UserBook ub WHERE ub.user.id = :userId AND ub.book.isbn = :isbn",
                Long.class)
            .setParameter("userId", userId)
            .setParameter("isbn", isbn)
            .getSingleResult();

    return count != null && count > 0;
  }

  private String normalize(String value) {
    if (value == null) {
      return null;
    }

    String trimmed = value.trim();
    return trimmed.isEmpty() ? null : trimmed;
  }
}
