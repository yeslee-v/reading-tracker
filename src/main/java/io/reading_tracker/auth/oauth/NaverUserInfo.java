package io.reading_tracker.auth.oauth;

import java.util.Map;

public class NaverUserInfo implements OAuth2UserInfo {

  private final Map<String, Object> attributes;

  public NaverUserInfo(Map<String, Object> attributes) {
    Object response = attributes.get("response");

    if (response == null) {
      throw new IllegalArgumentException("네이버 OAuth2 응답에 response 객체가 없습니다");
    }

    if (!(response instanceof Map)) {
      throw new IllegalArgumentException("네이버 OAuth2 응답의 response가 Map 타입이 아닙니다");
    }

    this.attributes = (Map<String, Object>) attributes.get("response");

    if (this.attributes.get("id") == null) {
      throw new IllegalArgumentException("네이버 OAuth2 응답에 id가 없습니다");
    }

    if (this.attributes.get("nickname") == null) {
      throw new IllegalArgumentException("네이버 OAuth2 응답에 nickname이 없습니다");
    }

    if (this.attributes.get("email") == null) {
      throw new IllegalArgumentException("네이버 OAuth2 응답에 email이 없습니다");
    }
  }

  @Override
  public String getProviderId() {
    return attributes.get("id").toString();
  }

  @Override
  public String getProvider() {
    return "naver";
  }

  @Override
  public String getName() {
    return attributes.get("nickname").toString();
  }

  @Override
  public String getEmail() {
    return attributes.get("email").toString();
  }
}
