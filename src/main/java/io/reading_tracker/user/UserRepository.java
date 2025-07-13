package io.reading_tracker.user;

import java.util.Optional;

public interface UserRepository {
  void save(User user);

  Optional<User> findByEmail(String email);
}
