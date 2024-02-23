package io.nuvalence.user.management.api.service.repository;

import io.nuvalence.user.management.api.service.entity.profile.EmployerProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface EmployerProfileRepository
        extends JpaRepository<EmployerProfile, UUID>, JpaSpecificationExecutor<EmployerProfile> {}
