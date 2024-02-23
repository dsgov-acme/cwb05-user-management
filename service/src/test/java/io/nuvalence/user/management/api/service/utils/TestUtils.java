package io.nuvalence.user.management.api.service.utils;

import io.nuvalence.user.management.api.service.entity.PermissionEntity;
import io.nuvalence.user.management.api.service.generated.models.CreateOrUpdatePermissionDTO;

import java.util.UUID;

/**
 * Utility methods for testing.
 */
@SuppressWarnings("PMD")
public class TestUtils {
    /**
     * Creates a mock CreateOrUpdatePermissionDTO for testing purposes.
     *
     * @return the generated object.
     */
    public static CreateOrUpdatePermissionDTO createOrUpdatePermissionModel() {
        CreateOrUpdatePermissionDTO permission = new CreateOrUpdatePermissionDTO();
        permission.setName("test_perm");
        permission.setDisplayName("Test Permission");
        permission.setDescription("This is a test permission.");
        return permission;
    }

    /**
     * Creates a mock PermissionEntity for testing purposes.
     *
     * @return the generated object.
     */
    public static PermissionEntity createMockPermission() {
        PermissionEntity permission = new PermissionEntity();
        permission.setId(UUID.randomUUID());
        permission.setName("test_perm");
        permission.setDescription("This is a test permission.");
        return permission;
    }
}
