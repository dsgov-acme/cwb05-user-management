package io.nuvalence.user.management.api.service.events.listener;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.nuvalence.events.event.dto.ApplicationRole;
import io.nuvalence.user.management.api.service.entity.ApplicationEntity;
import io.nuvalence.user.management.api.service.entity.PermissionEntity;
import io.nuvalence.user.management.api.service.repository.ApplicationRepository;
import io.nuvalence.user.management.api.service.service.ApplicationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@ExtendWith(MockitoExtension.class)
class RoleReportingEventProcessorTest {

    @Mock private ApplicationRepository applicationRepository;
    @Captor private ArgumentCaptor<ApplicationEntity> captor;
    @InjectMocks private ApplicationService applicationService;

    @Test
    void testExecute() {
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
