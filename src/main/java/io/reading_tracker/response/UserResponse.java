package io.reading_tracker.response;

import io.reading_tracker.domain.user.User;

public record UserResponse(Long id, String nickname, String email) {

  public static UserResponse from(User user) {
    return new UserResponse(user.getId(), user.getNickname(), user.getEmail());
  }
}

