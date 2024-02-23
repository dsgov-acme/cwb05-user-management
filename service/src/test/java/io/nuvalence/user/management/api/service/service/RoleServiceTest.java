package io.nuvalence.user.management.api.service.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.nuvalence.user.management.api.service.config.exception.BusinessLogicException;
import io.nuvalence.user.management.api.service.config.exception.ResourceNotFoundException;
import io.nuvalence.user.management.api.service.entity.ApplicationEntity;
import io.nuvalence.user.management.api.service.entity.PermissionEntity;
import io.nuvalence.user.management.api.service.entity.RoleEntity;
import io.nuvalence.user.management.api.service.enums.UserType;
import io.nuvalence.user.management.api.service.generated.models.RoleUpsertRequest;
import io.nuvalence.user.management.api.service.repository.PermissionRepository;
import io.nuvalence.user.management.api.service.repository.RoleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ExtendWith(MockitoExtension.class)
class RoleServiceTest {

    private final String testUuid = "ad00dbd6-f7dc-11ec-b939-0242ac120002";

    @Mock private RoleRepository roleRepository;

    @Mock private PermissionRepository permissionRepository;

    @InjectMocks private RoleService roleService;

    @Captor private ArgumentCaptor<RoleEntity> roleCaptor;

    // Add role tests
    @Test
    void addRole_addsRoleIfValid() {
        ApplicationEntity applicationEntity = createApplicationEntity();
        applicationEntity.setId(UUID.fromString(testUuid));

        PermissionEntity perm = new PermissionEntity();
        perm.setName("Valid_Permission");
        perm.setApplicationRole("TransactionManager_viewAll");
        perm.setApplicationId(applicationEntity.getId());

        when(permissionRepository.findAllByApplicationRole(any())).thenReturn(List.of(perm));

        RoleUpsertRequest roleCreationRequest = createRoleCreationRequest();
        roleService.upsertRole(roleCreationRequest);

        verify(roleRepository).save(roleCaptor.capture());
        RoleEntity roleCaptured = roleCaptor.getValue();

        assertEquals(roleCreationRequest.getName(), roleCaptured.getName());
    }

    @Test
    void addRole_fails_ifRoleExists() {
        RoleUpsertRequest role = createRoleCreationRequest();

        when(roleRepository.findByName(role.getName())).thenReturn(Optional.of(new RoleEntity()));

        Exception exception =
                assertThrows(BusinessLogicException.class, () -> roleService.upsertRole(role));
        assertEquals("This role already exists.", exception.getMessage());
    }

    // Update role tests
    @Test
    void updateRole_updatesRoleIfValid() {
        RoleEntity roleEntity = createRoleEntity();

        when(roleRepository.findById(roleEntity.getId()))
                .thenReturn(Optional.of(createRoleEntity()));

        ApplicationEntity applicationEntity = createApplicationEntity();
        applicationEntity.setId(UUID.fromString(testUuid));

        PermissionEntity permissionEntity = createPermissionEntity();
        permissionEntity.setApplication(applicationEntity);
        permissionEntity.setName("Invalid Permission Name");
        permissionEntity.setApplicationRole("Invalid_Permission");

        RoleUpsertRequest roleUpdateRequest = new RoleUpsertRequest();
        roleUpdateRequest.setId(roleEntity.getId());
        roleUpdateRequest.setName(permissionEntity.getName());
        roleUpdateRequest.setPermissions(List.of(permissionEntity.getApplicationRole()));

        PermissionEntity perm = new PermissionEntity();
        perm.setName("Invalid Permission Name");
        perm.setApplicationRole("Invalid_Permission");

        when(permissionRepository.findAllByApplicationRole(any())).thenReturn(List.of(perm));
        roleService.upsertRole(roleUpdateRequest);

        verify(roleRepository).save(roleCaptor.capture());
        RoleEntity updatedRole = roleCaptor.getValue();

        assertNotNull(updatedRole);
    }

    @Test
    void updateRole_fails_ifInvalidRoleId() {
        RoleEntity roleEntity = createRoleEntity();
        RoleUpsertRequest roleUpdateRequest = new RoleUpsertRequest();
        roleUpdateRequest.setId(roleEntity.getId());

        when(roleRepository.findById(roleEntity.getId())).thenReturn(Optional.empty());

        Exception exception =
                assertThrows(
                        BusinessLogicException.class,
                        () -> roleService.upsertRole(roleUpdateRequest));
        assertEquals("This role does not exist.", exception.getMessage());
    }

    @Test
    void updateRole_fails_ifInvalidPermission() {
        RoleEntity roleEntity = createRoleEntity();

        when(roleRepository.findById(roleEntity.getId()))
                .thenReturn(Optional.of(createRoleEntity()));

        RoleUpsertRequest roleUpdateRequest = new RoleUpsertRequest();
        roleUpdateRequest.setId(roleEntity.getId());
        roleUpdateRequest.setName("Updated role.");
        roleUpdateRequest.setPermissions(List.of("FAKE_PERM"));

        ApplicationEntity applicationEntity = createApplicationEntity();
        applicationEntity.setId(UUID.fromString(testUuid));

        Exception exception =
                assertThrows(
                        BusinessLogicException.class,
                        () -> roleService.upsertRole(roleUpdateRequest));

        assertEquals("The provided permission 'FAKE_PERM' is invalid.", exception.getMessage());
    }

    // Delete role tests
    @Test
    void deleteRoleById_deletesRoleIfValid() {
        RoleEntity role = createRoleEntity();

        when(roleRepository.findById(role.getId())).thenReturn(Optional.of(role));

        roleService.deleteRoleById(role.getId(), "default_resource");

        verify(roleRepository).delete(role);
    }

    @Test
    void deleteRoleById_fails_ifThatRoleDoesNotExist() {
        RoleEntity role = createRoleEntity();

        assertThrowsWithMessage(() -> roleService.deleteRoleById(role.getId(), "default_resource"));
    }

    @Test
    void upsertDefaultRoles_savesRolesIfValid() {
        String userType = "agency";
        List<UUID> roleIds = List.of(UUID.randomUUID(), UUID.randomUUID());

        when(roleRepository.findById(any())).thenReturn(Optional.of(createRoleEntity()));
        when(roleRepository.save(any())).thenReturn(createRoleEntity());

        List<UUID> savedRoleIds = roleService.upsertDefaultRoles(userType, roleIds);

        verify(roleRepository, times(2)).save(any(RoleEntity.class));

        assertEquals(roleIds.size(), savedRoleIds.size());
    }

    @Test
    void upsertDefaultRoles_deletesRolesIfEmpty() {
        String userType = "agency";
        List<UUID> roleIds = Collections.emptyList();

        List<UUID> savedRoleIds = roleService.upsertDefaultRoles(userType, roleIds);

        assertTrue(savedRoleIds.isEmpty());
    }

    @Test
    void getDefaultRoles_returnsDefaultRoles() {
        String userType = "public";

        when(roleRepository.findAllByUserType(UserType.fromText(userType)))
                .thenReturn(List.of(createRoleEntity(), createRoleEntity()));

        List<UUID> defaultRoles =
                roleService.getDefaultRoles(userType).stream().map(RoleEntity::getId).toList();

        assertEquals(2, defaultRoles.size());
    }

    @Test
    void deleteDefaultRoles_deletesDefaultRoles() {
        String userType = "public";

        RoleEntity roleEntity = createRoleEntity();
        roleEntity.setDefaultUserTypes(new HashSet<>(List.of(UserType.fromText(userType))));

        when(roleRepository.findAllByUserType(UserType.fromText(userType)))
                .thenReturn(List.of(createRoleEntity(), roleEntity));

        roleService.deleteDefaultRoles(userType);

        verify(roleRepository, times(2)).save(any(RoleEntity.class));
    }

    private void assertThrowsWithMessage(Executable executable) {
        Throwable thrownException =
                assertThrows(
                        (Class<? extends Throwable>) ResourceNotFoundException.class, executable);
        assertEquals("There is no role that exists with this id.", thrownException.getMessage());
    }

    // Helpers
    private RoleEntity createRoleEntity() {
        RoleEntity role = new RoleEntity();
        role.setName("ROLE_TO_TEST");
        role.setId(UUID.fromString("af102616-4207-4850-adc4-0bf91058a261"));
        role.setDefaultUserTypes(new HashSet<>());
        return role;
    }

    private RoleUpsertRequest createRoleCreationRequest() {
        RoleUpsertRequest role = new RoleUpsertRequest();
        role.setName("COMPLAINANT");
        role.setPermissions(List.of("TransactionManager_viewAll"));
        return role;
    }

    private PermissionEntity createPermissionEntity() {
        PermissionEntity entity = new PermissionEntity();
        entity.setName("Test Permission");
        entity.setId(UUID.randomUUID());
        return entity;
    }

    private ApplicationEntity createApplicationEntity() {
        ApplicationEntity entity = new ApplicationEntity();
        entity.setName("test_application");
        entity.setDisplayName("Test Application");
        entity.setId(UUID.randomUUID());
        return entity;
    }
}
