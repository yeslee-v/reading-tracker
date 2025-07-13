package io.reading_tracker.user;

import java.util.Date;
import java.util.Optional;

public class UserServiceImpl implements UserService{
  private final UserRepository userRepository;

  public UserServiceImpl(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Override
  public void join(String nickname, String email) {
    User user = userRepository.findByEmail(email).orElseThrow(() ->
        new IllegalArgumentException("User already exists!"));

    User newUser = new User(nickname, email);

    userRepository.save(newUser);
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
