package io.nuvalence.user.management.api.service.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.nuvalence.auth.access.AuthorizationHandler;
import io.nuvalence.user.management.api.service.entity.PermissionEntity;
import io.nuvalence.user.management.api.service.generated.models.ApplicationRolesDTO;
import io.nuvalence.user.management.api.service.service.ApplicationService;
import io.nuvalence.user.management.api.service.service.PermissionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class ApplicationApiDelegateImplTest {
    @Autowired private MockMvc mockMvc;

    @MockBean private PermissionService permissionService;

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
    void getApplicationRoles() throws Exception {
        when(permissionService.getAllActivePermissions()).thenReturn(createPermissions());

        mockMvc.perform(get("/api/v1/application/roles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(10)))
                .andExpect(jsonPath("$.[0].name").value("permission-0"))
                .andExpect(jsonPath("$.[0].applicationRole").value("um:role-name-0"));
    }

    @Test
    @WithMockUser
    void setApplicationRoles() throws Exception {
        doNothing().when(applicationService).setApplicationRoles(any(), any());

        ApplicationRolesDTO request =
                new ApplicationRolesDTO().name("application-0").roles(new ArrayList<>());

        final String putBody = new ObjectMapper().writeValueAsString(request);

        mockMvc.perform(
                        put("/api/v1/application/roles")
                                .content(putBody)
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(204));
    }

    private List<PermissionEntity> createPermissions() {
        List<PermissionEntity> permissions = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            PermissionEntity permission = new PermissionEntity();

            permission.setActive(true);
            permission.setName(String.format("permission-%d", i));
            permission.setGroup(String.format("group-%d", i % 2));
            permission.setDescription("Random permission.");
            permission.setApplicationRole(String.format("um:role-name-%d", i));

            permissions.add(permission);
        }

        return permissions;
    }
}
