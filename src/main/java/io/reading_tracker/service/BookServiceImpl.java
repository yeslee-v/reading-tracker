package io.reading_tracker.service;

import io.reading_tracker.domain.book.State;
import io.reading_tracker.domain.userbook.UserBook;
import io.reading_tracker.repository.UserBookRepository;
import io.reading_tracker.response.GetBookListResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {

  private static final Sort UPDATED_AT_DESC = Sort.by(Sort.Direction.DESC, "updatedAt");
  private static final int DEFAULT_PAGE_SIZE = 10;

  private final UserBookRepository userBookRepository;

  @Override
  public GetBookListResponse getBookList(Long userId, State stateFilter, int page) {
    Pageable pageable = PageRequest.of(page, DEFAULT_PAGE_SIZE, UPDATED_AT_DESC);

    Page<UserBook> userBooks = userBookRepository.findByUserIdAndState(userId, stateFilter, pageable);

    List<GetBookListResponse.BookItem> bookItems = userBooks.getContent().stream()
        .map(this::toBookItem)
        .toList();

    GetBookListResponse.Summary summary = new GetBookListResponse.Summary(
        toCount(userBookRepository.countByUserIdAndState(userId, State.PLANNED)),
        toCount(userBookRepository.countByUserIdAndState(userId, State.IN_PROGRESS)),
        toCount(userBookRepository.countByUserIdAndState(userId, State.COMPLETED)),
        toCount(userBookRepository.countByUserIdAndState(userId, State.ARCHIVED))
    );

    GetBookListResponse.Pagination pagination = new GetBookListResponse.Pagination(
        userBooks.getNumber() + 1,
        userBooks.getTotalPages(),
        userBooks.getTotalElements(),
        userBooks.hasPrevious(),
        userBooks.hasNext()
    );

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
        userBook.getState()
    );
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
}
