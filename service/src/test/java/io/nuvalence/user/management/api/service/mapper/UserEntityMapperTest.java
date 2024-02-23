package io.nuvalence.user.management.api.service.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.nuvalence.user.management.api.service.entity.AgencyUser;
import io.nuvalence.user.management.api.service.entity.PublicUser;
import io.nuvalence.user.management.api.service.entity.RoleEntity;
import io.nuvalence.user.management.api.service.entity.UserEntity;
import io.nuvalence.user.management.api.service.enums.UserType;
import io.nuvalence.user.management.api.service.generated.models.UserCreationRequest;
import io.nuvalence.user.management.api.service.generated.models.UserDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
class UserEntityMapperTest {

    private static final String FIRST_NAME = "John";
    private static final String MIDDLE_NAME = "Locke";
    private static final String LAST_NAME = "Doe";

    @Test
    void testMapperInstance_shouldNotBeNull() {
        assertNotNull(Mappers.getMapper(UserEntityMapper.class));
    }

    @Test
    void shouldMapUserEntityToUserDto() {
        UserEntity user = new PublicUser();
        user.setFirstName(FIRST_NAME);
        user.setMiddleName(MIDDLE_NAME);
        user.setLastName(LAST_NAME);
        user.setEmail("Invisable@google.com");
        user.setExternalId("48QI42I8CWObQuCvk2uuF3XlyS63");
        user.setPhoneNumber("555-555-5555");

        UserDTO userModel = Mappers.getMapper(UserEntityMapper.class).entityToModel(user);
        assertEquals(user.getFirstName(), userModel.getFirstName());
        assertEquals(user.getMiddleName(), userModel.getMiddleName());
        assertEquals(user.getLastName(), userModel.getLastName());
        assertEquals(user.getPhoneNumber(), userModel.getPhoneNumber());
        assertEquals(user.getEmail(), userModel.getEmail());
        assertEquals(user.getExternalId(), userModel.getExternalId());
    }

    @Test
    void shouldMapUserEntityToUserDtoWithRolesPresent() {
        UserEntity user = new PublicUser();
        user.setFirstName(FIRST_NAME);
        user.setMiddleName(MIDDLE_NAME);
        user.setLastName(LAST_NAME);
        user.setEmail("Invisable@google.com");
        user.setExternalId("48QI42I8CWObQuCvk2uuF3XlyS63");
        user.setPhoneNumber("555-555-5555");

        RoleEntity role = new RoleEntity();
        role.setId(UUID.randomUUID());
        role.setName("Role test");
        role.setDescription("Role description");
        role.setUsers(List.of(user));
        role.setPermissions(List.of());

        user.setRoles(List.of(role));

        UserDTO userModel = Mappers.getMapper(UserEntityMapper.class).entityToModel(user);
        assertEquals(user.getFirstName(), userModel.getFirstName());
        assertEquals(user.getMiddleName(), userModel.getMiddleName());
        assertEquals(user.getLastName(), userModel.getLastName());
        assertEquals(user.getPhoneNumber(), userModel.getPhoneNumber());
        assertEquals(user.getEmail(), userModel.getEmail());
        assertEquals(user.getExternalId(), userModel.getExternalId());

        assertEquals(1, user.getRoles().size());
        assertEquals(role.getId(), user.getRoles().get(0).getId());
        assertEquals(role.getName(), user.getRoles().get(0).getName());
        assertEquals(role.getDescription(), user.getRoles().get(0).getDescription());
    }

    @Test
    void shouldConvertUserCreationRequestToUserEntity() {

        UserCreationRequest userCreationRequest =
                new UserCreationRequest("ABCD", "idgiver", "public");

        UserEntity userEntity =
                Mappers.getMapper(UserEntityMapper.class)
                        .convertUserCreationRequestToUserEntity(userCreationRequest);

        assertEquals("ABCD", userEntity.getExternalId());
        assertEquals("idgiver", userEntity.getIdentityProvider());
        assertEquals(UserType.PUBLIC, userEntity.getUserType());
        assertEquals(false, userEntity.getDeleted());
    }

    @Test
    void shouldCreateDisplayNameWithAllFields() {
        UserEntity user = new PublicUser();
        user.setFirstName(FIRST_NAME);
        user.setMiddleName(MIDDLE_NAME);
        user.setLastName(LAST_NAME);

        UserDTO userDTO = Mappers.getMapper(UserEntityMapper.class).entityToModel(user);
        assertEquals("John Locke Doe", userDTO.getDisplayName());
    }

    @Test
    void shouldCreateDisplayNameWithNullMiddleName() {
        UserEntity user = new PublicUser();
        user.setFirstName(FIRST_NAME);
        user.setLastName(LAST_NAME);

        UserDTO userDTO = Mappers.getMapper(UserEntityMapper.class).entityToModel(user);
        assertEquals("John Doe", userDTO.getDisplayName());
    }

    @Test
    void shouldCreateDisplayNameWithMultipleSpaces() {
        UserEntity user = new PublicUser();
        user.setFirstName("John   ");
        user.setMiddleName("  Locke");
        user.setLastName("Doe");

        UserDTO userDTO = Mappers.getMapper(UserEntityMapper.class).entityToModel(user);
        assertEquals("John Locke Doe", userDTO.getDisplayName());
    }

    @Test
    void shouldCreateDisplayNameWithEmail() {
        UserEntity user = new PublicUser();
        user.setEmail("email@email.com");
        UserDTO userDTO = Mappers.getMapper(UserEntityMapper.class).entityToModel(user);

        assertEquals("email@email.com", userDTO.getDisplayName());
    }

    @Test
    void shouldCreateDisplayNameWithId() {
        UserEntity user = new PublicUser();
        UUID id = UUID.randomUUID();
        user.setId(id);
        UserDTO userDTO = Mappers.getMapper(UserEntityMapper.class).entityToModel(user);

        assertEquals(id.toString(), userDTO.getDisplayName());
    }

    @Test
    void shouldConvertPublicUserCreationRequestToPublicUserEntity() {
        UserCreationRequest request =
                new UserCreationRequest("externalId", "identityProvider", "public");
        // Assume request.setProfile() is appropriately called here if needed
        UserEntity userEntity =
                Mappers.getMapper(UserEntityMapper.class)
                        .convertUserCreationRequestToUserEntity(request);
        assertTrue(userEntity instanceof PublicUser);
        assertEquals("public", userEntity.getUserType().getValue());
    }

    @Test
    void shouldConvertAgencyUserCreationRequestToAgencyUserEntity() {
        UserCreationRequest request =
                new UserCreationRequest("externalId", "identityProvider", "agency");
        UserEntity userEntity =
                Mappers.getMapper(UserEntityMapper.class)
                        .convertUserCreationRequestToUserEntity(request);
        assertTrue(userEntity instanceof AgencyUser);
        assertEquals("agency", userEntity.getUserType().getValue());
    }

    @Test
    void shouldHandleNullProfileForPublicUser() {
        UserCreationRequest request =
                new UserCreationRequest("externalId", "identityProvider", "public");
        PublicUser userEntity =
                (PublicUser)
                        Mappers.getMapper(UserEntityMapper.class)
                                .convertUserCreationRequestToUserEntity(request);
        assertNull(userEntity.getProfile());
    }

    @Test
    void shouldThrowExceptionForUnsupportedUserType() {
        UserCreationRequest request =
                new UserCreationRequest("externalId", "identityProvider", "unsupportedType");
        assertThrows(
                IllegalArgumentException.class,
                () ->
                        Mappers.getMapper(UserEntityMapper.class)
                                .convertUserCreationRequestToUserEntity(request));
    }
}
