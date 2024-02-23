package io.nuvalence.user.management.api.service.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.nuvalence.auth.access.AuthorizationHandler;
import io.nuvalence.user.management.api.service.entity.LanguageEntity;
import io.nuvalence.user.management.api.service.generated.models.LanguageDTO;
import io.nuvalence.user.management.api.service.mapper.LanguageEntityMapper;
import io.nuvalence.user.management.api.service.service.LanguageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class LanguageApiDelegateImplTest {
    @Autowired private MockMvc mockMvc;

    @MockBean private LanguageService languageService;

    @MockBean private AuthorizationHandler authorizationHandler;

    @BeforeEach
    void setup() {
        when(authorizationHandler.isAllowed(any(), (Class<?>) any())).thenReturn(true);
        when(authorizationHandler.isAllowed(any(), (String) any())).thenReturn(true);
        when(authorizationHandler.isAllowedForInstance(any(), any())).thenReturn(true);
        when(authorizationHandler.getAuthFilter(any(), any())).thenReturn(element -> true);
    }

    @Test
    @WithMockUser
    void getSupportedLanguages() throws Exception {
        List<LanguageEntity> languages =
                List.of(
                        createLanguage(
                                "4e6ba44a-5446-4be9-a4a2-1090a9cca41a", "English", "en", "English"),
                        createLanguage(
                                "e00b33e9-00c7-4670-9518-e467f58bf6b6", "Spanish", "es", "Español"),
                        createLanguage(
                                "31177f51-67f9-46ba-b193-00664e33f896", "Chinese", "zh", "中文"),
                        createLanguage(
                                "3e0c9ad2-308d-4dc0-a33d-372cea213bcf", "Polish", "pl", "Polski"),
                        createLanguage(
                                "e95b36db-1313-4890-add1-89d43ffd5af5",
                                "Italian",
                                "it",
                                "Italiano"));

        when(languageService.getLanguages()).thenReturn(languages);

        List<LanguageDTO> mappedLanguages =
                languages.stream()
                        .map(LanguageEntityMapper.INSTANCE::languageEntityToLanguageDto)
                        .toList();

        mockMvc.perform(get("/api/v1/languages/").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$[0].languageStandardId")
                                .value(mappedLanguages.get(0).getLanguageStandardId()));
    }

    private LanguageEntity createLanguage(
            String id, String name, String languageCode, String localName) {
        LanguageEntity language = new LanguageEntity();
        language.setId(UUID.fromString(id));
        language.setLanguageName(name);
        language.setLanguageStandardId(languageCode);
        language.setLocalName(localName);
        return language;
    }
}
