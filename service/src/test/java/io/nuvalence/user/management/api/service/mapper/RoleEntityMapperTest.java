package io.nuvalence.user.management.api.service.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.nuvalence.user.management.api.service.entity.RoleEntity;
import io.nuvalence.user.management.api.service.generated.models.AssignedRoleDTO;
import io.nuvalence.user.management.api.service.generated.models.RoleDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

@ExtendWith(MockitoExtension.class)
class RoleEntityMapperTest {

    @Test
    void shouldMapRoleEntityToRoleDto() {
        RoleEntity role = new RoleEntity();
        role.setName("ROLE_TO_TEST");
        role.setId(UUID.fromString("af102616-4207-4850-adc4-0bf91058a261"));

        RoleDTO roleModel = RoleEntityMapper.INSTANCE.roleEntityToRoleDto(role);
        assertEquals(role.getName(), roleModel.getName());
        assertEquals(role.getId(), roleModel.getId());
    }

    @Test
    void shouldMapRoleDtoToRoleEntity() {
        RoleDTO role = new RoleDTO();
        role.setName("ROLE_TO_TEST");
        role.setId(UUID.fromString("af102616-4207-4850-adc4-0bf91058a261"));

        RoleEntity roleEntity = RoleEntityMapper.INSTANCE.roleDtoToRoleEntity(role);
        assertEquals(role.getName(), roleEntity.getName());
        assertEquals(role.getId(), roleEntity.getId());
    }

    @Test
    void shouldMapRoleEntityToAssignedRoleDTO() {
        RoleEntity role = new RoleEntity();
        role.setName("ROLE_TO_TEST");
        role.setId(UUID.fromString("af102616-4207-4850-adc4-0bf91058a261"));

        AssignedRoleDTO roleModel = RoleEntityMapper.INSTANCE.roleEntityToAssignedRoleDto(role);
        assertEquals(role.getName(), roleModel.getRoleName());
        assertEquals(role.getDescription(), roleModel.getDescription());
    }
}
