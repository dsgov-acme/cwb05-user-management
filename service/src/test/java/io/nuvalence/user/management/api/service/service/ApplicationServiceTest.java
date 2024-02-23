package io.nuvalence.user.management.api.service.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.nuvalence.events.event.dto.ApplicationRole;
import io.nuvalence.user.management.api.service.entity.ApplicationEntity;
import io.nuvalence.user.management.api.service.entity.LanguageEntity;
import io.nuvalence.user.management.api.service.entity.PermissionEntity;
import io.nuvalence.user.management.api.service.repository.ApplicationRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Tests for ApplicationService.
 */
@ExtendWith(MockitoExtension.class)
public class ApplicationServiceTest {

    private static List<ApplicationEntity> apps = null;

    @Mock private ApplicationRepository applicationRepository;

    @Captor private ArgumentCaptor<ApplicationEntity> captor;

    @InjectMocks private ApplicationService applicationService;

    @BeforeAll
    public static void setupLanguages() {
        apps = createApplications();
    }

    /**
     * Tests if the application service returns a successful 2xx response with 3 applications.
     */
    @Test
    void getsAllApplications() {
        when(applicationRepository.findAll()).thenReturn(apps);

        List<ApplicationEntity> applications = applicationService.getApplications();

        assertEquals(3, applications.size());
    }

    @Test
    void setApplicationRoles() {
        ApplicationEntity application = new ApplicationEntity();
        application.setId(UUID.randomUUID());
        application.setName("application-0");
        application.setDisplayName("My Application");
        application.setPermissions(createPermissions());

        when(applicationRepository.getApplicationByName(application.getName()))
                .thenReturn(Optional.of(application));

        // Update the application to only use half of the permissions. The rest should be inactive.
        List<ApplicationRole> permissions = new ArrayList<>();
        for (int i = 0; i < application.getPermissions().size(); i++) {
            PermissionEntity even = application.getPermissions().get(i);

            if (i % 2 == 0) {
                ApplicationRole applicationRole =
                        ApplicationRole.builder()
                                .name(even.getName())
                                .id(even.getId())
                                .description(even.getDescription())
                                .applicationRole(even.getApplicationRole())
                                .group(even.getGroup())
                                .build();

                permissions.add(applicationRole);
            }
        }

        applicationService.setApplicationRoles(application.getName(), permissions);

        verify(applicationRepository).save(captor.capture());

        ApplicationEntity savedApp = captor.getValue();
        assertEquals(application.getName(), savedApp.getName());
        assertEquals(application.getDisplayName(), savedApp.getDisplayName());

        Set<String> expectedActive =
                permissions.stream()
                        .map(ApplicationRole::getApplicationRole)
                        .collect(Collectors.toUnmodifiableSet());

        assertEquals(application.getPermissions().size(), savedApp.getPermissions().size());

        Set<String> foundActive =
                savedApp.getPermissions().stream()
                        .filter(permission -> permission.isActive())
                        .map(PermissionEntity::getApplicationRole)
                        .collect(Collectors.toUnmodifiableSet());

        assertEquals(expectedActive, foundActive);
    }

    @Test
    void getApplicationById() {
        when(applicationRepository.findById(any()))
                .thenReturn(Optional.of(createApplication("TEST_1", "Test 1")));

        Optional<ApplicationEntity> application =
                applicationService.getApplicationById(UUID.randomUUID());

        assertTrue(application.isPresent());
        assertTrue(application.get().getName().equalsIgnoreCase("TEST_1"));
        assertEquals("Test 1", Objects.requireNonNull(application.get()).getDisplayName());
    }

    @Test
    void getApplicationById_fails_ifApplicationDoesNotExist() {
        when(applicationRepository.findById(any())).thenReturn(Optional.empty());

        Optional<ApplicationEntity> result =
                applicationService.getApplicationById(UUID.randomUUID());
        assertTrue(result.isEmpty());
    }

    private static LanguageEntity createLanguage(String name, String standardId) {
        LanguageEntity lang = new LanguageEntity();
        lang.setId(UUID.randomUUID());
        lang.setLanguageName(name);
        lang.setLanguageStandardId(standardId);
        return lang;
    }

    private static List<LanguageEntity> createLanguages() {
        LanguageEntity language0 = createLanguage("English", "en");
        LanguageEntity language1 = createLanguage("Spanish", "es");
        LanguageEntity language2 = createLanguage("Greek", "el");

        return List.of(language0, language1, language2);
    }

    private static ApplicationEntity createApplication(String name, String displayName) {
        ApplicationEntity app = new ApplicationEntity();
        app.setId(UUID.randomUUID());
        app.setName(name);
        app.setDisplayName(displayName);
        return app;
    }

    private static List<ApplicationEntity> createApplications() {
        ApplicationEntity app0 = createApplication("group_a", "Group A");
        ApplicationEntity app1 = createApplication("group_b", "Group B");
        ApplicationEntity app2 = createApplication("group_c", "Group C");

        return List.of(app0, app1, app2);
    }

    private List<PermissionEntity> createPermissions() {
        List<PermissionEntity> permissions = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            PermissionEntity permission = new PermissionEntity();

            permission.setActive(true);
            permission.setName(String.format("permission-%d", i));
            permission.setGroup(String.format("group-%d", i % 2));
            permission.setDescription("Random permission.");
            permission.setApplicationRole(String.format("um:role-name-%d", i));

            permissions.add(permission);
        }

        return permissions;
    }
}
