package io.nuvalence.user.management.api.service.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.nuvalence.auth.access.AuthorizationHandler;
import io.nuvalence.user.management.api.service.entity.PermissionEntity;
import io.nuvalence.user.management.api.service.entity.RoleEntity;
import io.nuvalence.user.management.api.service.entity.UserEntity;
import io.nuvalence.user.management.api.service.generated.models.RoleDTO;
import io.nuvalence.user.management.api.service.generated.models.RoleUpsertRequest;
import io.nuvalence.user.management.api.service.service.RoleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
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
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
class RoleApiDelegateImplTest {
    @Autowired private MockMvc mockMvc;

    @MockBean private RoleService roleService;

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
    void addRole() throws Exception {
        RoleUpsertRequest roleUpsertRequest = new RoleUpsertRequest();
        roleUpsertRequest.id(UUID.randomUUID());
        roleUpsertRequest.name("Test role");
        roleUpsertRequest.description("Description");

        when(roleService.addRole(roleUpsertRequest)).thenReturn(new RoleEntity());
        final String postBody = new ObjectMapper().writeValueAsString(roleUpsertRequest);

        mockMvc.perform(
                        put("/api/v1/roles")
                                .content(postBody)
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void getAllRoles() throws Exception {
        List<RoleEntity> roles = List.of(createMockRoleEntity());

        when(roleService.getAllRoles()).thenReturn(roles);

        mockMvc.perform(
                        get("/api/v1/roles?resource=default_resource")
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value(roles.get(0).getName()));
    }

    @Test
    @WithMockUser
    void deleteRoleById() throws Exception {
        RoleDTO role = createMockRoleDto();

        doNothing()
                .when(roleService)
                .deleteRoleById(eq(role.getId()), ArgumentMatchers.anyString());

        mockMvc.perform(
                        delete(
                                "/api/v1/roles/"
                                        + role.getId().toString()
                                        + "?resource=default_resource"))
                .andExpect(status().isOk());
    }

    @Test
    void testFindRoleById_NotFound() throws Exception {
        UUID uuid = UUID.randomUUID();

        when(roleService.getRole(uuid)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/roles/" + uuid).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void testFindRoleById_NotAuthorizedToAccess() throws Exception {
        UUID uuid = UUID.randomUUID();
        RoleEntity roleEntity = new RoleEntity();

        when(roleService.getRole(uuid)).thenReturn(Optional.of(roleEntity));
        when(authorizationHandler.isAllowedForInstance("view", roleEntity)).thenReturn(false);

        mockMvc.perform(get("/api/v1/roles/" + uuid).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void testFindRoleById_Succeeds() throws Exception {
        UUID uuid = UUID.randomUUID();
        String roleName = "name";
        String roleDescription = "description";
        List<UserEntity> roleUsers = List.of();
        PermissionEntity permissionEntity = new PermissionEntity();
        permissionEntity.setApplicationRole("role");
        List<PermissionEntity> permissions = List.of(permissionEntity);

        RoleEntity roleEntity =
                new RoleEntity(uuid, roleName, roleDescription, roleUsers, permissions, null);

        when(roleService.getRole(uuid)).thenReturn(Optional.of(roleEntity));
        when(authorizationHandler.isAllowed("view", RoleEntity.class)).thenReturn(true);

        mockMvc.perform(get("/api/v1/roles/" + uuid).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(uuid.toString()))
                .andExpect(jsonPath("$.name").value(roleName))
                .andExpect(jsonPath("$.description").value(roleDescription))
                .andExpect(jsonPath("$.permissions", hasSize(1)))
                .andExpect(
                        jsonPath("$.permissions[0]").value(permissionEntity.getApplicationRole()));
    }

    private RoleDTO createMockRoleDto() {
        RoleDTO role = new RoleDTO();
        role.setName("ROLE_TO_TEST");
        role.setId(UUID.randomUUID());
        return role;
    }

    private RoleEntity createMockRoleEntity() {
        RoleEntity entity = new RoleEntity();
        entity.setName("ROLE_TO_TEST");
        entity.setId(UUID.randomUUID());
        return entity;
    }
}
