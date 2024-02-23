package io.nuvalence.user.management.api.service.audit;

import io.nuvalence.user.management.api.service.entity.UpdateTrackedEntity;

/**
 * Implementation of a specific audit event type.
 *
 * @param <S> Type of subject this event audits.
 */
public interface AuditHandler<S extends UpdateTrackedEntity> {
    void handlePreUpdateState(S subject);

    void handlePostUpdateState(S subject);

    void publishAuditEvent(String originatorId);
}
