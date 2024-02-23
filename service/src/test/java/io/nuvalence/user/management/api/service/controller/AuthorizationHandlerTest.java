package io.nuvalence.user.management.api.service.controller;

import static io.nuvalence.user.management.api.service.utils.TestUtils.createOrUpdatePermissionModel;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.nuvalence.auth.access.AuthorizationHandler;
import io.nuvalence.user.management.api.service.generated.models.CreateOrUpdatePermissionDTO;
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

import java.util.UUID;

/**
 * Class for testing the handling of not allowed operations.
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class AuthorizationHandlerTest {

    @Autowired private MockMvc mockMvc;

    @MockBean private AuthorizationHandler authorizationHandler;

    @BeforeEach
    void setup() {
        when(authorizationHandler.isAllowed(any(), (Class<?>) any())).thenReturn(false);
        when(authorizationHandler.isAllowed(any(), (String) any())).thenReturn(false);
        when(authorizationHandler.isAllowedForInstance(any(), any())).thenReturn(false);
        when(authorizationHandler.getAuthFilter(any(), any())).thenReturn(element -> false);
    }

    @Test
    @WithMockUser
    void addPermission_AccessDeniedException() throws Exception {
        CreateOrUpdatePermissionDTO permissionModel = createOrUpdatePermissionModel();
        final String postBody = new ObjectMapper().writeValueAsString(permissionModel);

        mockMvc.perform(
                        post("/api/v1/permission")
                                .content(postBody)
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser
    void updatePermission_AccessDeniedException() throws Exception {
        CreateOrUpdatePermissionDTO permissionModel = createOrUpdatePermissionModel();

        final String putBody = new ObjectMapper().writeValueAsString(permissionModel);

        mockMvc.perform(
                        put("/api/v1/permission/" + UUID.randomUUID())
                                .content(putBody)
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser
    void deletePermissionById_AccessDeniedException() throws Exception {
        mockMvc.perform(delete("/api/v1/permission/" + UUID.randomUUID()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser
    void deleteUserById() throws Exception {
        mockMvc.perform(delete("/api/v1/users/" + UUID.randomUUID()))
                .andExpect(status().isForbidden());
    }
}
