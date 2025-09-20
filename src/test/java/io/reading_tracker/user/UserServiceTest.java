package io.reading_tracker.user;

import io.reading_tracker.AppConfig;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

public class UserServiceTest {
  UserService userService;

  @BeforeEach
  public void beforeEach() {
    AppConfig appConfig = new AppConfig();

    userService = appConfig.userService();
  }

  @Test
  void join() {
    // given
    User newUser = new User(1L, "user1", "user1@gmail.com");

    // when
    userService.join(newUser);
    Optional<User> findUser = userService.findUser(newUser.getEmail());

    // then
    Assertions.assertThat(findUser).isPresent();
    Assertions.assertThat(findUser.get().getEmail()).isEqualTo(newUser.getEmail());
    Assertions.assertThat(findUser.get().getNickname()).isEqualTo(newUser.getNickname());
  }

  @Test
  void joinWithDuplicateUser() {
    // given
    User user = new User(1L, "user1", "user1@gmail.com");
    userService.join(user);

    // when & then
    Assertions.assertThatThrownBy(() -> userService.join(user))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("User already exists");
  }

  @Test
  void updateUser() {
    // given
    User user = new User(1L, "user1", "user1@gmail.com");
    userService.join(user);
    String newNickname = "updatedUser";

    // when
    userService.updateUser(newNickname, user.getEmail());

    // then
    User updatedUser = userService.findUser(user.getEmail()).orElseThrow();
    Assertions.assertThat(updatedUser.getNickname()).isEqualTo(newNickname);
  }

  @Test
  void updateUserNotFound() {
    // given
    String nonExistentEmail = "nonexistent@gmail.com";
    String newNickname = "updatedUser";

    // when & then
    Assertions.assertThatThrownBy(() -> userService.updateUser(newNickname, nonExistentEmail))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("User not found!");
  }

  @Test
  void findUserNotFound() {
    // given
    String nonExistentEmail = "nonexistent@gmail.com";

    // when
    Optional<User> result = userService.findUser(nonExistentEmail);

    // then
    Assertions.assertThat(result).isNotPresent();
  }
}

