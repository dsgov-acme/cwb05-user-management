package io.nuvalence.user.management.api.service.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.nuvalence.auth.access.AuthorizationHandler;
import io.nuvalence.user.management.api.service.entity.ApplicationEntity;
import io.nuvalence.user.management.api.service.generated.models.ApplicationDTO;
import io.nuvalence.user.management.api.service.service.ApplicationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class ApplicationsApiDelegateImplTest {
    @Autowired private MockMvc mockMvc;

    @MockBean private ApplicationService applicationService;

    @MockBean private AuthorizationHandler authorizationHandler;

    @BeforeEach
    void setup() {
        when(authorizationHandler.isAllowed(any(), (Class<?>) any())).thenReturn(true);
        when(authorizationHandler.isAllowed(any(), (String) any())).thenReturn(true);
        when(authorizationHandler.isAllowedForInstance(any(), any())).thenReturn(true);
        when(authorizationHandler.getAuthFilter(any(), any())).thenReturn(element -> true);
    }

    @Test
    @WithMockUser
    void getApplications() throws Exception {
        List<ApplicationDTO> expected = createApplications();

        when(applicationService.getApplications())
                .thenReturn(expected.stream().map(this::asEntity).collect(Collectors.toList()));

        mockMvc.perform(get("/api/v1/applications"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)));
    }

    @Test
    @WithMockUser
    void getApplicationById() throws Exception {
        ApplicationEntity application = this.asEntity(createApplication("TEST_1", "Test 1"));

        when(applicationService.getApplicationById(any())).thenReturn(Optional.of(application));

        mockMvc.perform(get("/api/v1/applications/" + application.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(String.valueOf(application.getId())))
                .andExpect(jsonPath("$.name").value(application.getName()))
                .andExpect(jsonPath("$.displayName").value(application.getDisplayName()));
    }

    private ApplicationEntity asEntity(ApplicationDTO app) {
        ApplicationEntity entity = new ApplicationEntity();
        entity.setName(app.getName());
        entity.setId(app.getId());
        entity.setDisplayName(app.getDisplayName());
        return entity;
    }

    private ApplicationDTO createApplication(String name, String displayName) {
        ApplicationDTO app = new ApplicationDTO();
        app.setId(UUID.randomUUID());
        app.setName(name);
        app.setDisplayName(displayName);
        return app;
    }

    private List<ApplicationDTO> createApplications() {
        ApplicationDTO app0 = createApplication("group_a", "Group A");
        ApplicationDTO app1 = createApplication("group_b", "Group B");
        ApplicationDTO app2 = createApplication("group_c", "Group C");

        return List.of(app0, app1, app2);
    }
}
