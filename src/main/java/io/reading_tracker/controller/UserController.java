package io.reading_tracker.controller;

import io.reading_tracker.auth.PrincipalDetails;
import io.reading_tracker.request.UpdateNicknameRequest;
import io.reading_tracker.response.UpdateNicknameResponse;
import io.reading_tracker.response.UserResponse;
import io.reading_tracker.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

  private final UserService userService;

  @GetMapping("/me")
  public ResponseEntity<UserResponse> getMyProfile(
      @AuthenticationPrincipal PrincipalDetails principalDetails) {
    UserResponse user = userService.getUserById(principalDetails.getUserId());
    return ResponseEntity.ok(user);
  }

  @PatchMapping("/me/nickname")
  public ResponseEntity<UpdateNicknameResponse> updateNickname(
      @AuthenticationPrincipal PrincipalDetails principalDetails,
      @RequestBody @Valid UpdateNicknameRequest request) {

    UpdateNicknameResponse updatedUser =
        userService.updateNickname(principalDetails.getUserId(), request);
    return ResponseEntity.ok(updatedUser);
  }
}
