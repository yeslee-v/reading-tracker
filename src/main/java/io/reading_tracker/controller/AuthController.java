package io.reading_tracker.controller;

import io.reading_tracker.auth.PrincipalDetails;
import io.reading_tracker.response.UserInfoResponse;
import java.util.Map;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

  @GetMapping("/user")
  public UserInfoResponse getCurrentUser(@AuthenticationPrincipal OAuth2User oAuth2User) {
    if (oAuth2User == null) {
      return UserInfoResponse.unauthenticated();
    }

    PrincipalDetails principalDetails = (PrincipalDetails) oAuth2User;

    /// JWT 로그인
    if (principalDetails.getAttributes() == null
        || principalDetails.getAttributes().get("response") == null) {
      String userId = String.valueOf(principalDetails.getUserId());
      String email = principalDetails.getEmail();
      String username = principalDetails.getUsername();

      return UserInfoResponse.of(userId, email, username, "local");
    }

    Map<String, Object> attributes = oAuth2User.getAttributes();

    /// unchecked cast 방어
    Object responseObj = attributes.get("response");

    if (!(responseObj instanceof Map<?, ?>)) {
      return UserInfoResponse.unauthenticated();
    }

    @SuppressWarnings("unchecked")
    Map<String, Object> naverResponse = (Map<String, Object>) responseObj;

    String id = (String) naverResponse.get("id");
    String email = (String) naverResponse.get("email");
    String nickname = (String) naverResponse.get("nickname");

    return UserInfoResponse.of(id, email, nickname, "naver");
  }
}
