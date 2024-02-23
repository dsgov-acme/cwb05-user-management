package io.nuvalence.user.management.api.service.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.nuvalence.user.management.api.service.entity.profile.Address;
import io.nuvalence.user.management.api.service.entity.profile.EmployerProfile;
import io.nuvalence.user.management.api.service.events.model.AuditEventRequestObjectDto;
import io.nuvalence.user.management.api.service.models.EmployerFilters;
import io.nuvalence.user.management.api.service.models.auditevents.AuditActivityType;
import io.nuvalence.user.management.api.service.models.auditevents.AuditEventBusinessObject;
import io.nuvalence.user.management.api.service.models.auditevents.ProfileCreatedAuditEventDto;
import io.nuvalence.user.management.api.service.repository.EmployerProfileRepository;
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

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ExtendWith(MockitoExtension.class)
public class EmployerProfileServiceTest {
    @Mock private EmployerProfileRepository repository;
    @Mock private AuditEventService employerAuditEventService;

    private EmployerProfileService service;

    @BeforeEach
    public void setUp() {
        service = new EmployerProfileService(repository, employerAuditEventService);
    }

    @Test
    void getEmployersByFilters() {
        EmployerProfile employer = EmployerProfile.builder().id(UUID.randomUUID()).build();
        Page<EmployerProfile> employerPageExpected =
                new PageImpl<>(Collections.singletonList(employer));

        when(repository.findAll(any(Specification.class), any(PageRequest.class)))
                .thenReturn(employerPageExpected);

        Page<EmployerProfile> employerPageResult =
                service.getEmployersByFilters(
                        EmployerFilters.builder()
                                .sortBy("legalName")
                                .sortOrder("ASC")
                                .pageNumber(0)
                                .pageSize(10)
                                .fein("fein")
                                .name("name")
                                .type("LLC")
                                .industry("industry")
                                .build());

        assertEquals(employerPageExpected, employerPageResult);
    }

    @Test
    void getEmployerById_Success() {
        EmployerProfile employer = EmployerProfile.builder().id(UUID.randomUUID()).build();

        when(repository.findById(any(UUID.class))).thenReturn(Optional.of(employer));

        Optional<EmployerProfile> employerResult = service.getEmployerById(UUID.randomUUID());

        assertTrue(employerResult.isPresent());
        assertEquals(employer, employerResult.get());
    }

    @Test
    void getEmployerById_Null() {
        when(repository.findById(any(UUID.class))).thenReturn(Optional.empty());

        Optional<EmployerProfile> employerResult = service.getEmployerById(UUID.randomUUID());

        assertTrue(employerResult.isEmpty());
    }

    @Test
    void getEmployerByIdWithNullId() {
        Optional<EmployerProfile> employerResult = service.getEmployerById(null);
        assertTrue(employerResult.isEmpty());
    }

    @Test
    void saveEmployer() {
        EmployerProfile employer =
                EmployerProfile.builder()
                        .id(UUID.randomUUID())
                        .mailingAddress(createAddress())
                        .locations(List.of(createAddress(), createAddress()))
                        .build();

        when(repository.save(any(EmployerProfile.class))).thenReturn(employer);

        EmployerProfile employerResult = service.saveEmployer(employer);

        assertEquals(employer, employerResult);
    }

    @Test
    void postAuditEventForEmployerCreated() {
        EmployerProfile employer =
                EmployerProfile.builder()
                        .id(UUID.randomUUID())
                        .createdBy(UUID.randomUUID().toString())
                        .build();

        ProfileCreatedAuditEventDto profileInfo =
                new ProfileCreatedAuditEventDto(employer.getCreatedBy());

        service.postAuditEventForEmployerCreated(employer);

        ArgumentCaptor<AuditEventRequestObjectDto> auditEventCaptor =
                ArgumentCaptor.forClass(AuditEventRequestObjectDto.class);
        verify(employerAuditEventService).sendAuditEvent(auditEventCaptor.capture());
        AuditEventRequestObjectDto capturedAuditEvent = auditEventCaptor.getValue();

        AuditEventRequestObjectDto testEvent =
                AuditEventRequestObjectDto.builder()
                        .originatorId(employer.getCreatedBy())
                        .userId(employer.getCreatedBy())
                        .summary("Profile Created.")
                        .businessObjectId(employer.getId())
                        .businessObjectType(AuditEventBusinessObject.EMPLOYER)
                        .data(profileInfo.toJson(), AuditActivityType.EMPLOYER_PROFILE_CREATED)
                        .build();

        assertEquals(testEvent.getOriginatorId(), capturedAuditEvent.getOriginatorId());
        assertEquals(testEvent.getUserId(), capturedAuditEvent.getUserId());
        assertEquals(testEvent.getSummary(), capturedAuditEvent.getSummary());
        assertEquals(testEvent.getBusinessObjectId(), capturedAuditEvent.getBusinessObjectId());
        assertEquals(testEvent.getBusinessObjectType(), capturedAuditEvent.getBusinessObjectType());
        assertEquals(testEvent.getData().toString(), capturedAuditEvent.getData().toString());
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
