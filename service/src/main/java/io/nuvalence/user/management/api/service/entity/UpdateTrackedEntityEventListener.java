package io.nuvalence.user.management.api.service.entity;

import io.nuvalence.user.management.api.service.util.RequestContextTimestamp;
import io.nuvalence.user.management.api.service.util.SpringApplicationContext;
import io.nuvalence.user.management.api.service.util.UserUtility;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;

import java.time.OffsetDateTime;

/**
 * Automatically sets created and last updated tracking data.
 */
@Slf4j
public class UpdateTrackedEntityEventListener {

    /**
     * Sets created and last updated tracking data for the given entity.
     *
     * @param entity entity to update
     */
    @PrePersist
    public void preUpdateTrackedEntityPersist(final UpdateTrackedEntity entity) {
        UserUtility.getCurrentApplicationUserId()
                .ifPresent(
                        userId -> {
                            entity.setCreatedBy(userId);
                            entity.setLastUpdatedBy(userId);
                        });

        OffsetDateTime currentTimestamp = getCommonTimestamp(entity);

        entity.setCreatedTimestamp(currentTimestamp);
        entity.setLastUpdatedTimestamp(currentTimestamp);
    }

    /**
     * Sets last updated tracking data for the given entity.
     *
     * @param entity entity to update
     */
    @PreUpdate
    public void preUpdateTrackedEntityUpdate(final UpdateTrackedEntity entity) {
        UserUtility.getCurrentApplicationUserId().ifPresent(entity::setLastUpdatedBy);
        OffsetDateTime currentTimestamp = getCommonTimestamp(entity);
        entity.setLastUpdatedTimestamp(currentTimestamp);
    }

    private OffsetDateTime getCommonTimestamp(UpdateTrackedEntity entity) {
        OffsetDateTime currentTimestamp;

        try {
            currentTimestamp =
                    SpringApplicationContext.getBeanByClass(RequestContextTimestamp.class)
                            .getCurrentTimestamp();
        } catch (BeansException e) {
            log.warn(
                    "Unable to get current timestamp from request context when saving entity ({})."
                            + " Using current system time. {}",
                    entity.getClass().getSimpleName(),
                    e.getMessage());
            currentTimestamp = OffsetDateTime.now();
        }
        return currentTimestamp;
    }
}
