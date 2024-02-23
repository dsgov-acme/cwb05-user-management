package io.nuvalence.user.management.api.service.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

import io.nuvalence.events.brokerclient.config.PublisherProperties;
import io.nuvalence.events.event.RoleReportingEvent;
import io.nuvalence.events.event.service.EventGateway;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

import java.time.OffsetDateTime;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class ApplicationRoleConfigTest {

    private static final String TOPIC_KEY = "APPLICATION_ROLE_REPORTING";

    @Mock private EventGateway mockEventGateway;
    @Mock private PublisherProperties mockPublisherProperties;
    private ApplicationRoleConfig applicationRoleConfig;

    @BeforeEach
    void setUp() {
        openMocks(this);
        applicationRoleConfig =
                new ApplicationRoleConfig(mockEventGateway, mockPublisherProperties);
    }

    @Test
    @ExtendWith(OutputCaptureExtension.class)
    void testPublishRoles_NotConfiguredTopic(CapturedOutput output) {

        when(mockPublisherProperties.getFullyQualifiedTopicName(TOPIC_KEY))
                .thenReturn(Optional.empty());

        // test
        applicationRoleConfig.publishRoles();

        verify(mockEventGateway, never()).publishEvent(any(), anyString());

        assertTrue(
                output.getOut()
                        .lines()
                        .anyMatch(
                                line ->
                                        line.contains("ERROR")
                                                && line.contains(
                                                        "Roles reporting topic not configured,"
                                                                + " topic name: "
                                                                + TOPIC_KEY)));
    }

    @Test
    void testPublishRoles_Success() {

        String topicName = "projects/dsgov-demo/topics/" + TOPIC_KEY;
        when(mockPublisherProperties.getFullyQualifiedTopicName(TOPIC_KEY))
                .thenReturn(Optional.of(topicName));

        final OffsetDateTime before = OffsetDateTime.now();

        ArgumentCaptor<RoleReportingEvent> eventCaptor =
                ArgumentCaptor.forClass(RoleReportingEvent.class);
        ArgumentCaptor<String> topicCaptor = ArgumentCaptor.forClass(String.class);

        // test
        applicationRoleConfig.publishRoles();

        verify(mockEventGateway).publishEvent(eventCaptor.capture(), topicCaptor.capture());

        assertEquals(topicName, topicCaptor.getValue());
        var capturedEvent = eventCaptor.getValue();
        assertNotNull(capturedEvent);

        assertEquals(10, capturedEvent.getRoles().size());

        assertEquals("user-management", capturedEvent.getMetadata().getOriginatorId());
        assertEquals("RoleReportingEvent", capturedEvent.getMetadata().getType());
        assertTrue(StringUtils.isNotBlank(capturedEvent.getMetadata().getCorrelationId()));

        assertEquals("um:basic", capturedEvent.getRoles().get(0).getApplicationRole());
        assertEquals("um:reader", capturedEvent.getRoles().get(1).getApplicationRole());

        assertEquals("um:admin", capturedEvent.getRoles().get(2).getApplicationRole());
        assertEquals(4, capturedEvent.getRoles().get(2).getCapabilities().size());
        assertEquals("admin-console", capturedEvent.getRoles().get(2).getCapabilities().get(0));
        assertEquals(
                "public-user-management", capturedEvent.getRoles().get(2).getCapabilities().get(1));
        assertEquals(
                "agency-user-management", capturedEvent.getRoles().get(2).getCapabilities().get(2));
        assertEquals(
                "user-role-management", capturedEvent.getRoles().get(2).getCapabilities().get(3));

        assertEquals("um:application-client", capturedEvent.getRoles().get(3).getApplicationRole());
        assertEquals("um:identity-client", capturedEvent.getRoles().get(4).getApplicationRole());
        assertEquals(
                "um:agency-profile-admin", capturedEvent.getRoles().get(5).getApplicationRole());
        assertEquals("um:individual-admin", capturedEvent.getRoles().get(6).getApplicationRole());
        assertEquals("um:individual-user", capturedEvent.getRoles().get(7).getApplicationRole());
        assertEquals("um:employer-admin", capturedEvent.getRoles().get(8).getApplicationRole());
        assertEquals("um:employer-user", capturedEvent.getRoles().get(9).getApplicationRole());

        final OffsetDateTime after = OffsetDateTime.now();
        var metadataTime = capturedEvent.getMetadata().getTimestamp();

        assertTrue(
                (metadataTime.isAfter(before) || metadataTime.isEqual(before))
                        && (metadataTime.isBefore(after) || metadataTime.isEqual(after)));
    }
}
