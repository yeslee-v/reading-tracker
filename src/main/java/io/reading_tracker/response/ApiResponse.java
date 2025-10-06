package io.reading_tracker.response;

import io.reading_tracker.exception.ErrorResponse;

public record ApiResponse<T>(boolean success, T data, ErrorResponse error) {

  public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, data, null);
    }

    public static <T> ApiResponse<T> error(ErrorResponse error) {
        return new ApiResponse<>(false, null, error);
    }
}
