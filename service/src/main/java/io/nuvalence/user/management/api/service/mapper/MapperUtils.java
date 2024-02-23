package io.nuvalence.user.management.api.service.mapper;

import io.nuvalence.user.management.api.service.entity.PermissionEntity;
import io.nuvalence.user.management.api.service.entity.RoleEntity;
import io.nuvalence.user.management.api.service.entity.UserEntity;
import io.nuvalence.user.management.api.service.generated.models.AssignedRoleDTO;
import io.nuvalence.user.management.api.service.generated.models.PermissionDTO;
import io.nuvalence.user.management.api.service.generated.models.RoleDTO;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.List;

/**
 * Simple Mapping utility class.
 */
@Slf4j
public class MapperUtils {

    private MapperUtils() {
        // private constructor
    }

    /**
     * Maps Permissions from entities to models.
     *
     * @param permissionEntities Permissions entities
     * @return Permission models
     */
    public static List<PermissionDTO> mapPermissionEntitiesToPermissionList(
            List<PermissionEntity> permissionEntities) {
        return permissionEntities.stream()
                .map(PermissionEntityMapper.INSTANCE::permissionEntityToPermissionDto)
                .toList();
    }

    /**
     * Simple list mapper for entity -> dto.
     *
     * @param roleEntities list of role entities
     * @return list of role dto
     */
    public static List<RoleDTO> mapRoleEntitiesToRoleList(List<RoleEntity> roleEntities) {
        return roleEntities.stream().map(RoleEntityMapper.INSTANCE::roleEntityToRoleDto).toList();
    }

    /**
     * Simple list mapper for RoleEntities -> AssignedRoleDTOs.
     *
     * @param roleEntities list of RoleEntities
     * @return list of AssignedRoleDTOs
     */
    public static List<AssignedRoleDTO> mapRoleEntitiesToAssignedRoleList(
            List<RoleEntity> roleEntities) {
        if (roleEntities == null) {
            return Collections.emptyList();
        }

        return roleEntities.stream()
                .map(RoleEntityMapper.INSTANCE::roleEntityToAssignedRoleDto)
                .toList();
    }

    /**
     * Simple list mapper for UserEntities to a list of AssignedRoleDTOs.
     * @param user a UserEntity
     * @return a list of AssignedRoleDTOs
     */
    public static List<AssignedRoleDTO> mapUserEntityToAssignedRoleList(UserEntity user) {
        return mapRoleEntitiesToAssignedRoleList(user.getRoles());
    }
}
