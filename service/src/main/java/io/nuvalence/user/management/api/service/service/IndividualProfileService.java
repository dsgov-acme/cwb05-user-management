package io.nuvalence.user.management.api.service.service;

import io.nuvalence.user.management.api.service.entity.profile.IndividualProfile;
import io.nuvalence.user.management.api.service.events.model.AuditEventRequestObjectDto;
import io.nuvalence.user.management.api.service.models.IndividualFilters;
import io.nuvalence.user.management.api.service.models.auditevents.AuditActivityType;
import io.nuvalence.user.management.api.service.models.auditevents.AuditEventBusinessObject;
import io.nuvalence.user.management.api.service.models.auditevents.ProfileCreatedAuditEventDto;
import io.nuvalence.user.management.api.service.repository.IndividualProfileRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

/**
 * Service for managing individual profiles.
 */
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class IndividualProfileService {
    private static final String CREATION_AUDIT_EVENT_ERR_MSG =
            "An error has occurred when recording a creation audit event for an";
    private final IndividualProfileRepository repository;
    private final AuditEventService auditEventService;

    public IndividualProfile saveIndividual(final IndividualProfile individual) {
        if (individual.getMailingAddress() != null) {
            individual.getMailingAddress().setIndividualForMailing(individual);
        }

        if (individual.getPrimaryAddress() != null) {
            individual.getPrimaryAddress().setIndividualForAddress(individual);
        }
        return repository.save(individual);
    }

    /**
     * Gets an individual profile by ID.
     *
     * @param id the ID of the individual profile to get
     * @return the individual profile
     */
    public Optional<IndividualProfile> getIndividualById(final UUID id) {
        if (id == null) {
            return Optional.empty();
        }

        return repository.findById(id);
    }

    public Page<IndividualProfile> getIndividualsByFilters(final IndividualFilters filters) {
        return repository.findAll(
                filters.getIndividualProfileSpecification(), filters.getPageRequest());
    }

    /**
     * Posts an audit event for an individual profile being created.
     *
     * @param profile the individual profile that was created
     * @return the ID of the audit event
     */
    public UUID postAuditEventForIndividualCreated(IndividualProfile profile) {

        ProfileCreatedAuditEventDto profileInfo =
                new ProfileCreatedAuditEventDto(profile.getCreatedBy());

        final String summary = "Profile Created.";

        final AuditEventRequestObjectDto auditEvent =
                AuditEventRequestObjectDto.builder()
                        .originatorId(profile.getCreatedBy())
                        .userId(profile.getCreatedBy())
                        .summary(summary)
                        .businessObjectId(profile.getId())
                        .businessObjectType(AuditEventBusinessObject.INDIVIDUAL)
                        .data(profileInfo.toJson(), AuditActivityType.INDIVIDUAL_PROFILE_CREATED)
                        .build();

        return auditEventService.sendAuditEvent(auditEvent);
    }

    public void postAuditEventForIndividualProfileCreated(IndividualProfile profile) {
        try {
            postAuditEventForIndividualCreated(profile);
        } catch (Exception e) {
            String errorMessage =
                    String.format(
                            CREATION_AUDIT_EVENT_ERR_MSG
                                    + " employer profile with user id %s for profile with id %s.",
                            profile.getCreatedBy(),
                            profile.getId());
            log.error(errorMessage, e);
        }
    }
}
