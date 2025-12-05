package io.reading_tracker.exception;

public class NotOwnerException extends RuntimeException {
  public NotOwnerException(String message) {
    super(message);
  }
}
