package io.nuvalence.user.management.api.service.controller;

import io.nuvalence.auth.access.AuthorizationHandler;
import io.nuvalence.user.management.api.service.audit.AuditableAction;
import io.nuvalence.user.management.api.service.audit.profile.EmployerProfileDataChangedAuditHandler;
import io.nuvalence.user.management.api.service.audit.profile.IndividualProfileDataChangedAuditHandler;
import io.nuvalence.user.management.api.service.config.exception.UnexpectedException;
import io.nuvalence.user.management.api.service.entity.profile.EmployerProfile;
import io.nuvalence.user.management.api.service.entity.profile.IndividualProfile;
import io.nuvalence.user.management.api.service.generated.controllers.ProfilesApiDelegate;
import io.nuvalence.user.management.api.service.generated.models.EmployerProfileCreateModel;
import io.nuvalence.user.management.api.service.generated.models.EmployerProfileResponseModel;
import io.nuvalence.user.management.api.service.generated.models.EmployerProfileUpdateModel;
import io.nuvalence.user.management.api.service.generated.models.IndividualProfileCreateModel;
import io.nuvalence.user.management.api.service.generated.models.IndividualProfileResponseModel;
import io.nuvalence.user.management.api.service.generated.models.IndividualProfileUpdateModel;
import io.nuvalence.user.management.api.service.generated.models.PageEmployerProfileResponseModel;
import io.nuvalence.user.management.api.service.generated.models.PageIndividualProfileResponseModel;
import io.nuvalence.user.management.api.service.mapper.EmployerProfileMapper;
import io.nuvalence.user.management.api.service.mapper.IndividualProfileMapper;
import io.nuvalence.user.management.api.service.mapper.PagingMetadataMapper;
import io.nuvalence.user.management.api.service.models.EmployerFilters;
import io.nuvalence.user.management.api.service.models.IndividualFilters;
import io.nuvalence.user.management.api.service.service.AuditEventService;
import io.nuvalence.user.management.api.service.service.EmployerProfileService;
import io.nuvalence.user.management.api.service.service.IndividualProfileService;
import io.nuvalence.user.management.api.service.service.UserService;
import io.nuvalence.user.management.api.service.util.RequestContextTimestamp;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProfilesApiDelegateImpl implements ProfilesApiDelegate {
    private final AuthorizationHandler authorizationHandler;
    private final IndividualProfileService individualService;
    private final IndividualProfileMapper individualMapper;
    private final PagingMetadataMapper pagingMetadataMapper;
    private final RequestContextTimestamp requestContextTimestamp;
    private final AuditEventService individualAuditEventService;
    private final EmployerProfileService employerService;
    private final EmployerProfileMapper employerMapper;
    private final AuditEventService auditEventService;
    private final UserService userService;

    private static final String INDIVIDUAL_PROFILE_NOT_FOUND_MSG = "Individual profile not found";
    private static final String CREATION_AUDIT_EVENT_ERR_MSG =
            "An error has occurred when recording a creation audit event for an";
    private static final String EMPLOYER_PROFILE_NOT_FOUND_MSG = "Employer profile not found";

    private static final String VIEW_ACTION = "view";

    private static final String CREATE_ACTION = "create";

    @Override
    public ResponseEntity<EmployerProfileResponseModel> postEmployerProfile(
            EmployerProfileCreateModel employerProfileCreateModel) {
        if (!authorizationHandler.isAllowed(CREATE_ACTION, EmployerProfile.class)) {
            throw new ForbiddenException();
        }

        EmployerProfile employer =
                employerService.saveEmployer(
                        employerMapper.createModelToEmployer(employerProfileCreateModel));

        EmployerProfileResponseModel employerProfileResponseModel =
                employerMapper.employerToResponseModel(employer);

        postAuditEventForEmployerProfileCreated(employer);

        return ResponseEntity.status(200).body(employerProfileResponseModel);
    }

    @Override
    public ResponseEntity<PageEmployerProfileResponseModel> getEmployerProfiles(
            String fein,
            String name,
            String type,
            String industry,
            String sortBy,
            String sortOrder,
            Integer pageNumber,
            Integer pageSize) {
        if (!authorizationHandler.isAllowed(VIEW_ACTION, EmployerProfile.class)) {
            throw new ForbiddenException();
        }

        Page<EmployerProfileResponseModel> results =
                employerService
                        .getEmployersByFilters(
                                new EmployerFilters(
                                        sortBy,
                                        sortOrder,
                                        pageNumber,
                                        pageSize,
                                        fein,
                                        name,
                                        type,
                                        industry))
                        .map(employerMapper::employerToResponseModel);

        PageEmployerProfileResponseModel response = new PageEmployerProfileResponseModel();
        response.items(results.toList());
        response.setPagingMetadata(pagingMetadataMapper.toPagingMetadata(results));

        return ResponseEntity.status(200).body(response);
    }

    @Override
    public ResponseEntity<EmployerProfileResponseModel> getEmployerProfile(UUID profileId) {
        final EmployerProfileResponseModel employerProfileResponseModel =
                employerService
                        .getEmployerById(profileId)
                        .filter(
                                employerInstance ->
                                        authorizationHandler.isAllowedForInstance(
                                                VIEW_ACTION, employerInstance))
                        .map(employerMapper::employerToResponseModel)
                        .orElseThrow(() -> new NotFoundException(EMPLOYER_PROFILE_NOT_FOUND_MSG));

        return ResponseEntity.status(200).body(employerProfileResponseModel);
    }

    @Override
    public ResponseEntity<EmployerProfileResponseModel> updateEmployerProfile(
            UUID profileId, EmployerProfileUpdateModel employerProfileUpdateModel) {
        if (!authorizationHandler.isAllowed("update", EmployerProfile.class)) {
            throw new ForbiddenException();
        }

        Optional<EmployerProfile> optionalEmployer = employerService.getEmployerById(profileId);
        if (optionalEmployer.isEmpty()) {
            throw new NotFoundException(EMPLOYER_PROFILE_NOT_FOUND_MSG);
        }
        EmployerProfile existingEmployer = optionalEmployer.get();

        try {
            final EmployerProfile savedEmployer =
                    AuditableAction.builder(EmployerProfile.class)
                            .auditHandler(
                                    new EmployerProfileDataChangedAuditHandler(auditEventService))
                            .requestContextTimestamp(requestContextTimestamp)
                            .action(
                                    employer -> {
                                        EmployerProfile employerToBeSaved =
                                                employerMapper.updateModelToEmployer(
                                                        employerProfileUpdateModel);
                                        employerToBeSaved.setId(profileId);
                                        employerToBeSaved.setCreatedBy(
                                                existingEmployer.getCreatedBy());
                                        employerToBeSaved.setCreatedTimestamp(
                                                existingEmployer.getCreatedTimestamp());

                                        employerService.saveEmployer(employerToBeSaved);

                                        return employerService
                                                .getEmployerById(profileId)
                                                .orElseThrow(
                                                        () ->
                                                                new UnexpectedException(
                                                                        EMPLOYER_PROFILE_NOT_FOUND_MSG
                                                                                + " after saving"));
                                    })
                            .build()
                            .execute(existingEmployer);

            return ResponseEntity.status(200)
                    .body(employerMapper.employerToResponseModel(savedEmployer));
        } catch (Exception e) {
            throw new UnexpectedException(e);
        }
    }

    @Override
    public ResponseEntity<IndividualProfileResponseModel> postIndividualProfile(
            IndividualProfileCreateModel individualProfileCreateModel) {
        if (!authorizationHandler.isAllowed(CREATE_ACTION, IndividualProfile.class)) {
            throw new ForbiddenException();
        }

        IndividualProfile individual =
                individualService.saveIndividual(
                        individualMapper.createModelToIndividual(individualProfileCreateModel));

        individualService.postAuditEventForIndividualProfileCreated(individual);

        IndividualProfileResponseModel individualProfileResponseModel =
                individualMapper.individualToResponseModel(individual);

        return ResponseEntity.status(HttpStatus.OK).body(individualProfileResponseModel);
    }

    @Override
    public ResponseEntity<IndividualProfileResponseModel> getIndividualProfile(UUID profileId) {
        final IndividualProfileResponseModel individualProfileResponseModel =
                individualService
                        .getIndividualById(profileId)
                        .filter(
                                individualInstance ->
                                        authorizationHandler.isAllowedForInstance(
                                                VIEW_ACTION, individualInstance))
                        .map(individualMapper::individualToResponseModel)
                        .orElseThrow(() -> new NotFoundException(INDIVIDUAL_PROFILE_NOT_FOUND_MSG));

        return ResponseEntity.status(200).body(individualProfileResponseModel);
    }

    @Override
    public ResponseEntity<IndividualProfileResponseModel> updateIndividualProfile(
            UUID profileId, IndividualProfileUpdateModel individualProfileUpdateModel) {
        if (!authorizationHandler.isAllowed("update", IndividualProfile.class)) {
            throw new ForbiddenException();
        }

        Optional<IndividualProfile> optionalIndividual =
                individualService.getIndividualById(profileId);
        if (optionalIndividual.isEmpty()) {
            throw new NotFoundException(INDIVIDUAL_PROFILE_NOT_FOUND_MSG);
        }
        IndividualProfile existingIndividual = optionalIndividual.get();

        try {
            final IndividualProfile savedIndividual =
                    AuditableAction.builder(IndividualProfile.class)
                            .auditHandler(
                                    new IndividualProfileDataChangedAuditHandler(
                                            individualAuditEventService))
                            .requestContextTimestamp(requestContextTimestamp)
                            .action(
                                    individual -> {
                                        IndividualProfile individualToBeSaved =
                                                individualMapper.updateModelToIndividual(
                                                        individualProfileUpdateModel);
                                        individualToBeSaved.setId(profileId);
                                        individualToBeSaved.setCreatedBy(
                                                existingIndividual.getCreatedBy());
                                        individualToBeSaved.setCreatedTimestamp(
                                                existingIndividual.getCreatedTimestamp());

                                        individualService.saveIndividual(individualToBeSaved);

                                        return individualService
                                                .getIndividualById(profileId)
                                                .orElseThrow(
                                                        () ->
                                                                new UnexpectedException(
                                                                        INDIVIDUAL_PROFILE_NOT_FOUND_MSG
                                                                                + " after saving"));
                                    })
                            .build()
                            .execute(existingIndividual);

            return ResponseEntity.status(200)
                    .body(individualMapper.individualToResponseModel(savedIndividual));
        } catch (Exception e) {
            throw new UnexpectedException(e);
        }
    }

    @Override
    public ResponseEntity<PageIndividualProfileResponseModel> getIndividualProfiles(
            String ssn,
            String name,
            String email,
            String sortBy,
            String sortOrder,
            Integer pageNumber,
            Integer pageSize) {
        if (!authorizationHandler.isAllowed(VIEW_ACTION, IndividualProfile.class)) {
            throw new ForbiddenException();
        }

        Page<IndividualProfileResponseModel> results =
                individualService
                        .getIndividualsByFilters(
                                new IndividualFilters(
                                        sortBy,
                                        sortOrder,
                                        pageNumber,
                                        pageSize,
                                        ssn,
                                        email,
                                        name,
                                        userService))
                        .map(individualMapper::individualToResponseModel);

        PageIndividualProfileResponseModel response = new PageIndividualProfileResponseModel();
        response.items(results.toList());
        response.setPagingMetadata(pagingMetadataMapper.toPagingMetadata(results));
        return ResponseEntity.status(200).body(response);
    }

    private void postAuditEventForEmployerProfileCreated(EmployerProfile profile) {
        try {
            employerService.postAuditEventForEmployerCreated(profile);
        } catch (Exception e) {
            String errorMessage =
                    String.format(
                            CREATION_AUDIT_EVENT_ERR_MSG
                                    + " employer profile with user id %s for profile with id %s.",
                            profile.getCreatedBy(),
                            profile.getId());
            log.error(errorMessage, e);
        }
    }
}
