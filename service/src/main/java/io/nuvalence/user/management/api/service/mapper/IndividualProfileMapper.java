package io.nuvalence.user.management.api.service.mapper;

import io.nuvalence.user.management.api.service.entity.profile.IndividualProfile;
import io.nuvalence.user.management.api.service.generated.models.IndividualProfileCreateModel;
import io.nuvalence.user.management.api.service.generated.models.IndividualProfileResponseModel;
import io.nuvalence.user.management.api.service.generated.models.IndividualProfileUpdateModel;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface IndividualProfileMapper {
    IndividualProfile createModelToIndividual(IndividualProfileCreateModel createModel);

    IndividualProfileResponseModel individualToResponseModel(IndividualProfile individual);

    IndividualProfile updateModelToIndividual(IndividualProfileUpdateModel updateModel);
}
