package io.nuvalence.user.management.api.service.repository;

import io.nuvalence.user.management.api.service.entity.RoleEntity;
import io.nuvalence.user.management.api.service.enums.UserType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for Roles.
 */
@Repository
public interface RoleRepository extends JpaRepository<RoleEntity, UUID> {

    @Query("select r from RoleEntity r where r.name = ?1")
    Optional<RoleEntity> findByName(String name);

    @Query("SELECT r FROM RoleEntity r WHERE :userType MEMBER OF r.defaultUserTypes")
    List<RoleEntity> findAllByUserType(@Param("userType") UserType userType);
}
