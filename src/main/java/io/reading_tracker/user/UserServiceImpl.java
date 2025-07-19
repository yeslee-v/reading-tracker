package io.reading_tracker.user;

import java.util.Date;
import java.util.Optional;

public class UserServiceImpl implements UserService{
  private final UserRepository userRepository;

  public UserServiceImpl(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Override
  public void join(User user) {
    Optional<User> existUser = userRepository.findByEmail(user.getEmail());

    if (existUser.isPresent()) {
      throw new IllegalArgumentException("User already exists");
    }

    userRepository.save(user);
  }

  @Override
  public Optional<User> findUser(String email) {
    return userRepository.findByEmail(email);
  }

  @Override
  public void updateUser(String nickname, String email) {
    User user = userRepository.findByEmail(email).orElseThrow(() ->
        new IllegalArgumentException("User not found!"));

    user.setNickname(nickname);
    user.setUpdateAt(new Date());
  }
}
