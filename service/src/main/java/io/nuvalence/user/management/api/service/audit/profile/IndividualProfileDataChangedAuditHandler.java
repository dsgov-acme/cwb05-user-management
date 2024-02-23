package io.nuvalence.user.management.api.service.audit.profile;

import io.nuvalence.user.management.api.service.audit.AuditHandler;
import io.nuvalence.user.management.api.service.audit.util.AuditMapManagementUtility;
import io.nuvalence.user.management.api.service.entity.profile.IndividualProfile;
import io.nuvalence.user.management.api.service.events.model.AuditEventRequestObjectDto;
import io.nuvalence.user.management.api.service.models.auditevents.AuditActivityType;
import io.nuvalence.user.management.api.service.models.auditevents.AuditEventBusinessObject;
import io.nuvalence.user.management.api.service.service.AuditEventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
public class IndividualProfileDataChangedAuditHandler implements AuditHandler<IndividualProfile> {

    private final Map<String, String> before = new HashMap<>();
    private final Map<String, String> after = new HashMap<>();

    private UUID profileId;

    private final AuditEventService individualAuditEventService;

    @Override
    public void handlePreUpdateState(IndividualProfile subject) {
        profileId = subject.getId();
        before.putAll(convertToMap(subject));
    }

    @Override
    public void handlePostUpdateState(IndividualProfile subject) {
        after.putAll(convertToMap(subject));
    }

    @Override
    public void publishAuditEvent(String originatorId) {
        AuditMapManagementUtility.removeCommonItems(before, after);

        try {
            String eventSummary;
            if (!before.isEmpty() || !after.isEmpty()) {
                eventSummary = String.format("Data for individual profile %s changed.", profileId);
            } else {
                return;
            }

            final AuditEventRequestObjectDto auditEvent =
                    AuditEventRequestObjectDto.builder()
                            .originatorId(originatorId)
                            .userId(originatorId)
                            .summary(eventSummary)
                            .businessObjectId(profileId)
                            .businessObjectType(AuditEventBusinessObject.INDIVIDUAL)
                            .data(
                                    before,
                                    after,
                                    null,
                                    AuditActivityType.INDIVIDUAL_PROFILE_DATA_CHANGED.getValue())
                            .build();

            individualAuditEventService.sendAuditEvent(auditEvent);

        } catch (Exception e) {
            String errorMessage =
                    "An unexpected exception occurred when recording audit event for individual"
                            + " profile data change for profile "
                            + profileId;
            log.error(errorMessage, e);
        }
    }

    private Map<String, String> convertToMap(IndividualProfile individual) {
        Map<String, String> map = new HashMap<>();

        if (individual == null) {
            return map;
        }

        map.put("ssn", individual.getSsn());

        map.putAll(
                AuditMapManagementUtility.convertAddressToMap(
                        "mailingAddress.", individual.getMailingAddress()));
        map.putAll(
                AuditMapManagementUtility.convertAddressToMap(
                        "primaryAddress.", individual.getPrimaryAddress()));

        return map;
    }
}
