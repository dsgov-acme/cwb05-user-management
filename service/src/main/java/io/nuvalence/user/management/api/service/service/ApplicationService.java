package io.nuvalence.user.management.api.service.service;

import io.nuvalence.events.event.dto.ApplicationRole;
import io.nuvalence.user.management.api.service.entity.ApplicationEntity;
import io.nuvalence.user.management.api.service.entity.PermissionEntity;
import io.nuvalence.user.management.api.service.repository.ApplicationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Service for retrieving applications.
 */
@Component
@Transactional
@RequiredArgsConstructor
public class ApplicationService {
    private final ApplicationRepository applicationRepository;

    /**
     * Returns a list of all applications.
     * @return list of applications
     */
    public List<ApplicationEntity> getApplications() {
        return applicationRepository.findAll();
    }

    /**
     * Returns an application by its id.
     *
     * @param id the id of the application.
     * @return an application.
     */
    public Optional<ApplicationEntity> getApplicationById(UUID id) {
        return applicationRepository.findById(id);
    }

    /**
     * Updates permissions for an application.
     *
     * @param name application name
     * @param newPermissions updated permissions
     */
    public void setApplicationRoles(String name, List<ApplicationRole> newPermissions) {
        // Either fetch an existing application, or create a new one.
        ApplicationEntity application =
                applicationRepository.getApplicationByName(name).orElse(new ApplicationEntity());

        application.setName(name);

        // In case we're creating a new application.
        if (application.getPermissions() == null) {
            application.setPermissions(new ArrayList<>());
            application.setDisplayName(name);
        }

        Map<String, PermissionEntity> permissions = new HashMap<>();

        // Mark all permissions as inactive; if they're in the request's permission list, then it'll
        // be marked as active again.
        for (PermissionEntity permission : application.getPermissions()) {
            permission.setActive(false);
            permissions.put(permission.getApplicationRole(), permission);
        }

        for (ApplicationRole newPermission : newPermissions) {
            // Check if the permission already exists for this application.
            PermissionEntity permission = permissions.get(newPermission.getApplicationRole());
            boolean isNewPermission = false;

            // Permission does not exist; create a new one.
            if (permission == null) {
                permission = new PermissionEntity();
                permission.setApplication(application);
                permission.setApplicationId(application.getId());

                isNewPermission = true;
            }

            permission.setActive(true);
            permission.setApplicationRole(newPermission.getApplicationRole());
            permission.setDescription(newPermission.getDescription());
            permission.setName(newPermission.getName());
            permission.setGroup(newPermission.getGroup());

            // In case we're not modifying an existing permission, add it to the of permissions. If
            // we're just modifying a permission then it already exists.
            if (isNewPermission) {
                application.getPermissions().add(permission);
            }
        }

        applicationRepository.save(application);
    }
}
