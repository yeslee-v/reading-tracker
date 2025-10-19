package io.reading_tracker.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserInfoResponse {
  private boolean authenticated;
  private String id;
  private String email;
  private String nickname;
  private String provider;

  public static UserInfoResponse unauthenticated() {
    return new UserInfoResponse(false, null, null, null, null);
  }

  public static UserInfoResponse of(String id, String email, String nickname, String provider) {
    return new UserInfoResponse(true, id, email, nickname, provider);
  }

}
