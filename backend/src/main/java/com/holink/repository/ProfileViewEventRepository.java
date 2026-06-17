package com.holink.repository;

import com.holink.entity.ProfileViewEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ProfileViewEventRepository extends JpaRepository<ProfileViewEvent, UUID> {

    long countByProfileId(UUID profileId);
}
