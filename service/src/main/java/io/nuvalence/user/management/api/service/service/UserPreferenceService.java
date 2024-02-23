package io.nuvalence.user.management.api.service.service;

import io.nuvalence.user.management.api.service.config.exception.BusinessLogicException;
import io.nuvalence.user.management.api.service.entity.LanguageEntity;
import io.nuvalence.user.management.api.service.entity.UserEntity;
import io.nuvalence.user.management.api.service.entity.UserPreferenceEntity;
import io.nuvalence.user.management.api.service.enums.PreferredCommunicationMethod;
import io.nuvalence.user.management.api.service.generated.models.UserPreferenceDTO;
import io.nuvalence.user.management.api.service.repository.UserPreferencesRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

/**
 * Service for User Preferences.
 */
@Component
@Transactional
@RequiredArgsConstructor
@Slf4j
public class UserPreferenceService {

    private final UserPreferencesRepository userPreferencesRepository;
    private final LanguageService languageService;

    /**
     * Returns user preferences, optionally by application.
     * 
     * @param userId user's id.
     * @return User Preferences.
     */
    public Optional<UserPreferenceEntity> getUserPreferences(UUID userId) {
        return userPreferencesRepository.findByUserId(userId);
    }

    /**
     * For a given user, override their user preferences.
     * @param updatedPreferences Updated preferences.
     * @param user userEntity.
     * @return UserPreferenceEntity.
     *
     * @throws BusinessLogicException if the preferred communication method is not allowed.
     */
    public UserPreferenceEntity updateUserPreferences(
            UserPreferenceDTO updatedPreferences, UserEntity user) {

        PreferredCommunicationMethod communicationMethod =
                PreferredCommunicationMethod.fromText(
                        updatedPreferences.getPreferredCommunicationMethod());
        if (communicationMethod == PreferredCommunicationMethod.NOT_ALLOWED) {
            throw new BusinessLogicException("Preferred Communication Method not allowed");
        }

        Optional<LanguageEntity> language =
                languageService.getLanguageByStandardId(updatedPreferences.getPreferredLanguage());
        if (language.isEmpty()) {
            throw new BusinessLogicException("Preferred Language not allowed");
        }

        Optional<UserPreferenceEntity> userPreferenceEntity = getUserPreferences(user.getId());
        UserPreferenceEntity preferenceEntity =
                userPreferenceEntity.orElseGet(UserPreferenceEntity::new);
        preferenceEntity.setUserId(user.getId());
        preferenceEntity.setPreferredLanguage(updatedPreferences.getPreferredLanguage());
        preferenceEntity.setPreferredCommunicationMethod(
                updatedPreferences.getPreferredCommunicationMethod());
        return userPreferencesRepository.save(preferenceEntity);
    }
}
