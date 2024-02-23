package io.nuvalence.user.management.api.service.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.nuvalence.user.management.api.service.entity.profile.Address;
import io.nuvalence.user.management.api.service.entity.profile.IndividualProfile;
import io.nuvalence.user.management.api.service.events.model.AuditEventRequestObjectDto;
import io.nuvalence.user.management.api.service.models.IndividualFilters;
import io.nuvalence.user.management.api.service.models.auditevents.AuditActivityType;
import io.nuvalence.user.management.api.service.models.auditevents.AuditEventBusinessObject;
import io.nuvalence.user.management.api.service.models.auditevents.ProfileCreatedAuditEventDto;
import io.nuvalence.user.management.api.service.repository.IndividualProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ExtendWith(MockitoExtension.class)
public class IndividualProfileServiceTest {
    @Mock private IndividualProfileRepository repository;
    @Mock private AuditEventService auditEventService;

    private IndividualProfileService service;

    @BeforeEach
    public void setUp() {
        service = new IndividualProfileService(repository, auditEventService);
    }

    @Test
    void getIndividualById_Success() {
        UUID individualId = UUID.randomUUID();
        IndividualProfile individual = IndividualProfile.builder().id(individualId).build();

        when(repository.findById(any(UUID.class))).thenReturn(Optional.of(individual));

        Optional<IndividualProfile> individualResult = service.getIndividualById(individualId);

        assertTrue(individualResult.isPresent());
        assertEquals(individual, individualResult.get());
        assertEquals(individualId, individualResult.get().getId());
    }

    @Test
    void getIndividualById_Null() {
        Optional<IndividualProfile> individualResult = service.getIndividualById(null);

        assertTrue(individualResult.isEmpty());
    }

    @Test
    void saveIndividual() {
        UUID individualId = UUID.randomUUID();
        IndividualProfile individual =
                IndividualProfile.builder()
                        .id(individualId)
                        .primaryAddress(createAddress())
                        .mailingAddress(createAddress())
                        .build();

        when(repository.save(any(IndividualProfile.class))).thenReturn(individual);

        IndividualProfile individualResult = service.saveIndividual(individual);

        assertEquals(individual, individualResult);
    }

    @Test
    void postAuditEventForIndividualCreated() {
        IndividualProfile individual =
                IndividualProfile.builder()
                        .id(UUID.randomUUID())
                        .createdBy(UUID.randomUUID().toString())
                        .build();

        ProfileCreatedAuditEventDto profileInfo =
                new ProfileCreatedAuditEventDto(individual.getCreatedBy());

        service.postAuditEventForIndividualCreated(individual);

        ArgumentCaptor<AuditEventRequestObjectDto> auditEventCaptor =
                ArgumentCaptor.forClass(AuditEventRequestObjectDto.class);
        verify(auditEventService).sendAuditEvent(auditEventCaptor.capture());
        AuditEventRequestObjectDto capturedAuditEvent = auditEventCaptor.getValue();

        AuditEventRequestObjectDto testEvent =
                AuditEventRequestObjectDto.builder()
                        .originatorId(individual.getCreatedBy())
                        .userId(individual.getCreatedBy())
                        .summary("Profile Created.")
                        .businessObjectId(individual.getId())
                        .businessObjectType(AuditEventBusinessObject.INDIVIDUAL)
                        .data(profileInfo.toJson(), AuditActivityType.INDIVIDUAL_PROFILE_CREATED)
                        .build();

        assertEquals(testEvent.getOriginatorId(), capturedAuditEvent.getOriginatorId());
        assertEquals(testEvent.getUserId(), capturedAuditEvent.getUserId());
        assertEquals(testEvent.getSummary(), capturedAuditEvent.getSummary());
        assertEquals(testEvent.getBusinessObjectId(), capturedAuditEvent.getBusinessObjectId());
        assertEquals(testEvent.getBusinessObjectType(), capturedAuditEvent.getBusinessObjectType());
        assertEquals(testEvent.getData().toString(), capturedAuditEvent.getData().toString());
    }

    @Test
    void getIndividualsByFiltersTest() {
        IndividualFilters filters = mock(IndividualFilters.class);

        when(repository.findAll(any(Specification.class), any(PageRequest.class)))
                .thenReturn(
                        new PageImpl<>(
                                List.of(
                                        IndividualProfile.builder().id(UUID.randomUUID()).build(),
                                        IndividualProfile.builder()
                                                .id(UUID.randomUUID())
                                                .build())));

        when(filters.getIndividualProfileSpecification()).thenReturn(mock(Specification.class));
        when(filters.getPageRequest()).thenReturn(PageRequest.of(0, 10));
        Page<IndividualProfile> result = service.getIndividualsByFilters(filters);

        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
    }

    private Address createAddress() {
        return Address.builder()
                .address1("123 Main St")
                .city("Any-town")
                .state("CA")
                .postalCode("12345")
                .build();
    }
}
