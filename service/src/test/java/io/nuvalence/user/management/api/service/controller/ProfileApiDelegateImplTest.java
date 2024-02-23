package io.nuvalence.user.management.api.service.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.nuvalence.auth.access.AuthorizationHandler;
import io.nuvalence.auth.util.SecurityContextUtility;
import io.nuvalence.user.management.api.service.config.SpringConfig;
import io.nuvalence.user.management.api.service.entity.profile.Address;
import io.nuvalence.user.management.api.service.entity.profile.EmployerProfile;
import io.nuvalence.user.management.api.service.entity.profile.IndividualProfile;
import io.nuvalence.user.management.api.service.generated.models.AddressModel;
import io.nuvalence.user.management.api.service.generated.models.EmployerProfileCreateModel;
import io.nuvalence.user.management.api.service.generated.models.EmployerProfileUpdateModel;
import io.nuvalence.user.management.api.service.generated.models.IndividualProfileCreateModel;
import io.nuvalence.user.management.api.service.generated.models.IndividualProfileUpdateModel;
import io.nuvalence.user.management.api.service.models.EmployerFilters;
import io.nuvalence.user.management.api.service.repository.EmployerProfileRepository;
import io.nuvalence.user.management.api.service.repository.IndividualProfileRepository;
import io.nuvalence.user.management.api.service.service.EmployerProfileService;
import io.nuvalence.user.management.api.service.service.IndividualProfileService;
import jakarta.ws.rs.core.MediaType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@WithMockUser(authorities = {"um:admin"})
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public class ProfileApiDelegateImplTest {

    @Autowired private MockMvc mockMvc;

    @MockBean private AuthorizationHandler authorizationHandler;

    @MockBean private EmployerProfileRepository employerProfileRepository;

    @MockBean private EmployerProfileService employerProfileService;

    @MockBean private IndividualProfileRepository individualProfileRepository;

    @MockBean private IndividualProfileService individualProfileService;

    @MockBean private Appender<ILoggingEvent> mockAppender;

    private ObjectMapper objectMapper;

    private final UUID userId = UUID.randomUUID();

    @BeforeEach
    void setup() {
        when(authorizationHandler.isAllowed(any(), (Class<?>) any())).thenReturn(true);
        when(authorizationHandler.isAllowed(any(), (String) any())).thenReturn(true);
        when(authorizationHandler.isAllowedForInstance(any(), any())).thenReturn(true);
        when(authorizationHandler.getAuthFilter(any(), any())).thenReturn(element -> true);
        Logger logger = (Logger) LoggerFactory.getLogger(ProfilesApiDelegateImpl.class);
        logger.addAppender(mockAppender);

        this.objectMapper = SpringConfig.getMapper();
    }

    @Test
    void getEmployerProfile_Success() throws Exception {
        EmployerProfile employer = createEmployer();
        UUID profileId = employer.getId();

        when(employerProfileService.getEmployerById(profileId)).thenReturn(Optional.of(employer));

        mockMvc.perform(get("/api/v1/profiles/employers/" + profileId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(profileId.toString()))
                .andExpect(jsonPath("$.fein").value(employer.getFein()))
                .andExpect(jsonPath("$.legalName").value(employer.getLegalName()))
                .andExpect(jsonPath("$.otherNames", hasSize(1)))
                .andExpect(jsonPath("$.otherNames[0]").value(employer.getOtherNames().get(0)))
                .andExpect(jsonPath("$.type").value(employer.getType()))
                .andExpect(jsonPath("$.industry").value(employer.getIndustry()))
                .andExpect(jsonPath("$.summaryOfBusiness").value(employer.getSummaryOfBusiness()))
                .andExpect(
                        jsonPath("$.mailingAddress.address1")
                                .value(employer.getMailingAddress().getAddress1()))
                .andExpect(jsonPath("$.locations", hasSize(1)))
                .andExpect(
                        jsonPath("$.locations[0].address1")
                                .value(employer.getLocations().get(0).getAddress1()));
    }

    @Test
    void getEmployerProfile_NotFound() throws Exception {
        EmployerProfile employer = createEmployer();
        UUID profileId = employer.getId();
        when(employerProfileService.getEmployerById(profileId)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/profiles/employers/" + profileId))
                .andExpect(status().isNotFound())
                .andExpect(content().string("{\"messages\":[\"Employer profile not found\"]}"));
    }

    @Test
    void getEmployerProfiles() throws Exception {
        EmployerProfile employer = createEmployer();
        Page<EmployerProfile> employerPage = new PageImpl<>(Collections.singletonList(employer));
        when(employerProfileService.getEmployersByFilters(any(EmployerFilters.class)))
                .thenReturn(employerPage);

        mockMvc.perform(get("/api/v1/profiles/employers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.items[0].id").value(employer.getId().toString()))
                .andExpect(jsonPath("$.items[0].fein").value(employer.getFein()))
                .andExpect(jsonPath("$.items[0].legalName").value(employer.getLegalName()))
                .andExpect(jsonPath("$.items[0].otherNames", hasSize(1)))
                .andExpect(
                        jsonPath("$.items[0].otherNames[0]").value(employer.getOtherNames().get(0)))
                .andExpect(jsonPath("$.items[0].type").value(employer.getType()))
                .andExpect(jsonPath("$.items[0].industry").value(employer.getIndustry()))
                .andExpect(
                        jsonPath("$.items[0].summaryOfBusiness")
                                .value(employer.getSummaryOfBusiness()))
                .andExpect(
                        jsonPath("$.items[0].mailingAddress.address1")
                                .value(employer.getMailingAddress().getAddress1()))
                .andExpect(jsonPath("$.items[0].locations", hasSize(1)))
                .andExpect(
                        jsonPath("$.items[0].locations[0].address1")
                                .value(employer.getLocations().get(0).getAddress1()))
                .andExpect(jsonPath("$.pagingMetadata.totalCount").value(1))
                .andExpect(jsonPath("$.pagingMetadata.pageNumber").value(0));
    }

    @Test
    void getEmployerProfilesForbidden() throws Exception {

        when(authorizationHandler.isAllowed("view", EmployerProfile.class)).thenReturn(false);

        mockMvc.perform(get("/api/v1/profiles/employers")).andExpect(status().isForbidden());
    }

    @Test
    void postEmployerProfile() throws Exception {
        EmployerProfileCreateModel employer = employerProfileCreateModel();

        String requestBodyJson = objectMapper.writeValueAsString(employer);

        when(employerProfileService.saveEmployer(any(EmployerProfile.class)))
                .thenReturn(createEmployer());

        mockMvc.perform(
                        post("/api/v1/profiles/employers")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBodyJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fein").value(employer.getFein()))
                .andExpect(jsonPath("$.legalName").value(employer.getLegalName()))
                .andExpect(jsonPath("$.otherNames", hasSize(1)))
                .andExpect(jsonPath("$.otherNames[0]").value(employer.getOtherNames().get(0)))
                .andExpect(jsonPath("$.type").value(employer.getType()))
                .andExpect(jsonPath("$.industry").value(employer.getIndustry()))
                .andExpect(jsonPath("$.summaryOfBusiness").value(employer.getSummaryOfBusiness()))
                .andExpect(
                        jsonPath("$.mailingAddress.address1")
                                .value(employer.getMailingAddress().getAddress1()))
                .andExpect(jsonPath("$.locations", hasSize(1)))
                .andExpect(
                        jsonPath("$.locations[0].address1")
                                .value(employer.getLocations().get(0).getAddress1()));
    }

    @Test
    void postEmployerProfileForbidden() throws Exception {
        EmployerProfileCreateModel employer = employerProfileCreateModel();

        String requestBodyJson = objectMapper.writeValueAsString(employer);

        when(authorizationHandler.isAllowed("create", EmployerProfile.class)).thenReturn(false);

        mockMvc.perform(
                        post("/api/v1/profiles/employers")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBodyJson))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateEmployerProfile_Success() throws Exception {
        EmployerProfileUpdateModel employer = employerProfileUpdateModel();

        EmployerProfile modifiedEmployer =
                EmployerProfile.builder()
                        .id(UUID.randomUUID())
                        .fein("fein - changed")
                        .legalName("legalName - changed")
                        .otherNames(Collections.singletonList("otherNames - changed"))
                        .type("LLC")
                        .industry("industry - changed")
                        .summaryOfBusiness("summaryOfBusiness - changed")
                        .businessPhone("businessPhone - changed")
                        .mailingAddress(createAddress())
                        .locations(List.of(createAddress()))
                        .build();

        when(employerProfileService.getEmployerById(any(UUID.class)))
                .thenReturn(Optional.of(createEmployer()))
                .thenReturn(Optional.of(modifiedEmployer));

        when(employerProfileService.saveEmployer(any(EmployerProfile.class)))
                .thenReturn(modifiedEmployer);

        String requestBodyJson = objectMapper.writeValueAsString(employer);

        try (MockedStatic<SecurityContextUtility> mock =
                Mockito.mockStatic(SecurityContextUtility.class)) {

            mock.when(SecurityContextUtility::getAuthenticatedUserId)
                    .thenReturn(UUID.randomUUID().toString());

            mockMvc.perform(
                            put("/api/v1/profiles/employers/" + modifiedEmployer.getId())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(requestBodyJson))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.fein").value(employer.getFein()))
                    .andExpect(jsonPath("$.legalName").value(employer.getLegalName()))
                    .andExpect(jsonPath("$.otherNames", hasSize(1)))
                    .andExpect(jsonPath("$.otherNames[0]").value(employer.getOtherNames().get(0)))
                    .andExpect(jsonPath("$.type").value(employer.getType()))
                    .andExpect(jsonPath("$.industry").value(employer.getIndustry()))
                    .andExpect(
                            jsonPath("$.summaryOfBusiness").value(employer.getSummaryOfBusiness()))
                    .andExpect(
                            jsonPath("$.mailingAddress.address1")
                                    .value(employer.getMailingAddress().getAddress1()))
                    .andExpect(jsonPath("$.locations", hasSize(1)))
                    .andExpect(
                            jsonPath("$.locations[0].address1")
                                    .value(employer.getLocations().get(0).getAddress1()));
        }
    }

    @Test
    void updateEmployerProfile_Forbidden() throws Exception {
        EmployerProfileUpdateModel employer = employerProfileUpdateModel();

        String requestBodyJson = objectMapper.writeValueAsString(employer);

        when(authorizationHandler.isAllowed("update", EmployerProfile.class)).thenReturn(false);

        mockMvc.perform(
                        put("/api/v1/profiles/employers/" + UUID.randomUUID())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBodyJson))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateEmployerProfile_NotFound() throws Exception {
        EmployerProfileUpdateModel employer = employerProfileUpdateModel();

        String requestBodyJson = objectMapper.writeValueAsString(employer);

        when(employerProfileRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        mockMvc.perform(
                        put("/api/v1/profiles/employers/" + UUID.randomUUID())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBodyJson))
                .andExpect(status().isNotFound())
                .andExpect(content().string("{\"messages\":[\"Employer profile not found\"]}"));
    }

    @Test
    void getIndividualProfile_Success() throws Exception {
        IndividualProfile individual = createIndividual();
        UUID profileId = individual.getId();

        when(individualProfileService.getIndividualById(profileId))
                .thenReturn(Optional.of(individual));

        mockMvc.perform(get("/api/v1/profiles/individuals/" + profileId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(profileId.toString()))
                .andExpect(jsonPath("$.ssn").value(individual.getSsn()))
                .andExpect(
                        jsonPath("$.primaryAddress.address1")
                                .value(individual.getPrimaryAddress().getAddress1()))
                .andExpect(
                        jsonPath("$.mailingAddress.address1")
                                .value(individual.getMailingAddress().getAddress1()));
    }

    @Test
    void getIndividualProfile_NotFound() throws Exception {
        IndividualProfile individual = createIndividual();
        UUID profileId = individual.getId();
        when(individualProfileRepository.findById(profileId)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/profiles/individuals/" + profileId))
                .andExpect(status().isNotFound())
                .andExpect(content().string("{\"messages\":[\"Individual profile not found\"]}"));
    }

    @Test
    void postIndividualProfile() throws Exception {
        IndividualProfileCreateModel individual = individualProfileCreateModel();

        String requestBodyJson = objectMapper.writeValueAsString(individual);

        when(individualProfileRepository.save(any(IndividualProfile.class)))
                .thenReturn(createIndividual());
        when(individualProfileService.saveIndividual(any(IndividualProfile.class)))
                .thenReturn(createIndividual());

        mockMvc.perform(
                        post("/api/v1/profiles/individuals")
                                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                                .content(requestBodyJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ssn").value(individual.getSsn()))
                .andExpect(
                        jsonPath("$.primaryAddress.address1")
                                .value(individual.getPrimaryAddress().getAddress1()))
                .andExpect(
                        jsonPath("$.mailingAddress.address1")
                                .value(individual.getMailingAddress().getAddress1()));
    }

    @Test
    void postIndividualProfileUnAuthorize() throws Exception {
        IndividualProfileCreateModel individual = individualProfileCreateModel();

        String requestBodyJson = objectMapper.writeValueAsString(individual);

        when(authorizationHandler.isAllowed("create", IndividualProfile.class)).thenReturn(false);

        mockMvc.perform(
                        post("/api/v1/profiles/individuals")
                                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                                .content(requestBodyJson))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateIndividualProfile_Success() throws Exception {
        IndividualProfileUpdateModel individual = individualProfileUpdateModel();

        IndividualProfile modifiedIndividual =
                IndividualProfile.builder()
                        .id(UUID.randomUUID())
                        .ssn("ssn2")
                        .primaryAddress(createAddress())
                        .mailingAddress(createAddress())
                        .build();

        when(individualProfileService.getIndividualById(any(UUID.class)))
                .thenReturn(Optional.of(createIndividual()))
                .thenReturn(Optional.of(modifiedIndividual));

        when(individualProfileRepository.save(any(IndividualProfile.class)))
                .thenReturn(modifiedIndividual);

        String requestBodyJson = objectMapper.writeValueAsString(individual);

        try (MockedStatic<SecurityContextUtility> mock =
                Mockito.mockStatic(SecurityContextUtility.class)) {

            mock.when(SecurityContextUtility::getAuthenticatedUserId)
                    .thenReturn(UUID.randomUUID().toString());

            mockMvc.perform(
                            put("/api/v1/profiles/individuals/" + modifiedIndividual.getId())
                                    .contentType(
                                            org.springframework.http.MediaType.APPLICATION_JSON)
                                    .content(requestBodyJson))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.ssn").value(modifiedIndividual.getSsn()))
                    .andExpect(
                            jsonPath("$.primaryAddress.address1")
                                    .value(modifiedIndividual.getPrimaryAddress().getAddress1()))
                    .andExpect(
                            jsonPath("$.mailingAddress.address1")
                                    .value(modifiedIndividual.getMailingAddress().getAddress1()));
        }
    }

    @Test
    void updateIndividualProfile_NotFound() throws Exception {
        IndividualProfileUpdateModel individual = individualProfileUpdateModel();

        String requestBodyJson = objectMapper.writeValueAsString(individual);

        when(individualProfileRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        mockMvc.perform(
                        put("/api/v1/profiles/individuals/" + UUID.randomUUID())
                                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                                .content(requestBodyJson))
                .andExpect(status().isNotFound())
                .andExpect(content().string("{\"messages\":[\"Individual profile not found\"]}"));
    }

    @Test
    void updateIndividualProfile_UnAuthorize() throws Exception {
        IndividualProfileUpdateModel individual = individualProfileUpdateModel();

        String requestBodyJson = objectMapper.writeValueAsString(individual);

        when(authorizationHandler.isAllowed("update", IndividualProfile.class)).thenReturn(false);

        mockMvc.perform(
                        put("/api/v1/profiles/individuals/" + UUID.randomUUID())
                                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                                .content(requestBodyJson))
                .andExpect(status().isForbidden());
    }

    @Test
    void getIndividualProfiles() throws Exception {
        IndividualProfile individual = createIndividual();
        Page<IndividualProfile> individualPage =
                new PageImpl<>(Collections.singletonList(individual));

        when(individualProfileService.getIndividualsByFilters(any())).thenReturn(individualPage);

        mockMvc.perform(get("/api/v1/profiles/individuals"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.items[0].id").value(individual.getId().toString()))
                .andExpect(jsonPath("$.items[0].ssn").value(individual.getSsn()))
                .andExpect(
                        jsonPath("$.items[0].primaryAddress.address1")
                                .value(individual.getPrimaryAddress().getAddress1()))
                .andExpect(
                        jsonPath("$.items[0].mailingAddress.address1")
                                .value(individual.getMailingAddress().getAddress1()))
                .andExpect(jsonPath("$.pagingMetadata.totalCount").value(1))
                .andExpect(jsonPath("$.pagingMetadata.pageNumber").value(0));
    }

    @Test
    void getIndividualProfilesUnAuthorize() throws Exception {

        when(authorizationHandler.isAllowed("view", IndividualProfile.class)).thenReturn(false);

        mockMvc.perform(get("/api/v1/profiles/individuals")).andExpect(status().isForbidden());
    }

    private Address createAddress() {
        return Address.builder()
                .address1("address1")
                .address2("address2")
                .city("city")
                .state("state")
                .postalCode("postalCode")
                .country("country")
                .county("county")
                .build();
    }

    private EmployerProfile createEmployer() {
        return EmployerProfile.builder()
                .id(UUID.randomUUID())
                .fein("fein")
                .legalName("legalName")
                .otherNames(Collections.singletonList("otherNames"))
                .type("LLC")
                .industry("industry")
                .summaryOfBusiness("summaryOfBusiness")
                .businessPhone("businessPhone")
                .mailingAddress(createAddress())
                .locations(List.of(createAddress()))
                .build();
    }

    private AddressModel createAddressModel() {
        AddressModel addressModel = new AddressModel();
        addressModel.address1("address1");
        addressModel.address2("address2");
        addressModel.city("city");
        addressModel.state("state");
        addressModel.postalCode("postalCode");
        addressModel.country("country");
        addressModel.county("county");

        return addressModel;
    }

    private EmployerProfileCreateModel employerProfileCreateModel() {
        EmployerProfileCreateModel employerProfileCreateModel = new EmployerProfileCreateModel();
        employerProfileCreateModel.setFein("fein");
        employerProfileCreateModel.setLegalName("legalName");
        employerProfileCreateModel.setOtherNames(Collections.singletonList("otherNames"));
        employerProfileCreateModel.setType("LLC");
        employerProfileCreateModel.setIndustry("industry");
        employerProfileCreateModel.setSummaryOfBusiness("summaryOfBusiness");
        employerProfileCreateModel.setMailingAddress(createAddressModel());
        employerProfileCreateModel.setLocations(List.of(createAddressModel()));
        employerProfileCreateModel.businessPhone("businessPhone");

        return employerProfileCreateModel;
    }

    private EmployerProfileUpdateModel employerProfileUpdateModel() {
        EmployerProfileUpdateModel employerProfileUpdateModel = new EmployerProfileUpdateModel();
        employerProfileUpdateModel.setFein("fein - changed");
        employerProfileUpdateModel.setLegalName("legalName - changed");
        employerProfileUpdateModel.setOtherNames(Collections.singletonList("otherNames - changed"));
        employerProfileUpdateModel.setType("LLC");
        employerProfileUpdateModel.setIndustry("industry - changed");
        employerProfileUpdateModel.setSummaryOfBusiness("summaryOfBusiness - changed");
        employerProfileUpdateModel.setMailingAddress(createAddressModel());
        employerProfileUpdateModel.setLocations(List.of(createAddressModel()));
        employerProfileUpdateModel.businessPhone("businessPhone - changed");

        return employerProfileUpdateModel;
    }

    private IndividualProfile createIndividual() {
        return IndividualProfile.builder()
                .id(UUID.randomUUID())
                .ssn("ssn")
                .primaryAddress(createAddress())
                .mailingAddress(createAddress())
                .build();
    }

    private IndividualProfileCreateModel individualProfileCreateModel() {
        IndividualProfileCreateModel individualProfileCreateModel =
                new IndividualProfileCreateModel();
        individualProfileCreateModel.setSsn("ssn");
        individualProfileCreateModel.setEmail("email@email.com");
        individualProfileCreateModel.setFirstName("First");
        individualProfileCreateModel.setLastName("Last");
        individualProfileCreateModel.setPhoneNumber("3331112222");
        individualProfileCreateModel.setPrimaryAddress(createAddressModel());
        individualProfileCreateModel.setMailingAddress(createAddressModel());
        return individualProfileCreateModel;
    }

    private IndividualProfileUpdateModel individualProfileUpdateModel() {
        IndividualProfileUpdateModel individualProfileUpdateModel =
                new IndividualProfileUpdateModel();
        individualProfileUpdateModel.setSsn("ssn");
        individualProfileUpdateModel.setEmail("email@email.com");
        individualProfileUpdateModel.setFirstName("First");
        individualProfileUpdateModel.setLastName("Last");
        individualProfileUpdateModel.setPhoneNumber("3331112222");
        individualProfileUpdateModel.setPrimaryAddress(createAddressModel());
        individualProfileUpdateModel.setMailingAddress(createAddressModel());
        return individualProfileUpdateModel;
    }
}
