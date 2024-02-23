package io.nuvalence.user.management.api.service.controller;

import io.nuvalence.auth.access.AuthorizationHandler;
import io.nuvalence.user.management.api.service.config.exception.ResourceNotFoundException;
import io.nuvalence.user.management.api.service.entity.RoleEntity;
import io.nuvalence.user.management.api.service.generated.controllers.RolesApiDelegate;
import io.nuvalence.user.management.api.service.generated.models.RoleDTO;
import io.nuvalence.user.management.api.service.generated.models.RoleUpsertRequest;
import io.nuvalence.user.management.api.service.mapper.RoleEntityMapper;
import io.nuvalence.user.management.api.service.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.NativeWebRequest;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Controller for Roles API.
 */
@Service
@RequiredArgsConstructor
public class RoleApiDelegateImpl implements RolesApiDelegate {

    private final RoleService roleService;
    private final AuthorizationHandler authorizationHandler;

    @Override
    public Optional<NativeWebRequest> getRequest() {
        return RolesApiDelegate.super.getRequest();
    }

    @Override
    public ResponseEntity<RoleDTO> upsertRole(RoleUpsertRequest body) {
        if (!authorizationHandler.isAllowed("update", RoleEntity.class)) {
            throw new AccessDeniedException("You do not have permission to modify this resource");
        }

        RoleEntity newRole = roleService.upsertRole(body);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(RoleEntityMapper.INSTANCE.roleEntityToRoleDto(newRole));
    }

    @Override
    public ResponseEntity<List<RoleDTO>> getAllRoles(String resource) {
        List<RoleDTO> roles =
                roleService.getAllRoles().stream()
                        .filter(authorizationHandler.getAuthFilter("view", RoleEntity.class))
                        .map(RoleEntityMapper.INSTANCE::roleEntityToRoleDto)
                        .toList();

        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(roles);
    }

    @Override
    public ResponseEntity<Void> deleteRoleById(UUID id, String resource) {
        if (!authorizationHandler.isAllowed("delete", RoleEntity.class)) {
            throw new AccessDeniedException("You do not have permission to modify this resource");
        }

        roleService.deleteRoleById(id, resource);

        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<RoleDTO> getRoleById(UUID id) {
        RoleEntity role =
                roleService
                        .getRole(id)
                        .filter(
                                roleEntity ->
                                        authorizationHandler.isAllowedForInstance(
                                                "view", roleEntity))
                        .orElseThrow(
                                () -> new ResourceNotFoundException("Role with ID not found."));

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(RoleEntityMapper.INSTANCE.roleEntityToRoleDto(role));
    }
}
