package io.reading_tracker.service;

import io.reading_tracker.auth.PrincipalDetails;
import io.reading_tracker.domain.book.Book;
import io.reading_tracker.domain.book.State;
import io.reading_tracker.domain.user.User;
import io.reading_tracker.domain.userbook.UserBook;
import io.reading_tracker.repository.BookRepository;
import io.reading_tracker.repository.UserBookRepository;
import io.reading_tracker.request.SaveBookRequest;
import io.reading_tracker.request.UpdateBookRequest;
import io.reading_tracker.response.GetBookListResponse;
import io.reading_tracker.response.SaveBookResponse;
import io.reading_tracker.response.UpdateBookResponse;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
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

  private final BookRepository bookRepository;
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
        userBook.getBook().getTitle(),
        userBook.getBook().getAuthor(),
        userBook.getBook().getPublisher(),
        currentPage,
        totalPages,
        calculateProgress(currentPage, totalPages),
        userBook.getState());
  }

  @Override
  @Transactional
  public SaveBookResponse saveBook(SaveBookRequest request) {
    Long id = request.id();
    String title = request.title();
    String author = request.author();
    String publisher = request.publisher();
    String isbn = request.isbn();
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
    Optional<UserBook> isBookRegistered =
        userBookRepository.findByUserIdAndBookId(user.getId(), id);

    if (isBookRegistered.isPresent()) {
      throw new IllegalStateException("이미 추가된 책입니다.");
    }

    Book book = findOrCreateBook(id, isbn, title, author, publisher);

    UserBook userBook = new UserBook(user, book, State.PLANNED, totalPages, 1);
    UserBook savedUserBook = userBookRepository.save(userBook);

    return new SaveBookResponse(
        savedUserBook.getId(),
        savedUserBook.getBook().getTitle(),
        savedUserBook.getBook().getAuthor(),
        savedUserBook.getBook().getPublisher(),
        savedUserBook.getState(),
        savedUserBook.getCurrentPage(),
        savedUserBook.getTotalPages());
  }

  @Override
  @Transactional
  public UpdateBookResponse updateBook(UpdateBookRequest request) {
    Long id = request.id();
    Integer currentPage = request.currentPage();
    String state = request.state();

    User user = getCurrentUser();
    Long userId = user.getId();
    Optional<UserBook> userBook = userBookRepository.findByUserIdAndBookId(userId, id);

    if (userBook.isEmpty()) {
      throw new IllegalStateException("추가되지 않은 도서입니다.");
    }

    if (currentPage != null) {
      userBookRepository.updateCurrentPageByUserId(userId, currentPage);
    }

    if (Objects.equals(currentPage, userBook.get().getTotalPages()) || !state.isBlank()) {
      userBookRepository.updateBookStateByUserId(userId, State.from(state));
    }

    return new UpdateBookResponse(
        userBook.get().getBook().getId(),
        calculateProgress(userBook.get().getCurrentPage(), userBook.get().getTotalPages()),
        userBook.get().getCurrentPage(),
        userBook.get().getState());
  }

  private int calculateProgress(Integer currentPage, Integer totalPages) {
    double progress = (double) currentPage / totalPages * 100.0;
    return (int) Math.floor(progress);
  }

  private int toCount(long count) {
    return Math.toIntExact(count);
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

  private Book findOrCreateBook(
      Long id, String isbn, String title, String author, String publisher) {
    Optional<Book> book = bookRepository.findById(id);

    if (book.isPresent()) {
      return book.get();
    }

    Book newBook = new Book(title, author, publisher, isbn);

    bookRepository.save(newBook);

    return newBook;
  }
}
