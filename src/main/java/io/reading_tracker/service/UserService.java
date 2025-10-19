package io.reading_tracker.service;

import io.reading_tracker.domain.user.User;
import java.util.Optional;

public interface UserService {

  User getUserById(Long userId);

  Optional<User> findUserByEmail(String email);

  User updateNickname(Long userId, String nickname);
}

