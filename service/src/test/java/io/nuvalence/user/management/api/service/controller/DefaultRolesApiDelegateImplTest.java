package io.nuvalence.user.management.api.service.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.nuvalence.auth.access.AuthorizationHandler;
import io.nuvalence.user.management.api.service.entity.RoleEntity;
import io.nuvalence.user.management.api.service.service.RoleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class DefaultRolesApiDelegateImplTest {

    private static final String USER_TYPE = "agency";
    private static final String DEFAULT_ROLE_PATH = "/api/v1/default-roles/";

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
    void addDefaultRoles() throws Exception {
        List<UUID> roleIds = List.of(UUID.randomUUID());
        when(roleService.getRole(any())).thenReturn(Optional.of(createRoleEntity()));
        when(roleService.upsertDefaultRoles(any(), any())).thenReturn(roleIds);

        final String postBody = new ObjectMapper().writeValueAsString(roleIds);

        mockMvc.perform(
                        put(DEFAULT_ROLE_PATH + USER_TYPE)
                                .content(postBody)
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0]").value(roleIds.get(0).toString()));
    }

    @Test
    void addDefaultRolesWithEmptyArray() throws Exception {
        List<UUID> roleIds = Collections.emptyList();

        when(roleService.getRole(any())).thenReturn(Optional.empty());
        when(roleService.upsertDefaultRoles(any(), any())).thenReturn(roleIds);

        final String postBody = new ObjectMapper().writeValueAsString(roleIds);

        mockMvc.perform(
                        put(DEFAULT_ROLE_PATH + USER_TYPE)
                                .content(postBody)
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void addDefaultRolesForbidden() throws Exception {
        List<UUID> roleIds = List.of(UUID.randomUUID());

        when(roleService.getRole(any())).thenReturn(Optional.of(createRoleEntity()));
        when(authorizationHandler.isAllowed("update", RoleEntity.class)).thenReturn(false);

        final String postBody = new ObjectMapper().writeValueAsString(roleIds);

        mockMvc.perform(
                        put(DEFAULT_ROLE_PATH + USER_TYPE)
                                .content(postBody)
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    void getDefaultRoles() throws Exception {
        List<RoleEntity> defaultRoles = List.of(createRoleEntity());

        when(roleService.getDefaultRoles(any())).thenReturn(defaultRoles);

        mockMvc.perform(get(DEFAULT_ROLE_PATH + USER_TYPE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0]").value(defaultRoles.get(0).getId().toString()));
    }

    @Test
    void getDefaultRolesForbidden() throws Exception {

        when(authorizationHandler.isAllowed("view", RoleEntity.class)).thenReturn(false);

        mockMvc.perform(get(DEFAULT_ROLE_PATH + USER_TYPE)).andExpect(status().isForbidden());
    }

    @Test
    void deleteDefaultRoles() throws Exception {

        mockMvc.perform(delete("/api/v1/default-roles/{userType}", USER_TYPE))
                .andExpect(status().isNoContent());

        verify(roleService).deleteDefaultRoles(USER_TYPE);
    }

    @Test
    void deleteDefaultRolesForbidden() throws Exception {
        when(authorizationHandler.isAllowed("delete", RoleEntity.class)).thenReturn(false);

        mockMvc.perform(delete("/api/v1/default-roles/{userType}", USER_TYPE))
                .andExpect(status().isForbidden());
    }

    private RoleEntity createRoleEntity() {
        RoleEntity role = new RoleEntity();
        role.setName("ROLE_TO_TEST");
        role.setId(UUID.fromString("af102616-4207-4850-adc4-0bf91058a261"));
        role.setDefaultUserTypes(new HashSet<>());
        return role;
    }
}
