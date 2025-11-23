package io.reading_tracker.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.reading_tracker.domain.user.User;
import io.reading_tracker.exception.UserNotFoundException;
import io.reading_tracker.repository.UserRepository;
import io.reading_tracker.response.UserResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class UserControllerTest {

  @Autowired private UserController userController;

  @Autowired private UserRepository userRepository;

  @Test
  @DisplayName("아이디로 유저 조회")
  void getUserById() {
    User saved = userRepository.save(new User("tester", "tester@example.com"));

    ResponseEntity<UserResponse> response = userController.getUserById(saved.getId());

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response).isNotNull();
    assertThat(response.getBody().id()).isEqualTo(saved.getId());
    assertThat(response.getBody().nickname()).isEqualTo("tester");
    assertThat(response.getBody().email()).isEqualTo("tester@example.com");
  }

  @Test
  @DisplayName("이메일로 유저 검색")
  void findUserByEmail() {
    userRepository.save(new User("tester", "tester@example.com"));

    ResponseEntity<UserResponse> response = userController.findUserByEmail("tester@example.com");

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response).isNotNull();
    assertThat(response.getBody().nickname()).isEqualTo("tester");
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
//    User saved = userRepository.save(new User("tester", "tester@example.com"));
//    UpdateNicknameRequest request = new UpdateNicknameRequest(saved.getId(), "new-nickname");
//
//    ResponseEntity<UserResponse> response = userController.updateNickname(saved.getId(), request);
//
//    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
//    assertThat(response).isNotNull();
//    assertThat(response.getBody().nickname()).isEqualTo("new-nickname");
  }

  @Test
  @DisplayName("빈 닉네임 예외 처리")
  void updateNickname_blank() {
//    User saved = userRepository.save(new User("tester", "tester@example.com"));
//    UpdateNicknameRequest request = new UpdateNicknameRequest(saved.getId(), " ");
//
//    assertThatThrownBy(() -> userController.updateNickname(saved.getId(), request))
//        .isInstanceOf(RuntimeException.class)
//        .hasMessageContaining("닉네임은 비어 있을 수 없습니다.");
  }
}
