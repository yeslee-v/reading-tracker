package io.reading_tracker.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateNicknameRequest(
    @NotBlank(message = "nickname은 비어있을 수 없습니다") @Size(max = 50, message = "닉네임은 50자 이하여야 합니다")
        String nickname) {}
