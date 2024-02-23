package io.nuvalence.user.management.api.service.auth;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.nuvalence.auth.access.AuthorizationHandler;
import io.nuvalence.user.management.api.service.entity.PublicUser;
import io.nuvalence.user.management.api.service.entity.UserEntity;
import io.nuvalence.user.management.api.service.enums.UserType;
import io.nuvalence.user.management.api.service.generated.models.UserCreationRequest;
import io.nuvalence.user.management.api.service.service.UserService;
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

import java.util.UUID;

/**
 * This class contains tests for Users API Delegate Implementation.
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class UsersApiDelegateImplTest {

    @Autowired private MockMvc mockMvc;

    @MockBean private UserService userService;

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
    void addUser() throws Exception {
        UserCreationRequest user = createNewUserModel();

        final String postBody = new ObjectMapper().writeValueAsString(user);

        // when create user, return whatever is passed in
        when(userService.createUser(any(UserEntity.class)))
                .thenAnswer(
                        i -> {
                            UserEntity userEntity = i.getArgument(0);
                            userEntity.setId(UUID.randomUUID());
                            return userEntity;
                        });

        mockMvc.perform(
                        post("/api/v1/users")
                                .content(postBody)
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void deleteUserById() throws Exception {
        UserEntity userEntity = createMockUser();
        // mock void method
        doNothing().when(userService).deleteUser(userEntity.getId());

        mockMvc.perform(delete("/api/v1/users/" + userEntity.getId().toString()))
                .andExpect(status().isOk());
    }

    private UserCreationRequest createNewUserModel() {
        UserCreationRequest user = new UserCreationRequest();
        user.setIdentityProvider("https://securetoken.google.com/my-project");
        user.setExternalId("SMGjTO5n3sZFVIi5IzpW2pI8vjf1");
        user.firstName("Rusty");
        user.middleName("Popins");
        user.lastName("Smith");
        user.phoneNumber("555-555-5555");
        user.setEmail("Rdawg@gmail.com");
        user.setUserType("public");
        return user;
    }

    private UserEntity createMockUser() {
        UserEntity user = new PublicUser();
        user.setId(UUID.randomUUID());
        user.setFirstName("Rusty");
        user.setMiddleName("Popins");
        user.setLastName("Smith");
        user.setEmail("Rdawg@gmail.com");
        user.setPhoneNumber("555-555-5555");
        user.setIdentityProvider("https://securetoken.google.com/my-project");
        user.setExternalId("SMGjTO5n3sZFVIi5IzpW2pI8vjf1");
        user.setUserType(UserType.PUBLIC);
        return user;
    }
}
