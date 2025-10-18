package io.reading_tracker.auth;

import io.reading_tracker.domain.user.UserInfoResponse;
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

    Map<String, Object> attributes = oAuth2User.getAttributes();
    Map<String, Object> naverResponse = (Map<String, Object>) attributes.get("response");

    if (naverResponse == null) {
      return UserInfoResponse.unauthenticated();
    }

    String id = (String) naverResponse.get("id");
    String email = (String) naverResponse.get("email");
    String nickname = (String) naverResponse.get("nickname");

    return UserInfoResponse.of(id, email, nickname, "naver");
  }
}
