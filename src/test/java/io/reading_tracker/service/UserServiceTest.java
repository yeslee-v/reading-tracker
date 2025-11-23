package io.reading_tracker.service;

import static org.assertj.core.api.Assertions.assertThat;

import io.reading_tracker.domain.user.User;
import io.reading_tracker.exception.UserNotFoundException;
import io.reading_tracker.repository.UserRepository;
import io.reading_tracker.request.UpdateNicknameRequest;
import io.reading_tracker.response.UpdateNicknameResponse;
import io.reading_tracker.response.UserResponse;
import jakarta.transaction.Transactional;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@Import(UserServiceImpl.class)
@ActiveProfiles("test")
@Transactional
class UserServiceTest {

  @Autowired private UserRepository userRepository;

  @Autowired private UserService userService;

  @Test
  @DisplayName("유효한 사용자 아이디로 사용자를 조회하면 사용자 정보가 반환된다")
  void getUserById_withValidUserId_returnUserInfo() {
    // given 유효한 아이디
    User user = userRepository.save(new User("tester", "tester@example.com"));
    Long userId = user.getId();

    // when 사용자를 조회하면
    UserResponse response = userService.getUserById(userId);

    // then 사용자 정보가 반환된다
    assertThat(response.email()).isEqualTo("tester@example.com");
  }

  @Test
  @DisplayName("유효하지 않은 아이디로 사용자를 조회하면 에러를 반환한다")
  void getUserById_withInvalidUserId_throwsError() {
    // given 존재하지 않은 아이디
    Long invalidUserId = 1L;

    // when 사용자를 조회하면
    // then UserNotFoundException 예외를 반환한다
    Assertions.assertThatThrownBy(() -> userService.getUserById(invalidUserId))
        .isInstanceOf(UserNotFoundException.class)
        .hasMessageContaining("유효하지 않은 사용자입니다");
  }

  @Test
  @DisplayName("변경하려는 닉네임이 주어지면 유저의 닉네임을 수정한다")
  void updateNickname_withValidNickname_returnNicknameModified() {
    // given 변경하려는 닉네임으로
    User user = userRepository.save(new User("tester", "tester@example.com"));
    String targetNickname = "TESTER";
    Long userId = user.getId();

    UpdateNicknameRequest request = new UpdateNicknameRequest(targetNickname);

    // when updateNickname를 호출하면
    UpdateNicknameResponse response = userService.updateNickname(userId, request);

    // then 변경된 유저의 닉네임을 반환한다
    assertThat(response.nickname()).isEqualTo(targetNickname);

    User updateUser = userRepository.findById(userId).orElseThrow();
    assertThat(updateUser.getNickname()).isEqualTo(targetNickname);
  }

  @Test
  @DisplayName("변경하려고 하는 닉네임의 사용자가 존재하지 않으면 에러를 반환한다")
  void updateNickname_withNotExistUser_throwsError() {
    // given 존재하지 않는 유저의 닉네임
    String targetNickname = "TESTER";
    Long invalidUserId = 1L;

    UpdateNicknameRequest request = new UpdateNicknameRequest(targetNickname);

    // when 닉네임을 수정하려고 하면
    // then 에러를 반환한다
    Assertions.assertThatThrownBy(() -> userService.updateNickname(invalidUserId, request))
        .isInstanceOf(UserNotFoundException.class)
        .hasMessageContaining("유효하지 않은 사용자입니다");
  }
}
