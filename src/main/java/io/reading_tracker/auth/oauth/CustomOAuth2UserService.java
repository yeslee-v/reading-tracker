package io.reading_tracker.auth.oauth;

import io.reading_tracker.auth.PrincipalDetails;
import io.reading_tracker.domain.user.User;
import io.reading_tracker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

  private final UserRepository userRepository;

  @Override
  public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

    OAuth2User oAuth2User = super.loadUser(userRequest);

    String registrationId = userRequest.getClientRegistration().getRegistrationId();
    log.info("OAuth2 로그인: provider={}", registrationId);

    OAuth2UserInfo userInfo = getOAuth2UserInfo(registrationId, oAuth2User);

    User user = saveOrUpdateUser(userInfo);

    return new PrincipalDetails(user, oAuth2User.getAttributes());
  }

  private User saveOrUpdateUser(OAuth2UserInfo userInfo) {
    User user = userRepository.findByProviderAndProviderId(
        userInfo.getProvider(),
        userInfo.getProviderId()
    ).map(existingUser -> {
      existingUser.updateProfile(userInfo.getName(),  userInfo.getEmail());
      log.info("기존 유저 정보 업데이트: user={}", existingUser.getEmail());
      return existingUser;
    }).orElseGet(() -> {
      User newUser = new User(
          userInfo.getName(),
          userInfo.getEmail(),
          userInfo.getProvider(),
          userInfo.getProviderId()
      );
      log.info("새 유저 생성: {}", newUser.getEmail());
      return newUser;
    });

    return userRepository.save(user);
  }

  private static OAuth2UserInfo getOAuth2UserInfo(String registrationId, OAuth2User oAuth2User) {
    if ("naver".equals(registrationId)) {
      return new NaverUserInfo(oAuth2User.getAttributes());
    }

    throw new OAuth2AuthenticationException("지원하지 않는 OAuth 입니다: " + registrationId);
  }
}
