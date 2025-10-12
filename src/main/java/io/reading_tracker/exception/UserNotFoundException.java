package io.reading_tracker.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class UserNotFoundException extends RuntimeException {

  public UserNotFoundException(Long userId) {
    super("사용자를 찾을 수 없습니다: " + userId);
  }

  public UserNotFoundException(String email) {
    super("사용자를 찾을 수 없습니다: " + email);
  }
}
