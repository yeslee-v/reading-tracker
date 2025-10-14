package io.reading_tracker.domain.user;

import java.util.Optional;

public interface UserService {

  User getUserById(Long userId);

  Optional<User> findUserByEmail(String email);

  User updateNickname(Long userId, String nickname);
}

