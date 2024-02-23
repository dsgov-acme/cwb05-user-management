package io.nuvalence.user.management.api.service.mapper;

import io.nuvalence.user.management.api.service.entity.profile.EmployerProfile;
import io.nuvalence.user.management.api.service.generated.models.EmployerProfileCreateModel;
import io.nuvalence.user.management.api.service.generated.models.EmployerProfileResponseModel;
import io.nuvalence.user.management.api.service.generated.models.EmployerProfileUpdateModel;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface EmployerProfileMapper {
    EmployerProfileMapper INSTANCE = Mappers.getMapper(EmployerProfileMapper.class);

    EmployerProfileResponseModel employerToResponseModel(EmployerProfile employer);

    EmployerProfile updateModelToEmployer(EmployerProfileUpdateModel updateModel);

    EmployerProfile createModelToEmployer(EmployerProfileCreateModel createModel);
}
