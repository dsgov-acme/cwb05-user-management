package io.nuvalence.user.management.api.service.mapper;

import io.nuvalence.user.management.api.service.entity.ApplicationEntity;
import io.nuvalence.user.management.api.service.generated.models.ApplicationDTO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * Maps definitions between ApplicationEntity and ApplicationDTO.
 */
@Mapper
public interface ApplicationEntityMapper {
    ApplicationEntityMapper INSTANCE = Mappers.getMapper(ApplicationEntityMapper.class);

    /**
     * Maps an Application entity to an Application DTO.
     *
     * @param application Application as an entity.
     * @return Application DTO.
     */
    ApplicationDTO applicationEntityToApplicationDto(ApplicationEntity application);
}
