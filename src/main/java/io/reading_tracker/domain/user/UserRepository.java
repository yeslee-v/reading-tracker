package io.reading_tracker.domain.user;

import java.util.Optional;

public interface UserRepository {
  void save(User user);

  Optional<User> findByEmail(String email);
}
