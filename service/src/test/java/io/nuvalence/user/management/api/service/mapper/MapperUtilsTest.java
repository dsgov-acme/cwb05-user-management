package io.nuvalence.user.management.api.service.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.nuvalence.user.management.api.service.entity.RoleEntity;
import io.nuvalence.user.management.api.service.generated.models.RoleDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

@ExtendWith(MockitoExtension.class)
class MapperUtilsTest {

    private final String testUuid = "3505d910-a479-423b-b3f8-a3d16798a651";

    @Test
    void mapRoleEntitiesToRoleList_completesMappingAsExpected() {
        RoleEntity roleEntity = new RoleEntity();
        roleEntity.setId(UUID.fromString("af102616-4207-4850-adc4-0bf91058a261"));
        roleEntity.setName("ROLE_TO_TEST_1");

        RoleEntity roleEntity1 = new RoleEntity();
        roleEntity1.setId(UUID.fromString("1b56e8df-a24b-4036-a5d4-7e5b1ca40f9d"));
        roleEntity1.setName("ROLE_TO_TEST_2");

        List<RoleEntity> roleEntities = List.of(roleEntity, roleEntity1);

        List<RoleDTO> mappedRoles = MapperUtils.mapRoleEntitiesToRoleList(roleEntities);
        assertEquals(mappedRoles.size(), roleEntities.size());

        for (int i = 0; i < mappedRoles.size(); i++) {
            assertEquals(mappedRoles.get(i).getId(), roleEntities.get(i).getId());
            assertEquals(mappedRoles.get(i).getName(), roleEntities.get(i).getName());
        }
    }
}
