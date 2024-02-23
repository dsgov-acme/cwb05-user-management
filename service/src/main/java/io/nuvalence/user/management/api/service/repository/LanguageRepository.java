package io.nuvalence.user.management.api.service.repository;

import io.nuvalence.user.management.api.service.entity.LanguageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Language Repository.
 */
@Repository
public interface LanguageRepository extends JpaRepository<LanguageEntity, UUID> {

    Optional<LanguageEntity> findByLanguageStandardId(String standardId);
}
