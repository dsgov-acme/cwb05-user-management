package io.nuvalence.user.management.api.service.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.nuvalence.user.management.api.service.config.exception.BusinessLogicException;
import io.nuvalence.user.management.api.service.entity.LanguageEntity;
import io.nuvalence.user.management.api.service.entity.PublicUser;
import io.nuvalence.user.management.api.service.entity.UserEntity;
import io.nuvalence.user.management.api.service.entity.UserPreferenceEntity;
import io.nuvalence.user.management.api.service.generated.models.UserPreferenceDTO;
import io.nuvalence.user.management.api.service.repository.UserPreferencesRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;

import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

@ExtendWith(MockitoExtension.class)
class UserPreferenceServiceTest {

    @Mock UserPreferencesRepository userPreferencesRepository;

    @InjectMocks UserPreferenceService userPreferenceService;
    @Mock LanguageService languageService;

    @Test
    void getUserPreferencesWithExistingPreferences() {
        UserPreferenceEntity expectedPreferences = createUserPreference();
        when(userPreferencesRepository.findByUserId(expectedPreferences.getUser().getId()))
                .thenReturn(Optional.of(expectedPreferences));

        // Call the method under test
        Optional<UserPreferenceEntity> result =
                userPreferenceService.getUserPreferences(expectedPreferences.getUser().getId());

        // Assert that the result is present and matches the expected preferences
        Assertions.assertTrue(result.isPresent());
        Assertions.assertEquals(expectedPreferences, result.get());

        // Verify that the repository's method was called with the correct user ID
        verify(userPreferencesRepository, times(1))
                .findByUserId(expectedPreferences.getUser().getId());
    }

    @Test
    void getUserPreferencesWithNonExistingPreferences() {
        // Create a sample user ID
        UUID userId = UUID.randomUUID();

        when(userPreferencesRepository.findByUserId(userId)).thenReturn(Optional.empty());

        Optional<UserPreferenceEntity> result = userPreferenceService.getUserPreferences(userId);

        // Assert that the result is empty
        Assertions.assertFalse(result.isPresent());

        // Verify that the repository's method was called with the correct user ID
        verify(userPreferencesRepository, times(1)).findByUserId(userId);
    }

    @Test
    void updateUserPreferencesSuccess() {
        UserEntity user = createUserEntity();
        UserPreferenceDTO updatedPreferences = createUserPreferenceDTO("es", "email");

        LanguageEntity validLanguage = createLanguageEntity();
        when(languageService.getLanguageByStandardId(updatedPreferences.getPreferredLanguage()))
                .thenReturn(Optional.of(validLanguage));

        doAnswer((Answer<UserPreferenceEntity>) invocation -> invocation.getArgument(0))
                .when(userPreferencesRepository)
                .save(any(UserPreferenceEntity.class));

        var entity = userPreferenceService.updateUserPreferences(updatedPreferences, user);

        // asserting userId because it's also the entity id, and since not auto generated, it needs
        // to be explicitly set by the service or jpa will throw an IdentifierGenerationException
        // during runtime
        assertEquals(user.getId(), entity.getUserId());
        verify(userPreferencesRepository, times(1)).save(any(UserPreferenceEntity.class));
    }

    @Test
    void updateUserPreferencesWithNotAllowedCommunicationMethod() {
        UserEntity user = createUserEntity();
        UserPreferenceDTO updatedPreferences = createUserPreferenceDTO("en", "er");

        BusinessLogicException exception =
                Assertions.assertThrows(
                        BusinessLogicException.class,
                        () ->
                                userPreferenceService.updateUserPreferences(
                                        updatedPreferences, user));

        String expectedErrorMessage = "Preferred Communication Method not allowed";
        String actualErrorMessage = exception.getMessage();
        Assertions.assertEquals(expectedErrorMessage, actualErrorMessage);
    }

    @Test
    void updateUserPreferencesWithNotAllowedPreferredLanguage() {
        UserEntity user = createUserEntity();
        UserPreferenceDTO updatedPreferences = createUserPreferenceDTO("ex", "sms");

        when(languageService.getLanguageByStandardId(any())).thenReturn(Optional.empty());

        BusinessLogicException exception =
                Assertions.assertThrows(
                        BusinessLogicException.class,
                        () ->
                                userPreferenceService.updateUserPreferences(
                                        updatedPreferences, user));

        String expectedErrorMessage = "Preferred Language not allowed";
        String actualErrorMessage = exception.getMessage();
        Assertions.assertEquals(expectedErrorMessage, actualErrorMessage);
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
        userEntity.setFirstName("John");
        userEntity.setMiddleName("Locke");
        userEntity.setLastName("Doe");
        userEntity.setEmail("Skipper@theIsland.com");
        userEntity.setPhoneNumber("555-555-5555");
        userEntity.setExternalId("TestExternalId1234");
        userEntity.setId(UUID.fromString("ca8cfd1b-8643-4185-ba7f-8c8fbc9a7da6"));
        userEntity.setRoles(new ArrayList<>());
        return userEntity;
    }

    private UserPreferenceDTO createUserPreferenceDTO(String language, String communicationMethod) {
        UserPreferenceDTO userPreferenceDTO = new UserPreferenceDTO();
        userPreferenceDTO.setPreferredCommunicationMethod(communicationMethod);
        userPreferenceDTO.setPreferredLanguage(language);
        return userPreferenceDTO;
    }

    private LanguageEntity createLanguageEntity() {
        LanguageEntity language = new LanguageEntity();
        language.setId(UUID.randomUUID());
        language.setLanguageName("english");
        language.setLanguageStandardId("en");
        language.setLanguageName("test");
        return language;
    }
}
