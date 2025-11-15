package io.reading_tracker.auth.oauth;

import io.reading_tracker.auth.PrincipalDetails;
import io.reading_tracker.domain.user.Auth;
import io.reading_tracker.domain.user.User;
import io.reading_tracker.repository.AuthRepository;
import io.reading_tracker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

  private final AuthRepository authRepository;
  private final UserRepository userRepository;

  private static OAuth2UserInfo getOAuth2UserInfo(String registrationId, OAuth2User oAuth2User) {
    if ("naver".equals(registrationId)) {
      return new NaverUserInfo(oAuth2User.getAttributes());
    }

    throw new OAuth2AuthenticationException("지원하지 않는 OAuth 입니다: " + registrationId);
  }

  @Override
  @Transactional
  public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

    OAuth2User oAuth2User = super.loadUser(userRequest);

    log.debug("access token: {}", userRequest.getAccessToken().getTokenValue());

    String registrationId = userRequest.getClientRegistration().getRegistrationId();

    log.info("OAuth2 로그인: provider={}", registrationId);

    OAuth2UserInfo userInfo = getOAuth2UserInfo(registrationId, oAuth2User);

    User user = saveOrUpdateUser(userInfo);

    return new PrincipalDetails(user, oAuth2User.getAttributes());
  }

  private User saveOrUpdateUser(OAuth2UserInfo userInfo) {
    Auth auth =
        authRepository
            .findByProviderAndProviderId(userInfo.getProvider(), userInfo.getProviderId())
            .map(
                existingAuth -> {
                  // 이미 계정이 존재하는 경우
                  User user = existingAuth.getUser();
                  // naver 인증 결과 값(nickname, email)을 기존 유저 객체에 업데이트
                  user.updateProfile(userInfo.getName(), userInfo.getEmail());
                  userRepository.save(user);
                  log.debug("기존 유저 프로필 업데이트: {}", user.getEmail());

                  return existingAuth;
                })
            .orElseGet(
                () -> {
                  // 계정이 없는 경우: 새 User 객체 생성
                  User newUser = new User(userInfo.getName(), userInfo.getEmail());
                  userRepository.save(newUser);
                  // 새 Auth 객체 생성 및 저장
                  Auth newAuth =
                      new Auth(
                          newUser, userInfo.getProvider(), userInfo.getProviderId(), null, null);
                  authRepository.save(newAuth);

                  log.info("새 유저 및 Auth 생성: {}", newUser.getEmail());

                  return newAuth;
                });

    return auth.getUser();
  }
}
