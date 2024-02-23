package io.nuvalence.user.management.api.service.service;

import io.nuvalence.user.management.api.service.entity.LanguageEntity;
import io.nuvalence.user.management.api.service.repository.LanguageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Service for Language.
 */
@Service
@RequiredArgsConstructor
public class LanguageService {

    private final LanguageRepository languageRepository;

    /**
     * Simple method to retrieve a language entity by the standardId.
     * @param standardId standard if for a language.
     * @return an authenticated userId.
     */
    // to be done implement cache for this method
    public Optional<LanguageEntity> getLanguageByStandardId(String standardId) {
        return languageRepository.findByLanguageStandardId(standardId);
    }

    public List<LanguageEntity> getLanguages() {
        return languageRepository.findAll();
    }
}
