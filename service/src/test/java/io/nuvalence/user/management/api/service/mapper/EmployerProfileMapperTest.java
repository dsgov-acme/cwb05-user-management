package io.nuvalence.user.management.api.service.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.nuvalence.user.management.api.service.entity.profile.EmployerProfile;
import io.nuvalence.user.management.api.service.generated.models.EmployerProfileCreateModel;
import io.nuvalence.user.management.api.service.generated.models.EmployerProfileResponseModel;
import io.nuvalence.user.management.api.service.generated.models.EmployerProfileUpdateModel;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.Collections;
import java.util.UUID;

@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public class EmployerProfileMapperTest {
    private final EmployerProfileMapper mapper = Mappers.getMapper(EmployerProfileMapper.class);

    @Test
    void employerToResponseModel() {
        UUID id = UUID.randomUUID();

        EmployerProfileResponseModel responseModelResult =
                mapper.employerToResponseModel(createEmployer(id));

        assertEquals(profileResponseModel(id), responseModelResult);
    }

    @Test
    void updateModelToEmployer() {
        EmployerProfile employerResult = mapper.updateModelToEmployer(profileUpdateModel());

        assertEquals(createEmployer(null), employerResult);
    }

    @Test
    void createModelToEmployer() {
        EmployerProfile employerResult = mapper.createModelToEmployer(profileCreateModel());

        assertEquals(createEmployer(null), employerResult);
    }

    private EmployerProfile createEmployer(UUID id) {
        return EmployerProfile.builder()
                .id(id)
                .fein("fein")
                .legalName("legalName")
                .otherNames(Collections.singletonList("otherNames"))
                .type("LLC")
                .industry("industry")
                .summaryOfBusiness("summaryOfBusiness")
                .businessPhone("businessPhone")
                .build();
    }

    private EmployerProfileUpdateModel profileUpdateModel() {
        EmployerProfileUpdateModel employerProfileUpdateModel = new EmployerProfileUpdateModel();
        employerProfileUpdateModel.setFein("fein");
        employerProfileUpdateModel.setLegalName("legalName");
        employerProfileUpdateModel.setOtherNames(Collections.singletonList("otherNames"));
        employerProfileUpdateModel.setType("LLC");
        employerProfileUpdateModel.setIndustry("industry");
        employerProfileUpdateModel.setSummaryOfBusiness("summaryOfBusiness");
        employerProfileUpdateModel.businessPhone("businessPhone");

        return employerProfileUpdateModel;
    }

    private EmployerProfileCreateModel profileCreateModel() {
        EmployerProfileCreateModel employerProfileCreateModel = new EmployerProfileCreateModel();
        employerProfileCreateModel.setFein("fein");
        employerProfileCreateModel.setLegalName("legalName");
        employerProfileCreateModel.setOtherNames(Collections.singletonList("otherNames"));
        employerProfileCreateModel.setType("LLC");
        employerProfileCreateModel.setIndustry("industry");
        employerProfileCreateModel.setSummaryOfBusiness("summaryOfBusiness");
        employerProfileCreateModel.businessPhone("businessPhone");

        return employerProfileCreateModel;
    }

    private EmployerProfileResponseModel profileResponseModel(UUID id) {
        EmployerProfileResponseModel employerProfileResponseModel =
                new EmployerProfileResponseModel();
        employerProfileResponseModel.setId(id);
        employerProfileResponseModel.setFein("fein");
        employerProfileResponseModel.setLegalName("legalName");
        employerProfileResponseModel.setOtherNames(Collections.singletonList("otherNames"));
        employerProfileResponseModel.setType("LLC");
        employerProfileResponseModel.setIndustry("industry");
        employerProfileResponseModel.setSummaryOfBusiness("summaryOfBusiness");
        employerProfileResponseModel.businessPhone("businessPhone");

        return employerProfileResponseModel;
    }
}
