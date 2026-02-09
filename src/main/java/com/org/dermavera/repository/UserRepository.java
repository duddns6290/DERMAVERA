package com.org.dermavera.repository;

import com.org.dermavera.entity.SocialType;
import com.org.dermavera.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUserId(String userId);

    boolean existsByUserId(String userId);

    Optional<User> findByProviderAndProviderId(
            SocialType provider,
            String providerId
    );
}
