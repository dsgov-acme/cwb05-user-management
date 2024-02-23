package io.nuvalence.user.management.api.service.service;

import io.nuvalence.user.management.api.service.config.exception.BusinessLogicException;
import io.nuvalence.user.management.api.service.config.exception.ResourceNotFoundException;
import io.nuvalence.user.management.api.service.entity.ApplicationEntity;
import io.nuvalence.user.management.api.service.entity.PermissionEntity;
import io.nuvalence.user.management.api.service.generated.models.CreateOrUpdatePermissionDTO;
import io.nuvalence.user.management.api.service.repository.PermissionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

/**
 * Service for Permission.
 */
@Component
@Transactional
@RequiredArgsConstructor
@Slf4j
public class PermissionService {

    private final PermissionRepository permissionRepository;

    private final ApplicationService applicationService;

    /**
     * Adds a permission to the database.
     *
     * @param permission the permission to add.
     * @return the new permission entity.
     *
     * @throws BusinessLogicException if the permission already exists.
     * @throws ResourceNotFoundException if the application does not exist.
     */
    public PermissionEntity addPermission(CreateOrUpdatePermissionDTO permission) {
        Optional<PermissionEntity> existingPermission =
                permissionRepository.findByApplicationRole(
                        permission.getApplicationRole().toLowerCase(Locale.US));

        if (existingPermission.isPresent()) {
            throw new BusinessLogicException("This permission already exists.");
        }

        PermissionEntity newPermission = new PermissionEntity();
        newPermission.setDescription(permission.getDescription());

        // Ensures lowercase on submission
        newPermission.setName(permission.getName().toLowerCase(Locale.US));

        if (permission.getApplication() != null) {
            Optional<ApplicationEntity> requestedApplication =
                    applicationService.getApplicationById(permission.getApplication());

            if (requestedApplication.isEmpty()) {
                throw new ResourceNotFoundException(
                        String.format(
                                "Application with ID %s not found.",
                                permission.getApplication().toString()));
            }

            newPermission.setApplication(requestedApplication.get());
        }

        return permissionRepository.save(newPermission);
    }

    /**
     * Updates a permission.
     *
     * @param permissionId The id of the permission.
     * @param permissionRequest The permission object.
     * @return the updated permission entity.
     */
    public PermissionEntity updatePermission(
            UUID permissionId, CreateOrUpdatePermissionDTO permissionRequest) {
        PermissionEntity permissionEntity =
                this.getPermissionById(permissionId)
                        .orElseThrow(
                                () ->
                                        new BusinessLogicException(
                                                "This permission does not exist."));

        permissionEntity.setName(permissionRequest.getName());
        permissionEntity.setDescription(permissionRequest.getDescription());

        // Check in case the applications field is omitted; it is technically not required.
        if (permissionRequest.getApplication() != null) {
            ApplicationEntity application =
                    applicationService
                            .getApplicationById(permissionRequest.getApplication())
                            .orElseThrow(
                                    () ->
                                            new ResourceNotFoundException(
                                                    "Application not found with ID "
                                                            + permissionRequest.getApplication()
                                                            + "."));

            permissionEntity.setApplication(application);
        }

        return permissionRepository.save(permissionEntity);
    }

    /**
     * Fetches a list of all permissions that exist.
     *
     * @return a list of all permissions.
     */
    public List<PermissionEntity> getAllPermissions() {
        return permissionRepository.findAll();
    }

    /**
     * Fetches all active permissions.
     *
     * @return a list of active permissions.
     * */
    public List<PermissionEntity> getAllActivePermissions() {
        return permissionRepository.findAllActive();
    }

    /**
     * Deletes a permission by its ID.
     *
     * @param permissionId is an id of a permission.
     */
    public void deletePermissionById(UUID permissionId) {
        PermissionEntity permissionEntity =
                this.getPermissionById(permissionId)
                        .orElseThrow(
                                () ->
                                        new ResourceNotFoundException(
                                                "There is no permission that exists with this"
                                                        + " ID."));

        permissionRepository.delete(permissionEntity);
    }

    /**
     * Returns a permission by its ID.
     *
     * @param permissionId is a permission's id
     * @return a permission, if one exists.
     */
    public Optional<PermissionEntity> getPermissionById(UUID permissionId) {
        return permissionRepository.findById(permissionId);
    }
}
