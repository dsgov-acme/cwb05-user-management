package io.nuvalence.user.management.api.service.entity;

import jakarta.persistence.PrePersist;

import java.time.OffsetDateTime;

/**
 * Automatically sets created at timestamp on users.
 */
public class UserEntityEventListener {
    /**
     * Sets created at timestamp.
     *
     * @param user userbeing persisted
     */
    @PrePersist
    public void preUpdateTrackedEntityPersist(final UserEntity user) {
        user.setCreatedAt(OffsetDateTime.now());
    }
}
