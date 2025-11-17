package io.reading_tracker.auth.oauth;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.reading_tracker.auth.PrincipalDetails;
import io.reading_tracker.domain.user.Auth;
import io.reading_tracker.domain.user.User;
import io.reading_tracker.repository.AuthRepository;
import io.reading_tracker.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.TimeUnit;
import org.assertj.core.data.Offset;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2AccessToken.TokenType;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@Testcontainers
class OAuth2LoginSuccessHandlerTest {

  @Container
  private static final GenericContainer<?> REDIS_CONTAINER =
      new GenericContainer<>(DockerImageName.parse("redis:latest")).withExposedPorts(6379);

  @MockitoBean OAuth2AuthorizedClientService authorizedClientService;

  @Autowired private OAuth2LoginSuccessHandler successHandler;
  @Autowired private AuthRepository authRepository;
  @Autowired private UserRepository userRepository;
  @Autowired private RedisTemplate<String, String> redisTemplate;
  @Autowired private PasswordEncoder passwordEncoder;

  @DynamicPropertySource
  private static void setRedisProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.data.redis.host", REDIS_CONTAINER::getHost);
    registry.add("spring.data.redis.port", () -> REDIS_CONTAINER.getFirstMappedPort().toString());
  }

  @AfterEach
  void tearDown() {
    redisTemplate.getRequiredConnectionFactory().getConnection().serverCommands().flushDb();
  }

  @Test
  @DisplayName("로그인을 성공하면 Access token은 redis에 Refresh token은 db에 암호화하여 저장한다.")
  void onAuthenticationSuccess_saveAccessTokenInRedisAndRefreshTokenInDb() throws IOException {
    // given refresh token이 null 유저가 로그인을 하면
    User testUser = new User("testUser", "testUser@mail.com");
    userRepository.save(testUser);

    Auth testAuth = new Auth(testUser, "testProvider", "testProviderId", null, null);
    authRepository.save(testAuth);

    // request, response mocking
    HttpServletRequest mockRequest = mock(HttpServletRequest.class);
    HttpServletResponse mockResponse = mock(HttpServletResponse.class);

    Authentication mockAuthentication = createMockAuthentication(testUser.getId());

    Instant now = Instant.now();
    Instant accessTokenExpiresAt = now.plus(Duration.ofHours(1));
    Instant refreshTokenExpiresAt = now.plus(Duration.ofDays(14));

    String newAccessToken = "new-access-token-value";
    String newRefreshToken = "new-refresh-token-value";

    OAuth2AccessToken mockAccessToken =
        new OAuth2AccessToken(TokenType.BEARER, newAccessToken, now, accessTokenExpiresAt);
    OAuth2RefreshToken mockRefreshToken =
        new OAuth2RefreshToken(newRefreshToken, now, refreshTokenExpiresAt);

    OAuth2AuthorizedClient mockClient = mock(OAuth2AuthorizedClient.class);
    when(mockClient.getAccessToken()).thenReturn(mockAccessToken);
    when(mockClient.getRefreshToken()).thenReturn(mockRefreshToken);

    when(authorizedClientService.loadAuthorizedClient(any(), any())).thenReturn(mockClient);

    // when onAuthenticationSuccess 인증에 성공하면
    successHandler.onAuthenticationSuccess(mockRequest, mockResponse, mockAuthentication);

    // then Access token은 redis에, Refresh token은 db에 암호화하여 저장한다.
    String expectedRedisKey = "at:" + testUser.getId();
    String savedAccessToken = redisTemplate.opsForValue().get(expectedRedisKey);

    assertThat(savedAccessToken).isEqualTo(newAccessToken);

    Long ttl = redisTemplate.getExpire(expectedRedisKey, TimeUnit.SECONDS);
    assertThat(ttl).isNotNull();
    assertThat(ttl).isCloseTo(Duration.ofHours(1).toSeconds(), Offset.offset(5L));

    Auth updatedAuth = authRepository.findById(testAuth.getId()).get();

    assertThat(updatedAuth.getRefreshToken()).isNotNull();
    assertThat(updatedAuth.getExpiredAt()).isEqualTo(refreshTokenExpiresAt);

    assertThat(passwordEncoder.matches(newRefreshToken, updatedAuth.getRefreshToken())).isTrue();
  }

  private Authentication createMockAuthentication(Long userId) {
    PrincipalDetails mockPrincipal = mock(PrincipalDetails.class);

    User mockUser = mock(User.class);
    when(mockUser.getId()).thenReturn(userId);
    when(mockPrincipal.getUser()).thenReturn(mockUser);

    Authentication mockAuthentication = mock(Authentication.class);
    when(mockAuthentication.getPrincipal()).thenReturn(mockPrincipal);

    OAuth2AuthenticationToken mockOAuthToken = mock(OAuth2AuthenticationToken.class);
    when(mockOAuthToken.getPrincipal()).thenReturn(mockPrincipal);
    when(mockOAuthToken.getAuthorizedClientRegistrationId()).thenReturn("naver");

    when(mockAuthentication.getName()).thenReturn("test-user-name");

    return mockOAuthToken;
  }
}
