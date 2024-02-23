package io.nuvalence.user.management.api.service.mapper;

import io.nuvalence.user.management.api.service.entity.AgencyUser;
import io.nuvalence.user.management.api.service.entity.PublicUser;
import io.nuvalence.user.management.api.service.entity.UserEntity;
import io.nuvalence.user.management.api.service.entity.profile.IndividualProfile;
import io.nuvalence.user.management.api.service.enums.UserType;
import io.nuvalence.user.management.api.service.generated.models.UserCreationRequest;
import io.nuvalence.user.management.api.service.generated.models.UserDTO;
import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Maps transaction definitions between UserEntity and User.
 */
@Mapper(componentModel = "spring")
@AllArgsConstructor
@NoArgsConstructor
public abstract class UserEntityMapper implements LazyLoadingAwareMapper {
    protected IndividualProfileMapper individualProfileMapper =
            Mappers.getMapper(IndividualProfileMapper.class);

    /**
     * Maps {@link UserEntity} to
     * {@link UserDTO}.
     *
     * @param userEntity an entity
     * @return user model
     */
    @Mapping(target = "preferences", source = "userPreference")
    @Mapping(
            target = "assignedRoles",
            expression = "java(MapperUtils.mapUserEntityToAssignedRoleList(userEntity))")
    @Mapping(target = "displayName", expression = "java(createDisplayName(userEntity))")
    public abstract UserDTO entityToModel(UserEntity userEntity);

    /**
     * Maps {@link UserEntity} to
     * {@link UserDTO}.
     *
     * @param userEntity an entity
     * @param appRoles   optional application roles
     * @return user model
     */
    public UserDTO entityToModelWithAppRoles(
            UserEntity userEntity, @Nullable List<String> appRoles) {
        UserDTO userDTO = entityToModel(userEntity);
        userDTO.applicationRoles(appRoles);
        return userDTO;
    }

    /**
     * Converts a string to a UserType.
     *
     * @param userType user type string
     * @return UserType enum instance.
     */
    public UserType convertStringToUserType(String userType) {
        return UserType.fromText(userType);
    }

    /**
     * Convert a UserType to a string.
     *
     * @param userType UserType enum
     * @return string value for user type
     */
    public String convertUserToTypeString(UserType userType) {
        if (userType == null) {
            return null;
        }
        return userType.toString();
    }

    /**
     * create display name for user.
     *
     * @param user user Entity
     * @return string value for the display name
     */
    public String createDisplayName(UserEntity user) {
        String firstName = user.getFirstName();
        String middleName = user.getMiddleName();
        String lastName = user.getLastName();

        if (user instanceof PublicUser) {
            PublicUser publicUser = (PublicUser) user;
            if (publicUser.getProfile() != null) {
                firstName = publicUser.getProfile().getFirstName();
                middleName = publicUser.getProfile().getMiddleName();
                lastName = publicUser.getProfile().getLastName();
            }
        }

        String displayName =
                Stream.of(firstName, middleName, lastName)
                        .filter(Objects::nonNull)
                        .collect(Collectors.joining(" "))
                        .replaceAll("\\s{2,}", " ")
                        .trim();

        if (displayName.isEmpty()) {
            displayName = user.getEmail() != null ? user.getEmail() : String.valueOf(user.getId());
        }

        return displayName;
    }

    /**
     * Converts a {@link UserCreationRequest} to a {@link UserEntity} and applies additional
     * configuration based on the user type. This unified approach directly incorporates the logic
     * for instantiating user entities based on specific types and applies any necessary configurations,
     * including setting up an IndividualProfile for PublicUser instances.
     *
     * @param userCreationRequest The request to create a user, containing all necessary data.
     * @return A fully configured UserEntity, ready for further processing or persistence.
     */
    public UserEntity convertUserCreationRequestToUserEntity(
            UserCreationRequest userCreationRequest) {
        switch (userCreationRequest.getUserType()) {
            case "public":
                PublicUser publicUser = new PublicUser();
                publicUser.setExternalId(userCreationRequest.getExternalId());
                publicUser.setIdentityProvider(userCreationRequest.getIdentityProvider());
                publicUser.setUserType(UserType.PUBLIC);
                publicUser.setDeleted(false);

                if (userCreationRequest.getProfile() != null) {
                    IndividualProfile individualProfile =
                            individualProfileMapper.createModelToIndividual(
                                    userCreationRequest.getProfile());
                    publicUser.setProfile(individualProfile);
                }

                return publicUser;

            case "agency":
                AgencyUser agencyUser = new AgencyUser();
                agencyUser.setExternalId(userCreationRequest.getExternalId());
                agencyUser.setIdentityProvider(userCreationRequest.getIdentityProvider());
                agencyUser.setUserType(UserType.AGENCY);
                agencyUser.setDeleted(false);

                return agencyUser;

            default:
                throw new IllegalArgumentException(
                        "Unsupported user type: " + userCreationRequest.getUserType());
        }
    }

    @AfterMapping
    void adjustUserDtoForSubtype(@MappingTarget UserDTO userDTO, UserEntity userEntity) {
        if (userEntity instanceof PublicUser) {
            PublicUser publicUser = (PublicUser) userEntity;

            userDTO.setUserType(UserType.PUBLIC.getValue());
            if (publicUser.getProfile() != null) {
                userDTO.setProfile(
                        Mappers.getMapper(IndividualProfileMapper.class)
                                .individualToResponseModel(publicUser.getProfile()));
            }
        }
    }
}
