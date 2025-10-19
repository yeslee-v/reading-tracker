package io.reading_tracker.auth.oauth;

public interface OAuth2UserInfo {
  String getProviderId();
  String getProvider();
  String getName();
  String getEmail();
}
