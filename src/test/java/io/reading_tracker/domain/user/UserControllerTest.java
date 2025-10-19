package io.reading_tracker.domain.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.reading_tracker.controller.UserController;
import io.reading_tracker.request.UpdateNicknameRequest;
import io.reading_tracker.exception.UserNotFoundException;
import io.reading_tracker.repository.UserRepository;
import io.reading_tracker.response.ApiResponse;
import io.reading_tracker.response.UserResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class UserControllerTest {

  @Autowired
  private UserController userController;

  @Autowired
  private UserRepository userRepository;

  @Test
  @DisplayName("아이디로 유저 조회")
  void getUserById() {
    User saved = userRepository.save(new User("tester", "tester@example.com", "local", "local-id"));

    ApiResponse<UserResponse> response = userController.getUserById(saved.getId());

    assertThat(response.success()).isTrue();
    assertThat(response.data()).isNotNull();
    assertThat(response.data().id()).isEqualTo(saved.getId());
    assertThat(response.data().nickname()).isEqualTo("tester");
    assertThat(response.data().email()).isEqualTo("tester@example.com");
  }

  @Test
  @DisplayName("이메일로 유저 검색")
  void findUserByEmail() {
    userRepository.save(new User("tester", "tester@example.com", "local", "local-id"));

    ApiResponse<UserResponse> response = userController.findUserByEmail("tester@example.com");

    assertThat(response.success()).isTrue();
    assertThat(response.data()).isNotNull();
    assertThat(response.data().nickname()).isEqualTo("tester");
  }

  @Test
  @DisplayName("존재하지 않는 이메일 검색 시 예외 처리")
  void findUserByEmail_notFound() {
    assertThatThrownBy(() -> userController.findUserByEmail("missing@example.com"))
        .isInstanceOf(UserNotFoundException.class)
        .hasMessageContaining("사용자를 찾을 수 없습니다");
  }

  @Test
  @DisplayName("유저 닉네임 수정")
  void updateNickname() {
    User saved = userRepository.save(new User("tester", "tester@example.com", "local", "local-id"));
    UpdateNicknameRequest request = new UpdateNicknameRequest("new-nickname");

    ApiResponse<UserResponse> response = userController.updateNickname(saved.getId(), request);

    assertThat(response.success()).isTrue();
    assertThat(response.data()).isNotNull();
    assertThat(response.data().nickname()).isEqualTo("new-nickname");
  }

  @Test
  @DisplayName("빈 닉네임 예외 처리")
  void updateNickname_blank() {
    User saved = userRepository.save(new User("tester", "tester@example.com", "local", "local-id"));
    UpdateNicknameRequest request = new UpdateNicknameRequest(" ");

    assertThatThrownBy(() -> userController.updateNickname(saved.getId(), request))
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining("닉네임은 비어 있을 수 없습니다.");
  }
}
