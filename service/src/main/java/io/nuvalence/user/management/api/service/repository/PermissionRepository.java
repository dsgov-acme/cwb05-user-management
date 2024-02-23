package io.nuvalence.user.management.api.service.repository;

import io.nuvalence.user.management.api.service.entity.PermissionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for Permission.
 */
@Repository
public interface PermissionRepository extends JpaRepository<PermissionEntity, UUID> {

    @Query("select p from PermissionEntity p where p.active = true")
    List<PermissionEntity> findAllActive();

    @Query("select p from PermissionEntity p where p.name = ?1")
    Optional<PermissionEntity> findByName(String name);

    @Query("select p from PermissionEntity p where p.name in ?1")
    List<PermissionEntity> findAllByName(Collection<String> names);

    @Query("select p from PermissionEntity p where p.applicationRole = ?1")
    Optional<PermissionEntity> findByApplicationRole(String applicationRole);

    @Query("select p from PermissionEntity p where p.applicationRole in ?1")
    List<PermissionEntity> findAllByApplicationRole(Collection<String> roles);
}
