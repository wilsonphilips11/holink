package com.holink.repository;

import com.holink.entity.Link;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LinkRepository extends JpaRepository<Link, UUID> {

    @Query("SELECT l FROM Link l WHERE l.profile.id = :profileId AND l.deletedAt IS NULL ORDER BY l.position ASC")
    List<Link> findActiveByProfileId(@Param("profileId") UUID profileId);

    @Query("SELECT l FROM Link l WHERE l.profile.id = :profileId AND l.deletedAt IS NULL AND l.active = true ORDER BY l.position ASC")
    List<Link> findPublicByProfileId(@Param("profileId") UUID profileId);

    @Query("SELECT l FROM Link l JOIN FETCH l.profile WHERE l.id = :id AND l.deletedAt IS NULL")
    Optional<Link> findActiveById(@Param("id") UUID id);

    @Query("SELECT COALESCE(MAX(l.position), -1) FROM Link l WHERE l.profile.id = :profileId AND l.deletedAt IS NULL")
    int findMaxPosition(@Param("profileId") UUID profileId);

    @Query("SELECT l FROM Link l WHERE l.id IN :ids AND l.profile.id = :profileId AND l.deletedAt IS NULL")
    List<Link> findByIdsAndProfileId(@Param("ids") List<UUID> ids, @Param("profileId") UUID profileId);
}
