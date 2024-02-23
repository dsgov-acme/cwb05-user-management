package io.nuvalence.user.management.api.service.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.nuvalence.user.management.api.service.entity.ApplicationEntity;
import io.nuvalence.user.management.api.service.entity.PermissionEntity;
import io.nuvalence.user.management.api.service.generated.models.PermissionDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

@ExtendWith(MockitoExtension.class)
class PermissionEntityMapperTest {

    @Test
    void shouldMapPermissionEntityToPermissionDto() {
        PermissionEntity permission = new PermissionEntity();
        permission.setId(UUID.randomUUID());
        permission.setName("test_perm");
        permission.setDescription("This is a test permission.");

        ApplicationEntity application = new ApplicationEntity();
        application.setId(UUID.randomUUID());
        application.setName("APPLICATION_1");

        application.setPermissions(List.of(permission));
        permission.setApplication(application);
        permission.setApplicationId(application.getId());

        PermissionDTO permissionModel =
                PermissionEntityMapper.INSTANCE.permissionEntityToPermissionDto(permission);
        assertEquals(permission.getId(), permissionModel.getId());
        assertEquals(permission.getName(), permissionModel.getName());
        assertEquals(permission.getDescription(), permissionModel.getDescription());
    }
}
