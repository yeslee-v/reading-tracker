package io.reading_tracker.controller;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.reading_tracker.auth.PrincipalDetails;
import io.reading_tracker.auth.PrincipalDetailsService;
import io.reading_tracker.auth.jwt.JwtAuthenticationFilter;
import io.reading_tracker.auth.oauth.CustomOAuth2UserService;
import io.reading_tracker.auth.oauth.OAuth2LoginSuccessHandler;
import io.reading_tracker.config.SecurityConfig;
import io.reading_tracker.domain.user.User;
import io.reading_tracker.request.UpdateNicknameRequest;
import io.reading_tracker.response.UpdateNicknameResponse;
import io.reading_tracker.response.UserResponse;
import io.reading_tracker.service.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.web.cors.CorsConfigurationSource;

@WebMvcTest(UserController.class)
@Import(SecurityConfig.class)
@ActiveProfiles("test")
class UserControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private UserService userService;

  @MockitoBean private PrincipalDetailsService principalDetailsService;

  @MockitoBean private CorsConfigurationSource corsConfigurationSource;

  @MockitoBean private CustomOAuth2UserService customOAuth2UserService;

  @MockitoBean private OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;

  @Autowired private ObjectMapper objectMapper;

  @Test
  @DisplayName("GET /api/users/me: 사용자 정보를 불러오는데 성공하면 200 OK를 반환한다")
  void getMyProfile_return200OK() throws Exception {
    // given 사용자 인증 값으로
    User fakeUser = new User("tester", "test@email.com"); // id: null
    ReflectionTestUtils.setField(fakeUser, "id", 1L);

    UserDetails fakePrincipal = new PrincipalDetails(fakeUser);

    UserResponse fakeResponse = new UserResponse(1L, "tester", "test@email.com");

    given(userService.getUserById(eq(1L))).willReturn(fakeResponse);

    // when getMyProfile를 호출하면
    ResultActions result =
        mockMvc.perform(
            get("/api/users/me").contentType(MediaType.APPLICATION_JSON).with(user(fakePrincipal)));

    // then 200 OK를 반환한다
    result.andExpect(status().isOk());

    result.andExpect(jsonPath("$.id").value(1L));
    result.andExpect(jsonPath("$.nickname").value("tester"));
    result.andExpect(jsonPath("$.email").value("test@email.com"));
  }

  @Test
  @DisplayName("GET /api/users/me: 로그인에 실패하면 401 Unauthorized를 반환한다")
  void getMyProfile_withInvalidUser_return401Unauthorized() throws Exception {
    // given 미로그인 유저가

    // when getMyProfile을 호출하면
    ResultActions result =
        mockMvc.perform(get("/api/users/me").contentType(MediaType.APPLICATION_JSON));

    // then 401 Unauthorized를 반환한다
    result.andExpect(status().isUnauthorized());
    result.andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
  }

  @Test
  @DisplayName("GET /api/users/me: 사용자 정보를 불러오는 중 서버가 터지면 500 Internal Server Error를 반환한다")
  void getMyProfile_return500InternalServerError() throws Exception {
    // given getMyProfile을 호출하는데
    User fakeUser = new User("tester", "test@email.com");
    ReflectionTestUtils.setField(fakeUser, "id", 1L);

    UserDetails fakePrincipal = new PrincipalDetails(fakeUser);

    given(userService.getUserById(eq(1L)))
        .willThrow(new RuntimeException("fake 서버가 터졌습니다(db 에러 등"));

    // when 서버가 터지면
    ResultActions result =
        mockMvc.perform(
            get("/api/users/me").contentType(MediaType.APPLICATION_JSON).with(user(fakePrincipal)));

    // then 500 Internal Server Error를 반환한다
    result.andExpect(status().isInternalServerError());
    result.andExpect(jsonPath("$.code").value("INTERNAL_SERVER_ERROR"));
  }

  @Test
  @DisplayName("PATCH /api/users/me/nickname: 사용자 닉네임을 성공적으로 수정하면 200 OK를 반환한다")
  void updateNickname_withNickname_return200OK() throws Exception {
    // given 변경할 닉네임(최대 20자)으로
    User fakeUser = new User("tester", "test@email.com");
    ReflectionTestUtils.setField(fakeUser, "id", 1L);

    UserDetails fakePrincipal = new PrincipalDetails(fakeUser);
    String fakeTargetNickname = "이것은_변경할_20자짜리_닉네임이다.";

    UpdateNicknameRequest fakeRequest = new UpdateNicknameRequest(fakeTargetNickname);
    UpdateNicknameResponse fakeResponse =
        new UpdateNicknameResponse(fakeUser.getId(), fakeTargetNickname);

    given(userService.updateNickname(eq(fakeUser.getId()), eq(fakeRequest)))
        .willReturn(fakeResponse);

    // when updateNickname을 호출하면
    ResultActions result =
        mockMvc.perform(
            patch("/api/users/me/nickname")
                .contentType(MediaType.APPLICATION_JSON)
                .with(user(fakePrincipal))
                .content(objectMapper.writeValueAsString(fakeRequest)));

    // then 200 OK를 반환한다
    result.andExpect(status().isOk());

    result.andExpect(jsonPath("$.nickname").value(fakeTargetNickname));
  }

  @Test
  @DisplayName("PATCH /api/users/me/nickname: 로그인에 실패하면 401 Unauthorized를 반환한다")
  void updateNickname_withInvalidUser_return401Unauthorized() throws Exception {
    // given 미로그인 유저가
    // when updateNickname을 호출하면
    ResultActions result =
        mockMvc.perform(patch("/api/users/me/nickname").contentType(MediaType.APPLICATION_JSON));

    // then 401 Unauthorized를 반환한다
    result.andExpect(status().isUnauthorized());
    result.andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
  }

  @Test
  @DisplayName("PATCH /api/users/me/nickname: 변경할 닉네임이 비어있다면 400 Bad Request를 반환한다")
  void updateNickname_withBlankNickname_return400BadRequest() throws Exception {
    // given 빈 닉네임 값으로
    User fakeUser = new User("tester", "test@email.com");
    ReflectionTestUtils.setField(fakeUser, "id", 1L);

    UserDetails fakePrincipal = new PrincipalDetails(fakeUser);
    String fakeTargetNickname = "";

    UpdateNicknameRequest fakeRequest = new UpdateNicknameRequest(fakeTargetNickname);

    // when updateNickname을 호출하면
    ResultActions result =
        mockMvc.perform(
            patch("/api/users/me/nickname")
                .contentType(MediaType.APPLICATION_JSON)
                .with(user(fakePrincipal))
                .content(objectMapper.writeValueAsString(fakeRequest)));

    // then 400 Bad Request를 반환한다
    result.andExpect(status().isBadRequest());
    result.andExpect(jsonPath("$.code").value("BAD_REQUEST"));
  }

  @Test
  @DisplayName("PATCH /api/users/me/nickname: 변경하려고 하는 닉네임이 20자를 초과하면 400 Bad Request를 반환한다")
  void updateNickname_withNicknameExceedingMaxLength_return400BadRequest() throws Exception {
    // given 50자를 초과하는 닉네임으로
    User fakeUser = new User("tester", "test@email.com");
    ReflectionTestUtils.setField(fakeUser, "id", 1L);

    UserDetails fakePrincipal = new PrincipalDetails(fakeUser);
    String fakeTargetNickname = "이것은_변경할_21자짜리_닉네임이다..";

    UpdateNicknameRequest fakeRequest = new UpdateNicknameRequest(fakeTargetNickname);

    // when updateNickname을 호출하면
    ResultActions result =
        mockMvc.perform(
            patch("/api/users/me/nickname")
                .contentType(MediaType.APPLICATION_JSON)
                .with(user(fakePrincipal))
                .content(objectMapper.writeValueAsString(fakeRequest)));

    // then 400 Bad Request를 반환한다
    result.andExpect(status().isBadRequest());
    result.andExpect(jsonPath("$.code").value("BAD_REQUEST"));
  }

  @Test
  @DisplayName(
      "PATCH /api/users/me/nickname: 사용자의 닉네임을 변경하는 도중 서버가 터지면 500 Internal Server Error를 반환한다")
  void updateNickname_return500InternalServerError() throws Exception {
    // given updateNickname을 호출하는데
    User fakeUser = new User("tester", "test@email.com");
    ReflectionTestUtils.setField(fakeUser, "id", 1L);

    UserDetails fakePrincipal = new PrincipalDetails(fakeUser);
    String fakeTargetNickname = "TESTER";

    UpdateNicknameRequest fakeRequest = new UpdateNicknameRequest(fakeTargetNickname);
    given(userService.updateNickname(eq(fakeUser.getId()), eq(fakeRequest)))
        .willThrow(new RuntimeException("fake 서버가 터졌습니다(db 에러 등"));

    // when 서버가 터지면
    ResultActions result =
        mockMvc.perform(
            patch("/api/users/me/nickname")
                .contentType(MediaType.APPLICATION_JSON)
                .with(user(fakePrincipal))
                .content(objectMapper.writeValueAsString(fakeRequest)));

    // then 500 Internal Server Error를 반환한다
    result.andExpect(status().isInternalServerError());
    result.andExpect(jsonPath("$.code").value("INTERNAL_SERVER_ERROR"));
  }

  @TestConfiguration
  static class MockFilterConfig {
    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
      return new JwtAuthenticationFilter(null, null) {
        @Override
        protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
          filterChain.doFilter(request, response);
        }
      };
    }
  }
}
