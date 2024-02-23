package io.nuvalence.user.management.api.service.service;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.nuvalence.user.management.api.service.entity.LanguageEntity;
import io.nuvalence.user.management.api.service.repository.LanguageRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

@ExtendWith(MockitoExtension.class)
class LanguageServiceTest {
    @Mock LanguageRepository languageRepository;
    @Mock LanguageService languageService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        languageService = new LanguageService(languageRepository);
    }

    @Test
    void testGetLanguageByStandardIdWithExistingLanguage() {

        LanguageEntity expectedLanguage = createLanguageEntity();
        when(languageRepository.findByLanguageStandardId(expectedLanguage.getLanguageStandardId()))
                .thenReturn(Optional.of(expectedLanguage));

        // Call the method under test
        Optional<LanguageEntity> result =
                languageService.getLanguageByStandardId(expectedLanguage.getLanguageStandardId());

        // Assert that the result is present and matches the expected language entity
        Assertions.assertEquals(Optional.of(expectedLanguage), result);

        // Verify that the language repository's method was called with the correct standard ID
        verify(languageRepository, times(1))
                .findByLanguageStandardId(expectedLanguage.getLanguageStandardId());
    }

    @Test
    void getLanguageByStandardIdWithNonExistingLanguage() {
        // Create a sample standard ID
        String standardId = "nonExistingLanguage";

        when(languageRepository.findByLanguageStandardId(standardId)).thenReturn(Optional.empty());

        // Call the method under test
        Optional<LanguageEntity> result = languageService.getLanguageByStandardId(standardId);

        // Assert that the result is empty
        Assertions.assertTrue(result.isEmpty());

        // Verify that the language repository's method was called with the correct standard ID
        verify(languageRepository, times(1)).findByLanguageStandardId(standardId);
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
