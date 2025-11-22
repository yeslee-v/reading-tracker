package io.reading_tracker.service;

import io.reading_tracker.annotation.DistributedLock;
import io.reading_tracker.domain.book.Book;
import io.reading_tracker.domain.book.State;
import io.reading_tracker.domain.user.User;
import io.reading_tracker.domain.userbook.UserBook;
import io.reading_tracker.repository.BookRepository;
import io.reading_tracker.repository.UserBookRepository;
import io.reading_tracker.request.AddUserBookRequest;
import io.reading_tracker.request.UpdateUserBookRequest;
import io.reading_tracker.response.AddUserBookResponse;
import io.reading_tracker.response.GetBookListResponse;
import io.reading_tracker.response.UpdateUserBookResponse;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {

  private static final Sort CREATED_AT_DESC = Sort.by(Sort.Direction.DESC, "createdAt");

  private final BookRepository bookRepository;
  private final UserBookRepository userBookRepository;

  @Override
  @Cacheable(cacheNames = "userBookList", key = "#userId + '::' + #stateFilter")
  public GetBookListResponse getBookList(Long userId, State stateFilter) {
    List<UserBook> userBooks =
        userBookRepository.findByUserIdAndState(userId, stateFilter, CREATED_AT_DESC);

    List<GetBookListResponse.BookItem> bookItems =
        userBooks.stream().map(this::toBookItem).toList();

    GetBookListResponse.Summary summary =
        new GetBookListResponse.Summary(
            toCount(userBookRepository.countByUserIdAndState(userId, State.IN_PROGRESS)),
            toCount(userBookRepository.countByUserIdAndState(userId, State.COMPLETED)),
            toCount(userBookRepository.countByUserIdAndState(userId, State.ARCHIVED)));

    // 캐싱

    return new GetBookListResponse(summary, bookItems);
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
  @Caching(
      evict = {
        @CacheEvict(cacheNames = "userBookList", key = "#user.id + '::IN_PROGRESS'"),
        @CacheEvict(cacheNames = "userBookList", key = "#user.id + '::COMPLETED'"),
        @CacheEvict(cacheNames = "userBookList", key = "#user.id + '::ARCHIVED'"),
      })
  @DistributedLock(key = "'addBook:' + #user.id + ':' + #request.isbn")
  public AddUserBookResponse addBookToUserLibrary(User user, AddUserBookRequest request) {
    String title = request.title();
    String author = request.author();
    String publisher = request.publisher();
    String isbn = request.isbn();
    Integer totalPages = request.totalPages();

    Book book =
        bookRepository
            .findBookByIsbn(isbn)
            .orElseGet(
                () -> {
                  Book newBook = new Book(title, author, publisher, isbn);
                  bookRepository.save(newBook);

                  return newBook;
                });

    Optional<UserBook> isBookRegistered =
        userBookRepository.findByUserIdAndBookId(user.getId(), book.getId());

    if (isBookRegistered.isPresent()) {
      throw new IllegalStateException("이미 사용자의 도서 목록에 추가되어 있습니다.");
    }

    UserBook newUserBook = new UserBook(user, book, State.IN_PROGRESS, totalPages, 1);
    UserBook savedUserBook = userBookRepository.save(newUserBook);

    return new AddUserBookResponse(
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
  @Caching(
      evict = {
        @CacheEvict(cacheNames = "userBookList", key = "#user.id + '::IN_PROGRESS'"),
        @CacheEvict(cacheNames = "userBookList", key = "#user.id + '::COMPLETED'"),
        @CacheEvict(cacheNames = "userBookList", key = "#user.id + '::ARCHIVED'"),
      })
  public UpdateUserBookResponse updateUserBookProgress(User user, UpdateUserBookRequest request) {
    Long userBookId = request.id();

    UserBook userBook =
        userBookRepository
            .findById(userBookId)
            .orElseThrow(() -> new IllegalStateException("해당 도서는 사용자 목록 내 존재하지 않습니다."));

    Integer currentPage = request.currentPage();

    State targetState = request.state();
    userBook.updateProgress(targetState, userBook.getTotalPages(), currentPage);

    return new UpdateUserBookResponse(
        userBook.getId(),
        calculateProgress(userBook.getCurrentPage(), userBook.getTotalPages()),
        userBook.getCurrentPage(),
        userBook.getState());
  }

  private int calculateProgress(Integer currentPage, Integer totalPages) {
    double progress = (double) currentPage / totalPages * 100.0;
    return (int) Math.floor(progress);
  }

  private int toCount(long count) {
    return Math.toIntExact(count);
  }
}
