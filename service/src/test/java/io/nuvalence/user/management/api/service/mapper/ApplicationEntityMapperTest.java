package io.nuvalence.user.management.api.service.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.nuvalence.user.management.api.service.entity.ApplicationEntity;
import io.nuvalence.user.management.api.service.generated.models.ApplicationDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

@ExtendWith(MockitoExtension.class)
class ApplicationEntityMapperTest {

    /**
     * Tests if Application entities are properly mapped to Application DTOs.
     */
    @Test
    void shouldMapApplicationEntityToApplicationDto() {
        ApplicationEntity applicationEntity = new ApplicationEntity();
        applicationEntity.setId(UUID.randomUUID());
        applicationEntity.setName("APPLICATION_NAME");
        applicationEntity.setDisplayName("Application Name");

        ApplicationDTO applicationDto =
                ApplicationEntityMapper.INSTANCE.applicationEntityToApplicationDto(
                        applicationEntity);
        assertEquals(applicationEntity.getId(), applicationDto.getId());
        assertEquals(applicationEntity.getName(), applicationDto.getName());
        assertEquals(applicationEntity.getDisplayName(), applicationDto.getDisplayName());
    }
}
