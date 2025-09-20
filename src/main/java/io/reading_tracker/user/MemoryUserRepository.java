package io.reading_tracker.user;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class MemoryUserRepository implements UserRepository{
  private Map<Long, User> users = new HashMap<>();

  @Override
  public void save(User user) {
    users.put(user.getId(), user);
  }

  @Override
  public Optional<User> findByEmail(String email) {
    return users.values().stream()
        .filter(user -> user.getEmail().equals(email))
        .findFirst();
  }
}
