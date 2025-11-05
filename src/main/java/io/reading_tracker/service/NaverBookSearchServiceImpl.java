package io.reading_tracker.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.reading_tracker.response.SearchBookResponse;
import java.util.Collections;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Slf4j
@Service
public class NaverBookSearchServiceImpl implements BookSearchService {

  private static final String BASE_URL = "https://openapi.naver.com";
  private static final String SEARCH_PATH = "/v1/search/book.json";

  private final RestClient restClient;

  public NaverBookSearchServiceImpl(
      @Value("${NAVER_CLIENT_ID}") String clientId,
      @Value("${NAVER_CLIENT_SECRET}") String clientSecret) {
    this.restClient =
        RestClient.builder()
            .baseUrl(BASE_URL)
            .defaultHeader("X-Naver-Client-Id", clientId)
            .defaultHeader("X-Naver-Client-Secret", clientSecret)
            .build();
  }

  @Override
  public SearchBookResponse search(String query) {
    try {
      NaverBookSearchResponse response =
          restClient
              .get()
              .uri(uriBuilder -> uriBuilder.path(SEARCH_PATH).queryParam("query", query).build())
              .retrieve()
              .body(NaverBookSearchResponse.class);

      if (response == null) {
        return new SearchBookResponse(0, 0, Collections.emptyList());
      }

      List<NaverBookItem> items =
          response.items() == null ? Collections.emptyList() : response.items();

      List<SearchBookResponse.BookItem> mappedItems = items.stream().map(this::toBookItem).toList();

      // 캐싱 TTL

      return new SearchBookResponse(response.total(), response.display(), mappedItems);
    } catch (RestClientException ex) {
      log.error("Failed to call Naver book search API", ex);
      throw new RuntimeException("네이버 도서 검색 API 호출에 실패했습니다.");
    }
  }

  private SearchBookResponse.BookItem toBookItem(NaverBookItem item) {
    return new SearchBookResponse.BookItem(
        item.isbn(), item.title(), item.author(), item.publisher(), item.link());
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  private record NaverBookSearchResponse(int total, int display, List<NaverBookItem> items) {}

  @JsonIgnoreProperties(ignoreUnknown = true)
  private record NaverBookItem(
      String isbn, String title, String author, String publisher, String link) {}
}
