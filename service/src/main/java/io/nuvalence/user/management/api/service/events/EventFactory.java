package io.nuvalence.user.management.api.service.events;

import io.nuvalence.events.event.AuditEvent;
import io.nuvalence.events.event.DirectNotificationEvent;
import io.nuvalence.events.event.Event;
import io.nuvalence.events.event.EventMetadata;
import io.nuvalence.events.event.NotificationEvent;
import io.nuvalence.events.event.RoleReportingEvent;
import io.nuvalence.events.event.dto.AuditEventDataBase;
import io.nuvalence.events.event.dto.BusinessObjectMetadata;
import io.nuvalence.events.event.dto.CommunicationMethod;
import io.nuvalence.events.event.dto.RequestContext;
import io.nuvalence.logging.util.CorrelationIdContext;
import io.nuvalence.user.management.api.service.events.model.ApplicationRoles;
import io.nuvalence.user.management.api.service.models.auditevents.AuditEventBusinessObject;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Factory for creating dsgov events.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class EventFactory {

    private static final String APPLICATION_NAME = "user-management";

    /**
     * Create an AuditEvent.
     *
     * @param appRoles the application roles
     * @return the event
     */
    public static RoleReportingEvent createRoleReportingEvent(ApplicationRoles appRoles) {

        RoleReportingEvent event =
                RoleReportingEvent.builder()
                        .name(appRoles.getName())
                        .roles(appRoles.getRoles())
                        .build();

        event.setMetadata(generateEventMetadata(event.getClass()));

        return event;
    }

    /**
     * Create an AuditEvent.
     *
     * @param userId the user id
     * @param notificationKey the notification key
     * @param notificationParameterMap the notification parameter map
     * @return the event
     */
    public static NotificationEvent createNotificationEvent(
            UUID userId, String notificationKey, Map<String, String> notificationParameterMap) {
        NotificationEvent event =
                NotificationEvent.builder()
                        .userId(userId)
                        .templateKey(notificationKey)
                        .parameters(notificationParameterMap)
                        .build();
        event.setMetadata(generateEventMetadata(NotificationEvent.class));
        return event;
    }

    public static DirectNotificationEvent createDirectNotificationEvent(
            String communicationMethod,
            String destination,
            String notificationKey,
            Map<String, String> notificationParameterMap) {
        DirectNotificationEvent event =
                DirectNotificationEvent.builder()
                        .communicationMethod(
                                CommunicationMethod.fromValueString(communicationMethod))
                        .destination(destination)
                        .templateKey(notificationKey)
                        .parameters(notificationParameterMap)
                        .build();
        event.setMetadata(generateEventMetadata(DirectNotificationEvent.class));
        return event;
    }

    /**
     * Create an AuditEvent.
     *
     * @param data the audit event data
     * @param originatorId the originator id
     * @param userId  the user id
     * @param summary the summary
     * @param businessObjectId the business object id
     * @param businessObjectType the business object type
     * @return the event
     */
    public static AuditEvent createAuditEvent(
            AuditEventDataBase data,
            String originatorId,
            String userId,
            String summary,
            UUID businessObjectId,
            AuditEventBusinessObject businessObjectType) {

        RequestContext requestContext =
                RequestContext.builder()
                        .originatorId(UUID.fromString(originatorId))
                        .userId(UUID.fromString(userId))
                        .traceId(UUID.fromString(CorrelationIdContext.getCorrelationId()))
                        .build();

        BusinessObjectMetadata businessObject =
                BusinessObjectMetadata.builder()
                        .id(businessObjectId)
                        .type(businessObjectType.getValue())
                        .build();

        AuditEvent event =
                AuditEvent.builder()
                        .summary(summary)
                        .eventData(data)
                        .requestContext(requestContext)
                        .businessObject(businessObject)
                        .build();

        event.setMetadata(generateEventMetadata(AuditEvent.class));

        return event;
    }

    private static EventMetadata generateEventMetadata(Class<? extends Event> eventClass) {
        return EventMetadata.builder()
                .id(UUID.randomUUID())
                .type(eventClass.getSimpleName())
                .originatorId(APPLICATION_NAME)
                .timestamp(OffsetDateTime.now())
                .correlationId(CorrelationIdContext.getCorrelationId())
                .build();
    }
}
