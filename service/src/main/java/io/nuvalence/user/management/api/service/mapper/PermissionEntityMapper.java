package io.nuvalence.user.management.api.service.mapper;

import io.nuvalence.user.management.api.service.entity.PermissionEntity;
import io.nuvalence.user.management.api.service.generated.models.PermissionDTO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * Maps between PermissionEntity and Permission.
 */
@Mapper
public interface PermissionEntityMapper {

    PermissionEntityMapper INSTANCE = Mappers.getMapper(PermissionEntityMapper.class);

    /**
     * Converts a {@link PermissionEntity}
     * to {@link io.nuvalence.user.management.api.service.generated.models.PermissionDTO}.
     *
     * @param permissionEntity Permission in Entity form
     * @return Permission DTO
     */
    default PermissionDTO permissionEntityToPermissionDto(PermissionEntity permissionEntity) {
        return new PermissionDTO()
                .id(permissionEntity.getId())
                .name(permissionEntity.getName())
                .description(permissionEntity.getDescription())
                .group(permissionEntity.getGroup())
                .applicationRole(permissionEntity.getApplicationRole());
    }

    /**
     * Converts a {@link io.nuvalence.user.management.api.service.generated.models.PermissionDTO} to
     * {@link PermissionEntity}.
     *
     * @param permission Permission in DTO form
     * @return Permission Entity
     */
    PermissionEntity permissionDtoToPermissionEntity(PermissionDTO permission);
}
