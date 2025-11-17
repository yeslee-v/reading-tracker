package io.reading_tracker.domain.book;


public enum State {
  IN_PROGRESS,
  COMPLETED,
  ARCHIVED;

  // 다른 타입의 값 받아 변환할 때
  public static State from(String state) {
    return switch (state) {
      case "IN_PROGRESS" -> State.IN_PROGRESS;
      case "COMPLETED" -> State.COMPLETED;
      case "ARCHIVED" -> State.ARCHIVED;
      default ->
          throw new IllegalArgumentException("지원하지 않는 상태입니다: " + state);
    };
  }
}
