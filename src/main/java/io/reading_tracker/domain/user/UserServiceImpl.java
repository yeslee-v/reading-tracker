package io.reading_tracker.domain.user;

import io.reading_tracker.exception.UserNotFoundException;
import io.reading_tracker.repository.UserRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

  private final UserRepository userRepository;

  @Override
  public User getUserById(Long userId) {
    return userRepository
        .findById(userId)
        .orElseThrow(() -> new UserNotFoundException(userId));
  }

  @Override
  public Optional<User> findUserByEmail(String email) {
    return userRepository.findByEmail(email);
  }

  @Override
  @Transactional
  public User updateNickname(Long userId, String nickname) {
    User user = getUserById(userId);
    user.changeNickname(nickname);
    return user;
  }
}
