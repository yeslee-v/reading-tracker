package io.reading_tracker.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
  public ResponseEntity<Object> handleBadRequestException(Exception e) {
    log.warn("400 Bad Request: {}", e.getMessage());
    ErrorResponse errorResponse = new ErrorResponse(HttpStatus.BAD_REQUEST.name(), e.getMessage());
    return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Object> handleMethodArgumentNotValidException(
      MethodArgumentNotValidException e) {
    log.error("400 Bad Request (Validation Failed): {}", e.getMessage());
    FieldError fieldError = e.getBindingResult().getFieldError();
    String errorMessage = fieldError == null ? "유효성 검사에 실패했습니다" : fieldError.getDefaultMessage();

    ErrorResponse errorResponse = new ErrorResponse(HttpStatus.BAD_REQUEST.name(), errorMessage);
    return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(ResponseStatusException.class)
  public ResponseEntity<Object> handleResponseStatusException(ResponseStatusException e) {
    log.error("MethodArgumentNotValidException: {}", e.getMessage());
    ErrorResponse errorResponse = new ErrorResponse(e.getStatusCode().toString(), e.getMessage());
    return new ResponseEntity<>(errorResponse, e.getStatusCode());
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<Object> handleException(Exception e) {
    log.error("500 Internal Server Error: ", e);
    ErrorResponse errorResponse = new ErrorResponse("INTERNAL_SERVER_ERROR", "서버 오류가 발생했습니다.");
    return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
  }
}
