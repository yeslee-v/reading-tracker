package io.reading_tracker.response;

import io.reading_tracker.domain.user.User;

public record UpdateNicknameResponse(Long id, String nickname) {

  public static UpdateNicknameResponse from(User user) {
    return new UpdateNicknameResponse(user.getId(), user.getNickname());
  }
}