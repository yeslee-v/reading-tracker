package io.reading_tracker.service;

import io.reading_tracker.repository.UserRepository;
import jakarta.transaction.Transactional;
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
    // when 사용자를 조회하면
    // then 사용자 정보가 반환된다
  }

  @Test
  @DisplayName("유효하지 않은 아이디로 사용자를 조회하면 에러를 반환한다")
  void getUserById_withInvalidUserId_throwsError() {
    // given 존재하지 않은 아이디
    // when 사용자를 조회하면
    // then 예외를 반환한다
  }

  @Test
  @DisplayName("변경하려는 닉네임이 주어지면 유저의 닉네임을 수정한다")
  void updateNickname_withValidNickname_returnNicknameModified() {
    // given 변경하려는 닉네임으로
    // when updateNickname를 호출하면
    // then 변경된 유저의 닉네임을 반환한다
  }

  @Test
  @DisplayName("변경하려고 하는 닉네임의 사용자가 존재하지 않으면 에러를 반환한다")
  void updateNickname_withNotExistUser_throwsError() {
    // given 존재하지 않는 유저의 닉네임
    // when 닉네임을 수정하려고 하면
    // then 에러를 반환한다
  }
}
