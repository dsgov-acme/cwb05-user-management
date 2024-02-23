package io.nuvalence.user.management.api.service.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.nuvalence.user.management.api.service.entity.profile.IndividualProfile;
import io.nuvalence.user.management.api.service.generated.models.IndividualProfileCreateModel;
import io.nuvalence.user.management.api.service.generated.models.IndividualProfileResponseModel;
import io.nuvalence.user.management.api.service.generated.models.IndividualProfileUpdateModel;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.UUID;

@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public class IndividualProfileMapperTest {
    private final IndividualProfileMapper mapper = Mappers.getMapper(IndividualProfileMapper.class);

    @Test
    void individualToResponseModel() {
        UUID id = UUID.randomUUID();

        IndividualProfileResponseModel responseModelResult =
                mapper.individualToResponseModel(createIndividual(id));

        assertEquals(profileResponseModel(id), responseModelResult);
    }

    @Test
    void updateModelIndividual() {
        IndividualProfile individualResult = mapper.updateModelToIndividual(profileUpdateModel());

        assertEquals(createIndividual(null), individualResult);
    }

    @Test
    void createModelToIndividua() {
        IndividualProfile individualResult = mapper.createModelToIndividual(profileCreateModel());

        assertEquals(createIndividual(null), individualResult);
    }

    private IndividualProfile createIndividual(UUID id) {
        return IndividualProfile.builder().id(id).ssn("ssn").build();
    }

    private IndividualProfileUpdateModel profileUpdateModel() {
        IndividualProfileUpdateModel individualProfileUpdateModel =
                new IndividualProfileUpdateModel();
        individualProfileUpdateModel.setSsn("ssn");
        return individualProfileUpdateModel;
    }

    private IndividualProfileCreateModel profileCreateModel() {
        IndividualProfileCreateModel individualProfileCreateModel =
                new IndividualProfileCreateModel();
        individualProfileCreateModel.setSsn("ssn");
        return individualProfileCreateModel;
    }

    private IndividualProfileResponseModel profileResponseModel(UUID id) {
        IndividualProfileResponseModel individualProfileResponseModel =
                new IndividualProfileResponseModel();
        individualProfileResponseModel.setId(id);
        individualProfileResponseModel.setSsn("ssn");
        return individualProfileResponseModel;
    }
}
