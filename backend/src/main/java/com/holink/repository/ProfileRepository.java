package com.holink.repository;

import com.holink.entity.Profile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ProfileRepository extends JpaRepository<Profile, UUID> {

    Optional<Profile> findByUsername(String username);

    Optional<Profile> findByUserId(UUID userId);

    boolean existsByUsername(String username);
}
