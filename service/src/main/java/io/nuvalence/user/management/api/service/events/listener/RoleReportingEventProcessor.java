package io.nuvalence.user.management.api.service.events.listener;

import io.nuvalence.events.event.RoleReportingEvent;
import io.nuvalence.events.exception.EventProcessingException;
import io.nuvalence.events.subscriber.EventProcessor;
import io.nuvalence.user.management.api.service.service.ApplicationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * This class is responsible for processing RoleReporting events.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class RoleReportingEventProcessor implements EventProcessor<RoleReportingEvent> {

    private final ApplicationService applicationService;

    @Override
    public Class<RoleReportingEvent> getEventClass() {
        return RoleReportingEvent.class;
    }

    @Override
    public void execute(RoleReportingEvent event) throws EventProcessingException {
        try {
            log.info(
                    RoleReportingEvent.class.getSimpleName() + " received from " + event.getName());
            applicationService.setApplicationRoles(event.getName(), event.getRoles());
        } catch (Exception e) {
            throw new EventProcessingException(e);
        }
    }
}
