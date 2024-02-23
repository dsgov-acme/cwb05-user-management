package io.nuvalence.user.management.api.service.controller;

import io.nuvalence.auth.access.AuthorizationHandler;
import io.nuvalence.user.management.api.service.config.exception.ResourceNotFoundException;
import io.nuvalence.user.management.api.service.entity.RoleEntity;
import io.nuvalence.user.management.api.service.generated.controllers.DefaultRolesApiDelegate;
import io.nuvalence.user.management.api.service.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * Controller for default roles API.
 */
@Service
@RequiredArgsConstructor
public class DefaultRolesApiDelegateImpl implements DefaultRolesApiDelegate {

    private final AuthorizationHandler authorizationHandler;
    private final RoleService roleService;

    @Override
    public ResponseEntity<List<UUID>> upsertDefaultRoles(String userType, List<UUID> roleIds) {
        if (!authorizationHandler.isAllowed("update", RoleEntity.class)) {
            throw new AccessDeniedException("You do not have permission to modify this resource");
        }

        roleIds.forEach(
                roleId ->
                        roleService
                                .getRole(roleId)
                                .orElseThrow(
                                        () ->
                                                new ResourceNotFoundException(
                                                        "Role with id " + roleId + " not found")));

        List<UUID> savedRoleIds = roleService.upsertDefaultRoles(userType, roleIds);

        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(savedRoleIds);
    }

    @Override
    public ResponseEntity<List<UUID>> getDefaultRoles(String userType) {
        if (!authorizationHandler.isAllowed("view", RoleEntity.class)) {
            throw new AccessDeniedException("You do not have permission to view this resource");
        }

        List<UUID> roleIds =
                roleService.getDefaultRoles(userType).stream().map(RoleEntity::getId).toList();

        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(roleIds);
    }

    @Override
    public ResponseEntity<Void> deleteDefaultRoles(String userType) {
        if (!authorizationHandler.isAllowed("delete", RoleEntity.class)) {
            throw new AccessDeniedException("You do not have permission to delete this resource");
        }

        roleService.deleteDefaultRoles(userType);

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
