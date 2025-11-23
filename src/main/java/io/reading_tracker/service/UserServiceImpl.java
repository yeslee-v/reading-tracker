package io.reading_tracker.service;

import io.reading_tracker.domain.user.User;
import io.reading_tracker.exception.UserNotFoundException;
import io.reading_tracker.repository.UserRepository;
import io.reading_tracker.request.UpdateNicknameRequest;
import io.reading_tracker.response.UpdateNicknameResponse;
import io.reading_tracker.response.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

  private final UserRepository userRepository;

  @Override
  public UserResponse getUserById(Long userId) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new UserNotFoundException("유효하지 않은 사용자입니다"));

    return UserResponse.from(user);
  }

  @Override
  @Transactional
  public UpdateNicknameResponse updateNickname(Long userId, UpdateNicknameRequest request) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new UserNotFoundException("유효하지 않은 사용자입니다"));
    user.updateNickname(request.nickname());

    return UpdateNicknameResponse.from(user);
  }
}
