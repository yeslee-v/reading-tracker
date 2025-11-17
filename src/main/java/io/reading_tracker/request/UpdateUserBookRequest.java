package io.reading_tracker.request;

import io.reading_tracker.domain.book.State;
import io.reading_tracker.validation.AtLeastOneNotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@AtLeastOneNotNull(
    fields = {"currentPage", "state"},
    message = "currentPage 또는 state 둘 중 하나는 값이 존재해야 합니다")
public record UpdateUserBookRequest(
    @NotNull(message = "id는 필수입니다") Long id,
    @Min(value = 1, message = "currentPage는 1 이상이어야 합니다") Integer currentPage,
    State state) {}
