package io.reading_tracker.auth;

import io.reading_tracker.domain.user.User;
import io.reading_tracker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PrincipalDetailsService implements UserDetailsService {

  private final UserRepository userRepository;

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    User user =
        userRepository
            .findByLoginId(username) // username: 로그인 창에 입력하는 아이디
            .orElseThrow(() -> new UsernameNotFoundException("해당 유저를 찾을 수 없습니다."));

    return new PrincipalDetails(user);
  }
}
