package io.nuvalence.user.management.api.service.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.nuvalence.auth.access.AuthorizationHandler;
import io.nuvalence.auth.util.SecurityContextUtility;
import io.nuvalence.user.management.api.service.entity.PublicUser;
import io.nuvalence.user.management.api.service.entity.UserEntity;
import io.nuvalence.user.management.api.service.generated.models.UserUpdateRequest;
import io.nuvalence.user.management.api.service.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;
import java.util.UUID;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class MySelfApiDelegateImplTest {
    @Autowired private MockMvc mockMvc;

    @MockBean private AuthorizationHandler authorizationHandler;

    @MockBean private UserService userService;

    private final String mySelfEndpoint = "/api/v1/myself";

    @BeforeEach
    void setup() {
        when(authorizationHandler.isAllowed(any(), (Class<?>) any())).thenReturn(true);
        when(authorizationHandler.isAllowed(any(), (String) any())).thenReturn(true);
        when(authorizationHandler.isAllowedForInstance(any(), any())).thenReturn(true);
        when(authorizationHandler.getAuthFilter(any(), any())).thenReturn(element -> true);
    }

    private UserEntity createMockUser() {
        UserEntity user = new PublicUser();
        user.setId(UUID.randomUUID());
        user.setFirstName("John");
        user.setMiddleName("Locke");
        user.setLastName("Doe");
        user.setEmail("Rdawg@gmail.com");
        user.setExternalId("SMGjTO5n3sZFVIi5IzpW2pI8vjf1");
        user.setPhoneNumber("555-555-5555");
        return user;
    }

    @Test
    void testGetMySelfWithValidUserId() throws Exception {
        UserEntity user = createMockUser();
        when(authorizationHandler.isAllowedForInstance("view", user)).thenReturn(true);

        when(userService.getUserByIdLoaded(user.getId())).thenReturn(Optional.of(user));

        try (MockedStatic<SecurityContextUtility> mock =
                Mockito.mockStatic(SecurityContextUtility.class)) {
            mock.when(SecurityContextUtility::getAuthenticatedUserId)
                    .thenReturn(user.getId().toString());

            mockMvc.perform(get(mySelfEndpoint))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(user.getId().toString()));
        }
    }

    @Test
    void testGetMySelfWithInvalidUserId() throws Exception {
        UserEntity user = createMockUser();
        when(authorizationHandler.isAllowedForInstance("view", user)).thenReturn(true);

        when(userService.getUserByIdLoaded(user.getId())).thenReturn(Optional.of(user));
        try (MockedStatic<SecurityContextUtility> mock =
                Mockito.mockStatic(SecurityContextUtility.class)) {
            mock.when(SecurityContextUtility::getAuthenticatedUserId)
                    .thenReturn(UUID.randomUUID().toString());
            mockMvc.perform(get(mySelfEndpoint)).andExpect(status().isNotFound());
        }
    }

    @Test
    void testUpdateMySelfWithValidUserId() throws Exception {
        UserEntity user = createMockUser();
        when(userService.getUserByIdLoaded(user.getId())).thenReturn(Optional.of(user));
        when(authorizationHandler.isAllowedForInstance("update", user)).thenReturn(true);

        when(userService.updateUserById(user.getId(), new UserUpdateRequest())).thenReturn(user);

        try (MockedStatic<SecurityContextUtility> mock =
                Mockito.mockStatic(SecurityContextUtility.class)) {
            mock.when(SecurityContextUtility::getAuthenticatedUserId)
                    .thenReturn(user.getId().toString());
            mockMvc.perform(
                            put(mySelfEndpoint)
                                    .content("{}")
                                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").exists());
        }
    }

    @Test
    void testUpdateMySelfWithInvalidUserId() throws Exception {
        UserEntity user = createMockUser();
        when(userService.getUserByIdLoaded(user.getId())).thenReturn(Optional.empty());
        when(authorizationHandler.isAllowedForInstance("update", user)).thenReturn(true);

        when(userService.updateUserById(user.getId(), new UserUpdateRequest())).thenReturn(user);

        try (MockedStatic<SecurityContextUtility> mock =
                Mockito.mockStatic(SecurityContextUtility.class)) {
            mock.when(SecurityContextUtility::getAuthenticatedUserId)
                    .thenReturn(user.getId().toString());
            mockMvc.perform(
                            put(mySelfEndpoint)
                                    .content("{}")
                                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound());
        }
    }
}
