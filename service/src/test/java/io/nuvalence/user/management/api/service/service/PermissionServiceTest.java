package io.nuvalence.user.management.api.service.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.nuvalence.user.management.api.service.config.exception.BusinessLogicException;
import io.nuvalence.user.management.api.service.config.exception.ResourceNotFoundException;
import io.nuvalence.user.management.api.service.entity.ApplicationEntity;
import io.nuvalence.user.management.api.service.entity.PermissionEntity;
import io.nuvalence.user.management.api.service.generated.models.CreateOrUpdatePermissionDTO;
import io.nuvalence.user.management.api.service.repository.ApplicationRepository;
import io.nuvalence.user.management.api.service.repository.PermissionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@ExtendWith(MockitoExtension.class)
class PermissionServiceTest {
    @Mock private PermissionRepository permissionRepository;

    @Mock private ApplicationRepository applicationRepository;

    @Mock private ApplicationService applicationService;

    @Captor private ArgumentCaptor<PermissionEntity> permissionCaptor;

    @Captor private ArgumentCaptor<UUID> idCaptor;

    @InjectMocks private PermissionService permissionService;

    @Test
    void addPermission_creates_a_permission() {
        ApplicationEntity application = createApplicationEntity();
        CreateOrUpdatePermissionDTO permissionModel = createOrUpdatePermissionRequest();
        permissionModel.setApplication(application.getId());

        when(applicationService.getApplicationById(any())).thenReturn(Optional.of(application));

        permissionService.addPermission(permissionModel);

        verify(permissionRepository).save(permissionCaptor.capture());
        PermissionEntity savedPermission = permissionCaptor.getValue();
        assertTrue(savedPermission.getName().equalsIgnoreCase(permissionModel.getName()));
        assertEquals(savedPermission.getName(), permissionModel.getName());
        assertEquals(savedPermission.getDescription(), permissionModel.getDescription());
    }

    @Test
    void addPermission_fails_ifNameIsTaken() {
        CreateOrUpdatePermissionDTO permissionModel = createOrUpdatePermissionRequest();
        PermissionEntity permissionEntity = createPermissionEntity();

        when(permissionRepository.findByApplicationRole(permissionModel.getApplicationRole()))
                .thenReturn(Optional.of(permissionEntity));

        Exception exception =
                assertThrows(
                        BusinessLogicException.class,
                        () -> permissionService.addPermission(permissionModel));

        System.out.println(exception.getMessage());
        assertTrue(exception.getMessage().contains("This permission already exists."));
    }

    @Test
    void addPermission_fails_ifApplicationDoesNotExist() {
        CreateOrUpdatePermissionDTO permissionModel = createOrUpdatePermissionRequest();

        when(applicationService.getApplicationById(any())).thenReturn(Optional.empty());
        when(permissionRepository.findByApplicationRole(any())).thenReturn(Optional.empty());

        Exception exception =
                assertThrows(
                        ResourceNotFoundException.class,
                        () -> permissionService.addPermission(permissionModel));
        assertTrue(
                exception
                        .getMessage()
                        .contains(
                                String.format(
                                        "Application with ID %s not found.",
                                        permissionModel.getApplication().toString())));
    }

    @Test
    void updatePermission_updates_a_permission() {
        CreateOrUpdatePermissionDTO permissionModel = createOrUpdatePermissionRequest();
        PermissionEntity permission = createPermissionEntity();
        permission.setApplication(createApplicationEntity());

        // make the model app list the same as what the entity already has associated to it
        permissionModel.setApplication(permission.getApplication().getId());

        when(permissionRepository.findById(any())).thenReturn(Optional.of(permission));
        when(applicationService.getApplicationById(any()))
                .thenReturn(Optional.of(permission.getApplication()));

        permissionService.updatePermission(UUID.randomUUID(), permissionModel);

        verify(permissionRepository).save(permissionCaptor.capture());

        PermissionEntity savedPermission = permissionCaptor.getValue();
        assertTrue(savedPermission.getName().equalsIgnoreCase(permissionModel.getName()));
        assertEquals(savedPermission.getName(), permissionModel.getName());
        assertEquals(savedPermission.getDescription(), permissionModel.getDescription());
    }

    @Test
    void updatePermission_updates_a_permission_and_adds_removes_applications() {
        CreateOrUpdatePermissionDTO permissionModel = createOrUpdatePermissionRequest();

        // make the model app list different from what's there to force both an insert and a
        // deletion to happen
        permissionModel.setApplication(UUID.randomUUID());
        PermissionEntity permission = createPermissionEntity();

        ApplicationEntity applicationEntity = createApplicationEntity();
        applicationEntity.setId(permissionModel.getApplication());

        permission.setApplication(applicationEntity);

        when(permissionRepository.findById(any())).thenReturn(Optional.of(permission));
        when(applicationService.getApplicationById(any()))
                .thenReturn(Optional.of(applicationEntity));

        permissionService.updatePermission(UUID.randomUUID(), permissionModel);

        verify(permissionRepository).save(permissionCaptor.capture());
        PermissionEntity savedPermission = permissionCaptor.getValue();
        assertTrue(savedPermission.getName().equalsIgnoreCase(permissionModel.getName()));
        assertEquals(savedPermission.getName(), permissionModel.getName());
        assertEquals(savedPermission.getDescription(), permissionModel.getDescription());
    }

    @Test
    void updatePermission_fails_if_permissionDoesNotExist() {
        CreateOrUpdatePermissionDTO permissionModel = createOrUpdatePermissionRequest();
        when(permissionRepository.findById(any())).thenReturn(Optional.empty());

        assertThrowsWithMessage(
                BusinessLogicException.class,
                "This permission does not exist.",
                () -> permissionService.updatePermission(UUID.randomUUID(), permissionModel));
    }

    private void assertThrowsWithMessage(
            Class<? extends Throwable> expectedException,
            String expectedMessage,
            Executable executable) {
        Throwable thrownException = assertThrows(expectedException, executable);
        assertTrue(thrownException.getMessage().contains(expectedMessage));
    }

    @Test
    void updatePermission_fails_if_applicationDoesNotExist() {
        CreateOrUpdatePermissionDTO permissionModel = createOrUpdatePermissionRequest();
        PermissionEntity permissionEntity = createPermissionEntity();

        when(permissionRepository.findById(any())).thenReturn(Optional.of(permissionEntity));

        assertThrowsWithMessage(
                ResourceNotFoundException.class,
                String.format(
                        "Application not found with ID %s.",
                        permissionModel.getApplication().toString()),
                () -> permissionService.updatePermission(UUID.randomUUID(), permissionModel));
    }

    @Test
    void getAllPermissions_gets_permissions() {
        PermissionEntity permissionEntity = createPermissionEntity();
        permissionEntity.setApplication(createApplicationEntity());

        when(permissionRepository.findAll()).thenReturn(List.of(permissionEntity));

        List<PermissionEntity> res = permissionService.getAllPermissions();

        assertEquals(1, Objects.requireNonNull(res).size());
        assertEquals(permissionEntity.getName(), res.get(0).getName());
        assertEquals(permissionEntity.getDescription(), res.get(0).getDescription());

        assertEquals(
                permissionEntity.getApplication().getName(), res.get(0).getApplication().getName());
        assertEquals(
                permissionEntity.getApplication().getName(), res.get(0).getApplication().getName());

        verify(permissionRepository).findAll();
    }

    @Test
    void deletePermissionById_deletes_permission() {
        PermissionEntity permissionEntity = createPermissionEntity();
        permissionEntity.setApplication(createApplicationEntity());

        when(permissionRepository.findById(any())).thenReturn(Optional.of(permissionEntity));

        permissionService.deletePermissionById(UUID.randomUUID());

        verify(permissionRepository).delete(permissionCaptor.capture());
    }

    @Test
    void deletePermissionById_fails_if_permissionDoesNotExist() {
        when(permissionRepository.findById(any())).thenReturn(Optional.empty());

        assertThrowsWithMessage(
                ResourceNotFoundException.class,
                "There is no permission that exists with this ID.",
                () -> permissionService.deletePermissionById(UUID.randomUUID()));
    }

    @Test
    void getPermissionById_gets_permission() {
        PermissionEntity permissionEntity = createPermissionEntity();
        permissionEntity.setApplication(createApplicationEntity());

        when(permissionRepository.findById(any())).thenReturn(Optional.of(permissionEntity));

        PermissionEntity res =
                permissionService.getPermissionById(permissionEntity.getId()).orElseThrow();

        verify(permissionRepository).findById(idCaptor.capture());
        assertEquals(Objects.requireNonNull(res).getId(), idCaptor.getValue());
    }

    @Test
    void getPermissionById_fails_if_permissionDoesNotExist() {
        when(permissionRepository.findById(any())).thenReturn(Optional.empty());

        assertTrue(permissionService.getPermissionById(UUID.randomUUID()).isEmpty());
    }

    private PermissionEntity createPermissionEntity() {
        PermissionEntity permissionEntity = new PermissionEntity();
        permissionEntity.setId(UUID.randomUUID());
        permissionEntity.setApplicationRole("test_perm");
        permissionEntity.setName("Test Permission");
        permissionEntity.setDescription("This is a test permission.");
        return permissionEntity;
    }

    private CreateOrUpdatePermissionDTO createOrUpdatePermissionRequest() {
        CreateOrUpdatePermissionDTO permission = new CreateOrUpdatePermissionDTO();
        permission.setName("test_perm");
        permission.setApplicationRole("test_perm");
        permission.setDisplayName("Test Permission");
        permission.setDescription("This is a test permission.");
        permission.setApplication(UUID.randomUUID());
        return permission;
    }

    private ApplicationEntity createApplicationEntity() {
        ApplicationEntity applicationEntity = new ApplicationEntity();
        applicationEntity.setId(UUID.randomUUID());
        applicationEntity.setName("APPLICATION_1");
        return applicationEntity;
    }
}
