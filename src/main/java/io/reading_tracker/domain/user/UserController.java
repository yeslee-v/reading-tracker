package io.reading_tracker.domain.user;

import io.reading_tracker.exception.UserNotFoundException;
import io.reading_tracker.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

  private final UserService userService;

  @GetMapping("/{userId}")
  public ApiResponse<UserResponse> getUserById(@PathVariable Long userId) {
    User user = userService.getUserById(userId);
    return ApiResponse.success(UserResponse.from(user));
  }

  @GetMapping("/search")
  public ApiResponse<UserResponse> findUserByEmail(@RequestParam String email) {
    return userService
        .findUserByEmail(email)
        .map(UserResponse::from)
        .map(ApiResponse::success)
        .orElseThrow(() -> new UserNotFoundException(email));
  }

  @PatchMapping("/{userId}/nickname")
  public ApiResponse<UserResponse> updateNickname(
      @PathVariable Long userId, @RequestBody UpdateNicknameRequest request) {

    if (request == null || !StringUtils.hasText(request.nickname())) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "닉네임은 비어 있을 수 없습니다.");
    }

    User updatedUser = userService.updateNickname(userId, request.nickname());
    return ApiResponse.success(UserResponse.from(updatedUser));
  }
}
