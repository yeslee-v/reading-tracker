package io.reading_tracker.domain.user;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class MemoryUserRepository implements UserRepository{
  private static Map<Long, User> users = new HashMap<>();

  @Override
  public void save(User user) {
    users.put(user.getId(), user);
  }

  @Override
  public Optional<User> findByEmail(String email) {
    return Optional.ofNullable(users.get(email));
  }
}
