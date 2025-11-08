package io.reading_tracker.repository;

import io.reading_tracker.domain.user.Auth;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuthRepository extends JpaRepository<Auth, Long> {
  Optional<Auth> findByProviderAndProviderId(String provider, String providerId);
}
