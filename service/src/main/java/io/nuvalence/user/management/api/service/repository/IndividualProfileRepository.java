package io.nuvalence.user.management.api.service.repository;

import io.nuvalence.user.management.api.service.entity.profile.IndividualProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface IndividualProfileRepository
        extends JpaRepository<IndividualProfile, UUID>,
                JpaSpecificationExecutor<IndividualProfile> {}
