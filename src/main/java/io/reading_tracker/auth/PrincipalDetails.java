package io.reading_tracker.auth;

import io.reading_tracker.domain.user.User;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

@Getter
public class PrincipalDetails implements OAuth2User, UserDetails {

  private final User user;
  private final Map<String, Object> attributes;

  /**
   * 일반 로그인
   *
   * @param user
   */
  public PrincipalDetails(User user) {
    this.user = user;
    this.attributes = null;
  }

  /**
   * 소셜 로그인
   *
   * @param user
   * @param attributes
   */
  public PrincipalDetails(User user, Map<String, Object> attributes) {
    this.user = user;
    this.attributes = attributes;
  }

  @Override
  public Map<String, Object> getAttributes() {
    return attributes;
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return Collections.singleton(new SimpleGrantedAuthority("ROLE_USER"));
  }

  @Override
  public String getPassword() {
    return null;
  }

  @Override
  public String getUsername() {
    return user.getEmail();
  }

  @Override
  public boolean isAccountNonExpired() {
    return true;
  }

  @Override
  public boolean isAccountNonLocked() {
    return true;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return true;
  }

  @Override
  public boolean isEnabled() {
    return true;
  }

  @Override
  public String getName() {
    return user.getNickname();
  }

  public Long getUserId() {
    return user.getId();
  }

  public String getEmail() {
    return user.getEmail();
  }
}
