package io.nuvalence.user.management.api.service.mapper;

import io.nuvalence.user.management.api.service.entity.PermissionEntity;
import io.nuvalence.user.management.api.service.entity.RoleEntity;
import io.nuvalence.user.management.api.service.generated.models.AssignedRoleDTO;
import io.nuvalence.user.management.api.service.generated.models.RoleDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import java.util.Collections;
import java.util.List;

/**
 * Maps transaction definitions between RoleEntity and Role.
 */
@Mapper
public interface RoleEntityMapper {

    RoleEntityMapper INSTANCE = Mappers.getMapper(RoleEntityMapper.class);

    /**
     * Converts a Role Entity into a Role DTO.
     *
     * @param roleEntity Role in Entity form
     * @return Role DTO
     */
    @Mapping(
            source = "permissions",
            target = "permissions",
            qualifiedByName = "mapPermissionsByName")
    RoleDTO roleEntityToRoleDto(RoleEntity roleEntity);

    /**
     * Maps a list of permissions into a list of names.
     *
     * @param permissions List of permissions.
     * @return mapped permissions
     */
    @Named("mapPermissionsByName")
    default List<String> mapPermissionsByName(List<PermissionEntity> permissions) {
        if (permissions == null) {
            return Collections.emptyList();
        }

        return permissions.stream().map(PermissionEntity::getApplicationRole).toList();
    }

    /**
     * Converts a Role Entity into an Assigned Role DTO.
     *
     * @param roleEntity Role in Entity form
     * @return Assigned Role DTO
     */
    @Mapping(target = "roleName", source = "roleEntity.name")
    AssignedRoleDTO roleEntityToAssignedRoleDto(RoleEntity roleEntity);

    /**
     * Converts a Role Entity into a Role DTO.
     *
     * @param role Role in DTO form
     * @return Role Entity.
     */
    @Mapping(target = "permissions", ignore = true)
    @Mapping(target = "users", ignore = true)
    RoleEntity roleDtoToRoleEntity(RoleDTO role);
}
