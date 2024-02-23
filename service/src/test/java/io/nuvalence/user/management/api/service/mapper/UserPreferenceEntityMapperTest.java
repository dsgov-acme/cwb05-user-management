package io.nuvalence.user.management.api.service.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import io.nuvalence.user.management.api.service.entity.PublicUser;
import io.nuvalence.user.management.api.service.entity.UserEntity;
import io.nuvalence.user.management.api.service.entity.UserPreferenceEntity;
import io.nuvalence.user.management.api.service.generated.models.UserPreferenceDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.UUID;

@ExtendWith(MockitoExtension.class)
class UserPreferenceEntityMapperTest {

    @Test
    void testMapperInstance_shouldNotBeNull() {
        assertNotNull(UserPreferenceEntityMapper.INSTANCE);
    }

    @Test
    void shouldMapUserPreferenceEntityToUserPreferenceDto() {
        UserPreferenceEntity userPreferenceEntity = createUserPreference();
        UserPreferenceDTO userPreferenceDTO =
                UserPreferenceEntityMapper.INSTANCE.convertUserPreferenceEntityToUserModel(
                        userPreferenceEntity);
        assertEquals(
                userPreferenceDTO.getPreferredLanguage(),
                userPreferenceEntity.getPreferredLanguage());
        assertEquals(
                userPreferenceDTO.getPreferredCommunicationMethod(),
                userPreferenceEntity.getPreferredCommunicationMethod());
    }

    private UserPreferenceEntity createUserPreference() {
        UserPreferenceEntity userPreference = new UserPreferenceEntity();
        UserEntity user = createUserEntity();
        userPreference.setUser(user);
        userPreference.setPreferredLanguage("en");
        userPreference.setPreferredCommunicationMethod("sms");
        return userPreference;
    }

    private UserEntity createUserEntity() {
        UserEntity userEntity = new PublicUser();
        userEntity.setEmail("Skipper@theIsland.com");
        userEntity.setFirstName("John");
        userEntity.setMiddleName("Locke");
        userEntity.setLastName("Doe");
        userEntity.setExternalId("TestExternalId1234");
        userEntity.setId(UUID.fromString("ca8cfd1b-8643-4185-ba7f-8c8fbc9a7da6"));
        userEntity.setRoles(new ArrayList<>());
        return userEntity;
    }
}
