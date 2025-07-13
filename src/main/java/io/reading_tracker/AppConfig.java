package io.reading_tracker;

import io.reading_tracker.user.MemoryUserRepository;
import io.reading_tracker.user.UserRepository;
import io.reading_tracker.user.UserService;
import io.reading_tracker.user.UserServiceImpl;
import org.springframework.context.annotation.Bean;

public class AppConfig {
  @Bean
  public UserService userService() {
    return new UserServiceImpl(userRepository());
  }

  @Bean
  public UserRepository userRepository() {
    return new MemoryUserRepository();
  }
}
