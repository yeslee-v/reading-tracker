package io.reading_tracker.user;

import java.util.Optional;

public interface UserService {
  void join(String nickname, String email);

  Optional<User> findUser(String email);

  void updateUser(String nickname, String email);
}
