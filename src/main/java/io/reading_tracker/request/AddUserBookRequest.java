package io.reading_tracker.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AddUserBookRequest(
    @NotBlank(message = "ISBN은 필수입니다") @Size(min = 10, max = 13, message = "ISBN은 10 ~ 13자리여야 합니다")
        String isbn,
    @NotBlank(message = "title은 필수입니다") String title,
    @NotBlank(message = "author은 필수입니다") String author,
    @NotBlank(message = "publisher는 필수입니다") String publisher,
    @NotNull(message = "totalPages는 필수입니다") @Min(value = 1, message = "totalPages는 1 이상이어야 합니다")
        Integer totalPages) {}
