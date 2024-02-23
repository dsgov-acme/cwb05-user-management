package io.nuvalence.user.management.api.service.controller;

import io.nuvalence.auth.access.AuthorizationHandler;
import io.nuvalence.user.management.api.service.config.exception.ResourceNotFoundException;
import io.nuvalence.user.management.api.service.entity.ApplicationEntity;
import io.nuvalence.user.management.api.service.generated.controllers.ApplicationsApiDelegate;
import io.nuvalence.user.management.api.service.generated.models.ApplicationDTO;
import io.nuvalence.user.management.api.service.mapper.ApplicationEntityMapper;
import io.nuvalence.user.management.api.service.service.ApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * Routes for application(s).
 */
@Service
@RequiredArgsConstructor
public class ApplicationsApiDelegateImpl implements ApplicationsApiDelegate {
    private static final ApplicationEntityMapper mapper = ApplicationEntityMapper.INSTANCE;
    private final ApplicationService applicationService;
    private final AuthorizationHandler authorizationHandler;

    @Override
    public ResponseEntity<List<ApplicationDTO>> getApplications() {
        List<ApplicationDTO> applications =
                applicationService.getApplications().stream()
                        .filter(authorizationHandler.getAuthFilter("view", ApplicationEntity.class))
                        .map(mapper::applicationEntityToApplicationDto)
                        .toList();

        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(applications);
    }

    @Override
    public ResponseEntity<ApplicationDTO> getApplicationById(UUID id) {
        ApplicationEntity application =
                applicationService
                        .getApplicationById(id)
                        .filter(
                                applicationEntity ->
                                        authorizationHandler.isAllowedForInstance(
                                                "view", applicationEntity))
                        .orElseThrow(
                                () ->
                                        new ResourceNotFoundException(
                                                "Application with ID '" + id + "' not found."));

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(mapper.applicationEntityToApplicationDto(application));
    }
}
