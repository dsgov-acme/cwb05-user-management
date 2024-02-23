package io.nuvalence.user.management.api.service.controller;

import io.nuvalence.auth.access.AuthorizationHandler;
import io.nuvalence.user.management.api.service.config.exception.ResourceNotFoundException;
import io.nuvalence.user.management.api.service.entity.LanguageEntity;
import io.nuvalence.user.management.api.service.generated.controllers.LanguagesApiDelegate;
import io.nuvalence.user.management.api.service.generated.models.LanguageDTO;
import io.nuvalence.user.management.api.service.mapper.LanguageEntityMapper;
import io.nuvalence.user.management.api.service.service.LanguageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Routes for languages supported by a given application(s).
 */
@Service
@RequiredArgsConstructor
public class LanguageApiDelegateImpl implements LanguagesApiDelegate {

    private final LanguageService languageService;

    private final AuthorizationHandler authorizationHandler;

    @Override
    public ResponseEntity<List<LanguageDTO>> getSupportedLanguages() {
        List<LanguageEntity> supportedLanguages = languageService.getLanguages();

        if (supportedLanguages.isEmpty()) {
            throw new ResourceNotFoundException("No supported languages found");
        }

        List<LanguageDTO> mappedLanguages =
                supportedLanguages.stream()
                        .filter(authorizationHandler.getAuthFilter("view", LanguageEntity.class))
                        .map(LanguageEntityMapper.INSTANCE::languageEntityToLanguageDto)
                        .toList();

        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(mappedLanguages);
    }
}
