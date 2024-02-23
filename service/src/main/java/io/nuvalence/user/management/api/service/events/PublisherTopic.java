package io.nuvalence.user.management.api.service.events;

import lombok.Getter;

/**
 * Enumerates the topics that can be published to.
 */
@Getter
public enum PublisherTopic {
    APPLICATION_ROLE_REPORTING,
    AUDIT_EVENTS_RECORDING
}
