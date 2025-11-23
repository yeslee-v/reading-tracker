package io.reading_tracker.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import io.reading_tracker.domain.user.User;
import io.reading_tracker.exception.UserNotFoundException;
import io.reading_tracker.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

  @Mock private UserRepository userRepository;

  private UserServiceImpl userService;

  @BeforeEach
  void setUp() {
    userService = new UserServiceImpl(userRepository);
  }

  @Test
  @DisplayName("아이디로 사용자를 조회한다")
  void getUserById_success() {
    User user = createUser(1L, "tester", "tester@example.com");
    when(userRepository.findById(1L)).thenReturn(Optional.of(user));

    User result = userService.getUserById(1L);

    assertThat(result).isSameAs(user);
  }

  @Test
  @DisplayName("아이디로 사용자를 조회할 때 없으면 예외를 던진다")
  void getUserById_notFound() {
    when(userRepository.findById(1L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> userService.getUserById(1L))
        .isInstanceOf(UserNotFoundException.class)
        .hasMessageContaining("사용자를 찾을 수 없습니다");
  }

  @Test
  @DisplayName("이메일로 사용자를 찾는다")
  void findUserByEmail() {
    User user = createUser(1L, "tester", "tester@example.com");
    when(userRepository.findByEmail("tester@example.com")).thenReturn(Optional.of(user));

    Optional<User> result = userService.findUserByEmail("tester@example.com");

    assertThat(result).contains(user);
  }

  @Test
  @DisplayName("닉네임을 수정한다")
  void updateNickname_success() {
    User user = createUser(1L, "tester", "tester@example.com");
    when(userRepository.findById(1L)).thenReturn(Optional.of(user));

    User updated = userService.updateNickname(1L, "new-nickname");

    assertThat(updated.getNickname()).isEqualTo("new-nickname");
  }

  @Test
  @DisplayName("빈 닉네임으로 수정하면 예외를 던진다")
  void updateNickname_blank() {
    User user = createUser(1L, "tester", "tester@example.com");
    when(userRepository.findById(1L)).thenReturn(Optional.of(user));

    assertThatThrownBy(() -> userService.updateNickname(1L, " "))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("닉네임은 비어 있을 수 없습니다.");
  }

  private User createUser(Long id, String nickname, String email) {
    User user = new User(nickname, email);
    ReflectionTestUtils.setField(user, "id", id);
    return user;
  }
}
