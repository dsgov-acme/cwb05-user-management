package io.nuvalence.user.management.api.service.repository;

import io.nuvalence.user.management.api.service.entity.UserEntity;
import io.nuvalence.user.management.api.service.entity.profile.IndividualProfile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository for User.
 */
@Repository
public interface UserRepository
        extends JpaRepository<UserEntity, UUID>, JpaSpecificationExecutor<UserEntity> {

    @EntityGraph(value = "user.complete")
    @Query("select e from UserEntity e where e.id = :id")
    Optional<UserEntity> findByIdLoaded(@Param("id") UUID id);

    @Query("SELECT u FROM UserEntity u")
    Page<UserEntity> findAll(Pageable pageable);

    @Query("SELECT p FROM PublicUser u JOIN u.profile p WHERE u.id = :userId")
    Optional<IndividualProfile> findProfileByUserId(@Param("userId") UUID userId);
}
