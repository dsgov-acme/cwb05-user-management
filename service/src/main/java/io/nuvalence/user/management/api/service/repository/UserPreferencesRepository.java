package io.nuvalence.user.management.api.service.repository;

import io.nuvalence.user.management.api.service.entity.UserPreferenceEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * User Preferences Repository.
 */
@Repository
public interface UserPreferencesRepository extends JpaRepository<UserPreferenceEntity, UUID> {

    @EntityGraph(value = "user.complete")
    Optional<UserPreferenceEntity> findByUserId(UUID userid);
}
