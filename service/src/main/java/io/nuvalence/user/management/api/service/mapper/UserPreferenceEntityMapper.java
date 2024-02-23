package io.nuvalence.user.management.api.service.mapper;

import io.nuvalence.user.management.api.service.entity.UserPreferenceEntity;
import io.nuvalence.user.management.api.service.generated.models.UserPreferenceDTO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * Maps transaction definitions between UserEntity and User.
 */
@Mapper(componentModel = "spring")
public interface UserPreferenceEntityMapper extends LazyLoadingAwareMapper {
    UserPreferenceEntityMapper INSTANCE = Mappers.getMapper(UserPreferenceEntityMapper.class);

    /**
     * Maps {@link UserPreferenceEntity} to
     * {@link UserPreferenceDTO}.
     *
     * @param userPreferenceEntity an entity
     * @return user model
     */
    UserPreferenceDTO convertUserPreferenceEntityToUserModel(
            UserPreferenceEntity userPreferenceEntity);
}
