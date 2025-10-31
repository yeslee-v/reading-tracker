package io.reading_tracker.domain.book;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public enum State {
  PLANNED,
  IN_PROGRESS,
  COMPLETED,
  ARCHIVED;

  // 다른 타입의 값 받아 변환할 때
  public static State from(String state) {
    return switch (state) {
      case "PLANNED" -> State.PLANNED;
      case "IN_PROGRESS" -> State.IN_PROGRESS;
      case "COMPLETED" -> State.COMPLETED;
      case "ARCHIVED" -> State.ARCHIVED;
      default ->
          throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "지원하지 않는 상태입니다: " + state);
    };
  }
}
