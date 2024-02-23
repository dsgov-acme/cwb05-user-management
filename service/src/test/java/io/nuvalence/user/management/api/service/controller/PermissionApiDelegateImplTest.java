package io.nuvalence.user.management.api.service.controller;

import static io.nuvalence.user.management.api.service.utils.TestUtils.createMockPermission;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.nuvalence.auth.access.AuthorizationHandler;
import io.nuvalence.user.management.api.service.entity.PermissionEntity;
import io.nuvalence.user.management.api.service.generated.models.CreateOrUpdatePermissionDTO;
import io.nuvalence.user.management.api.service.repository.PermissionRepository;
import io.nuvalence.user.management.api.service.service.PermissionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class PermissionApiDelegateImplTest {
    @Autowired private MockMvc mockMvc;

    @Mock private PermissionRepository permissionRepository;

    @MockBean private PermissionService permissionService;

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
    void addPermission() throws Exception {
        CreateOrUpdatePermissionDTO permissionModel = createOrUpdatePermissionModel();
        PermissionEntity permission = createMockPermission();

        when(permissionService.addPermission(permissionModel)).thenReturn(permission);
        final String postBody = new ObjectMapper().writeValueAsString(permissionModel);

        mockMvc.perform(
                        post("/api/v1/permission")
                                .content(postBody)
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void updatePermission() throws Exception {
        PermissionEntity permission = createMockPermission();
        CreateOrUpdatePermissionDTO permissionModel = createOrUpdatePermissionModel();

        when(permissionService.updatePermission(any(), any())).thenReturn(permission);
        final String putBody = new ObjectMapper().writeValueAsString(permissionModel);

        mockMvc.perform(
                        put("/api/v1/permission/" + permission.getId().toString())
                                .content(putBody)
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void getAllPermissions() throws Exception {
        PermissionEntity permissionEntity = createMockPermission();
        when(permissionService.getAllPermissions()).thenReturn(List.of(permissionEntity));

        mockMvc.perform(get("/api/v1/permission").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].description").value(permissionEntity.getDescription()))
                .andExpect(jsonPath("$[0].name").value(permissionEntity.getName()));
    }

    @Test
    @WithMockUser
    void deletePermissionById() throws Exception {
        PermissionEntity permissionEntity = createMockPermission();
        when(permissionRepository.findById(any())).thenReturn(Optional.of(permissionEntity));

        mockMvc.perform(delete("/api/v1/permission/" + UUID.randomUUID()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void getPermissionById() throws Exception {
        PermissionEntity permissionEntity = createMockPermission();
        when(permissionService.getPermissionById(permissionEntity.getId()))
                .thenReturn(Optional.of(permissionEntity));

        mockMvc.perform(get("/api/v1/permission/" + permissionEntity.getId().toString()))
                .andExpect(status().isOk());
    }

    private CreateOrUpdatePermissionDTO createOrUpdatePermissionModel() {
        CreateOrUpdatePermissionDTO permission = new CreateOrUpdatePermissionDTO();
        permission.setName("test_perm");
        permission.setDisplayName("Test Permission");
        permission.setDescription("This is a test permission.");
        return permission;
    }
}
