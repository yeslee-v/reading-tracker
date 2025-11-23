package io.reading_tracker.service;

import io.reading_tracker.request.UpdateNicknameRequest;
import io.reading_tracker.response.UpdateNicknameResponse;
import io.reading_tracker.response.UserResponse;

public interface UserService {

  UserResponse getUserById(Long userId);

  UpdateNicknameResponse updateNickname(Long userId, UpdateNicknameRequest request);
}
