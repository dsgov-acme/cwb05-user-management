package io.nuvalence.user.management.api.service.controller;

import io.nuvalence.auth.access.AuthorizationHandler;
import io.nuvalence.user.management.api.service.config.exception.ResourceNotFoundException;
import io.nuvalence.user.management.api.service.entity.PermissionEntity;
import io.nuvalence.user.management.api.service.generated.controllers.PermissionApiDelegate;
import io.nuvalence.user.management.api.service.generated.models.CreateOrUpdatePermissionDTO;
import io.nuvalence.user.management.api.service.generated.models.PermissionDTO;
import io.nuvalence.user.management.api.service.mapper.MapperUtils;
import io.nuvalence.user.management.api.service.mapper.PermissionEntityMapper;
import io.nuvalence.user.management.api.service.service.PermissionService;
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
 * Controller for Role API.
 */
@Service
@RequiredArgsConstructor
public class PermissionApiDelegateImpl implements PermissionApiDelegate {
    private static final PermissionEntityMapper mapper = PermissionEntityMapper.INSTANCE;
    private static final String UPDATE_PERMISSION = "update";
    private static final String ACCESS_DENIED_MESSAGE =
            "You do not have permission to modify this resource";
    private final PermissionService permissionService;
    private final AuthorizationHandler authorizationHandler;

    @Override
    public Optional<NativeWebRequest> getRequest() {
        return PermissionApiDelegate.super.getRequest();
    }

    @Override
    public ResponseEntity<Void> addPermission(CreateOrUpdatePermissionDTO body) {
        if (!authorizationHandler.isAllowed(UPDATE_PERMISSION, PermissionEntity.class)) {
            throw new AccessDeniedException(ACCESS_DENIED_MESSAGE);
        }

        permissionService.addPermission(body);

        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<Void> updatePermission(UUID id, CreateOrUpdatePermissionDTO body) {
        if (!authorizationHandler.isAllowed(UPDATE_PERMISSION, PermissionEntity.class)) {
            throw new AccessDeniedException(ACCESS_DENIED_MESSAGE);
        }

        permissionService.updatePermission(id, body);

        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<List<PermissionDTO>> getAllPermissions() {
        List<PermissionEntity> permissions =
                permissionService.getAllPermissions().stream()
                        .filter(authorizationHandler.getAuthFilter("view", PermissionEntity.class))
                        .toList();

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(MapperUtils.mapPermissionEntitiesToPermissionList(permissions));
    }

    @Override
    public ResponseEntity<Void> deletePermissionById(UUID id) {
        if (!authorizationHandler.isAllowed(UPDATE_PERMISSION, PermissionEntity.class)) {
            throw new AccessDeniedException(ACCESS_DENIED_MESSAGE);
        }

        permissionService.deletePermissionById(id);

        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<PermissionDTO> getPermissionById(UUID id) {
        PermissionEntity permission =
                permissionService
                        .getPermissionById(id)
                        .filter(
                                permissionEntity ->
                                        authorizationHandler.isAllowedForInstance(
                                                "view", permissionEntity))
                        .orElseThrow(
                                () ->
                                        new ResourceNotFoundException(
                                                "Could not find permission with ID '" + id + "'."));

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(mapper.permissionEntityToPermissionDto(permission));
    }
}
