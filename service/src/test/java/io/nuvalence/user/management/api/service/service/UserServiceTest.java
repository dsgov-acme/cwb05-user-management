package io.nuvalence.user.management.api.service.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.nuvalence.user.management.api.service.config.exception.BusinessLogicException;
import io.nuvalence.user.management.api.service.config.exception.ResourceNotFoundException;
import io.nuvalence.user.management.api.service.entity.AgencyUser;
import io.nuvalence.user.management.api.service.entity.PermissionEntity;
import io.nuvalence.user.management.api.service.entity.PublicUser;
import io.nuvalence.user.management.api.service.entity.RoleEntity;
import io.nuvalence.user.management.api.service.entity.UserEntity;
import io.nuvalence.user.management.api.service.entity.profile.IndividualProfile;
import io.nuvalence.user.management.api.service.enums.UserType;
import io.nuvalence.user.management.api.service.generated.models.RoleDTO;
import io.nuvalence.user.management.api.service.generated.models.UserRoleDTO;
import io.nuvalence.user.management.api.service.generated.models.UserUpdateRequest;
import io.nuvalence.user.management.api.service.repository.RoleRepository;
import io.nuvalence.user.management.api.service.repository.UserRepository;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings({"checkstyle:ClassFanOutComplexity", "checkstyle:ClassDataAbstractionCoupling"})
class UserServiceTest {

    @Mock private UserRepository userRepository;

    @Mock private RoleRepository roleRepository;

    @InjectMocks private UserService userService;

    @Mock private RoleService roleService;

    @Captor private ArgumentCaptor<UserEntity> userCaptor;

    // Constants
    private final String testEmail = "Skipper@theisland.net";
    private final String testExternalId = "TestExternalId123456789";

    // Create user tests.
    @Test
    void createUserAgencyWithDefaultRoles() {
        final UserEntity user = createUserEntity();
        final RoleEntity role = createRoleEntity();

        when(roleService.getDefaultRoles("agency")).thenReturn(List.of(role));
        when(userRepository.save(user)).thenReturn(user);
        UserEntity savedUser = userService.createUser(user);

        assertNotNull(savedUser);
        assertEquals(user, savedUser);
        assertEquals(1, savedUser.getRoles().size());
        assertEquals(role, savedUser.getRoles().get(0));

        verify(userRepository).save(user);
    }

    @Test
    void createUserPublicWithDefaultRoles() {
        final UserEntity user = createUserEntity();
        user.setUserType(UserType.PUBLIC);
        final RoleEntity role = createRoleEntity();

        when(roleService.getDefaultRoles("public")).thenReturn(List.of(role));
        when(userRepository.save(user)).thenReturn(user);
        UserEntity savedUser = userService.createUser(user);

        assertNotNull(savedUser);
        assertEquals(user, savedUser);
        assertEquals(1, savedUser.getRoles().size());
        assertEquals(role, savedUser.getRoles().get(0));

        verify(userRepository).save(user);
    }

    // Update user tests.
    @Test
    void updateUser_fully_updates_a_user() {
        // Create a user and make sure the user repository responds with it
        UserEntity originalUserEntity = createUserEntity();
        when(userRepository.findByIdLoaded(originalUserEntity.getId()))
                .thenAnswer(x -> Optional.of(createUserEntity()));

        // Set up the new values, update request, and expected new entity
        final String newFirstName = "John";
        final String newMiddleName = "Locke";
        final String newLastName = "II";
        final String newEmail = testEmail;
        final String newPhoneNumber = "555-555-5555";
        UserUpdateRequest updateRequest = new UserUpdateRequest();
        updateRequest.firstName(newFirstName);
        updateRequest.middleName(newMiddleName);
        updateRequest.lastName(newLastName);
        updateRequest.phoneNumber(newPhoneNumber);
        updateRequest.setEmail(newEmail);
        UserEntity newUserEntity = createUserEntity();
        newUserEntity.setFirstName(newFirstName);
        newUserEntity.setMiddleName(newMiddleName);
        newUserEntity.setLastName(newLastName);
        newUserEntity.setPhoneNumber(newPhoneNumber);
        newUserEntity.setEmail(newEmail);
        newUserEntity.setExternalId(testExternalId);

        // Fake the UserEntity return when save is called in the userService.updateUser(...)
        // function
        when(userRepository.save(isA(UserEntity.class))).thenReturn(newUserEntity);

        // Make the update request
        UserEntity res = userService.updateUserById(originalUserEntity.getId(), updateRequest);

        assertNotNull(res);

        verify(userRepository)
                .save(userCaptor.capture()); // userRepository should have been asked to save once
        UserEntity savedEntity = userCaptor.getValue(); // Capture what it was sent to save

        // Validate that the UserEntity submitted to userRepository.save and the returned user
        // are correct
        validateUserUpdate(originalUserEntity, updateRequest, savedEntity);
    }

    @Test
    void updateUser_partially_updates_a_user() {
        // Create a user and make sure the user repository responds with it
        UserEntity originalUserEntity = createUserEntity();
        when(userRepository.findByIdLoaded(originalUserEntity.getId()))
                .thenAnswer(x -> Optional.of(createUserEntity()));

        // Set up the new values and utility objects
        final String newFirstName = "John";
        final String newEmail = testEmail;
        UserUpdateRequest updateRequest = new UserUpdateRequest();
        UserEntity expectedUserEntity = createUserEntity();

        // Test changing only the user's first name
        updateRequest.setFirstName(newFirstName);
        expectedUserEntity.setFirstName(newFirstName);
        when(userRepository.save(isA(UserEntity.class))).thenReturn(expectedUserEntity);

        UserEntity res = userService.updateUserById(originalUserEntity.getId(), updateRequest);
        verify(userRepository, times(1)).save(userCaptor.capture());

        validateUserUpdate(originalUserEntity, updateRequest, res);
        expectedUserEntity.setFirstName(originalUserEntity.getFirstName()); // Reset for next test

        // Test changing only the user's email
        updateRequest = new UserUpdateRequest(); // Clear out old changes requested
        updateRequest.setEmail(newEmail);
        expectedUserEntity.setEmail(
                newEmail); // User repo return is automatically updated via reference

        res = userService.updateUserById(originalUserEntity.getId(), updateRequest);
        assertNotNull(res);

        verify(userRepository, times(2)).save(userCaptor.capture()); // 2x now
        validateUserUpdate(originalUserEntity, updateRequest, res);

        expectedUserEntity.setEmail(originalUserEntity.getEmail()); // Reset for next test

        // Test changing only the user's external id
        updateRequest = new UserUpdateRequest(); // Clear out old changes requested
        expectedUserEntity.setExternalId(
                testExternalId); // User repo return is automatically updated via reference

        res = userService.updateUserById(originalUserEntity.getId(), updateRequest);
        verify(userRepository, times(3)).save(userCaptor.capture()); // 3x now
        validateUserUpdate(originalUserEntity, updateRequest, res);

        // Test submitting the existing email and external id, but a changed display name
        updateRequest = new UserUpdateRequest(); // Clear out old changes requested
        updateRequest.setFirstName(newFirstName);
        updateRequest.setEmail(originalUserEntity.getEmail());
        expectedUserEntity.setFirstName(
                newFirstName); // User repo return is automatically updated via reference
        expectedUserEntity.setEmail(originalUserEntity.getEmail());
        expectedUserEntity.setExternalId(originalUserEntity.getExternalId());

        res = userService.updateUserById(originalUserEntity.getId(), updateRequest);
        verify(userRepository, times(4)).save(userCaptor.capture()); // 4x now
        validateUserUpdate(originalUserEntity, updateRequest, res);
    }

    // Makes sure that the request still succeeds (and doesn't affect anything) if no data is sent
    // to update
    @Test
    void updateUser_does_not_update_a_user() {
        // Create a user and make sure the user repository responds with it
        UserEntity originalUserEntity = createUserEntity();
        when(userRepository.findByIdLoaded(originalUserEntity.getId()))
                .thenAnswer(x -> Optional.of(createUserEntity()));

        // Set up the empty update request and expected new entity
        UserUpdateRequest updateRequest = new UserUpdateRequest();
        UserEntity newUserEntity = createUserEntity();

        // Fake the UserEntity return when save is called in the userService.updateUser(...)
        // function
        when(userRepository.save(isA(UserEntity.class))).thenReturn(newUserEntity);

        // Make the update request
        UserEntity res = userService.updateUserById(originalUserEntity.getId(), updateRequest);

        verify(userRepository)
                .save(userCaptor.capture()); // userRepository should have been asked to save once

        // Validate that the UserEntity submitted to userRepository.save and the returned user
        // are correct
        validateUserUpdate(originalUserEntity, updateRequest, res);
    }

    @Test
    void updateUser_fails_ifUserDoesNotExist() {
        // Create a user and make sure the user repository responds with it
        UserEntity originalUserEntity = createUserEntity();

        // Stub the findByIdLoaded method to return the user
        when(userRepository.findByIdLoaded(originalUserEntity.getId()))
                .thenReturn(Optional.of(originalUserEntity));

        // Set up the new values and utility objects
        UserUpdateRequest updateRequest = new UserUpdateRequest();

        // Stub the findByIdLoaded method again to return an empty optional
        when(userRepository.findByIdLoaded(originalUserEntity.getId()))
                .thenReturn(Optional.empty());

        // Now, assert the exception is thrown
        assertThrowsWithMessage(
                ResourceNotFoundException.class,
                "User not found.",
                () -> userService.updateUserById(originalUserEntity.getId(), updateRequest));

        // Make sure the user was never updated in the repository
        verify(userRepository, times(0)).save(any());
    }
    // Delete User Tests.

    @Test
    void deleteUser_fails_ifUserDoesNotExist() {
        UserEntity userEntity = createUserEntity();

        assertThrowsWithMessage(
                ResourceNotFoundException.class,
                "User with ID '" + userEntity.getId() + "' not found.",
                () -> userService.deleteUser(userEntity.getId()));
    }

    // Assign Role to User Tests.
    @Test
    void assignRoleToUser_assignsRoleIfValid() {
        UserRoleDTO userRole = createUserRoleDto();
        Optional<UserEntity> userEntity = Optional.of(createUserEntity());
        Optional<RoleEntity> roleEntity = Optional.of(createRoleEntity());

        when(userRepository.findById(userEntity.get().getId())).thenReturn(userEntity);
        when(roleRepository.findById(roleEntity.get().getId())).thenReturn(roleEntity);

        userService.assignRoleToUser(userRole.getUserId(), userRole.getRoleId());

        verify(userRepository).save(userCaptor.capture());
        UserEntity capturedUser = userCaptor.getValue();
        assertEquals(1, capturedUser.getRoles().size());
        assertEquals(roleEntity.get().getName(), capturedUser.getRoles().get(0).getName());
    }

    @Test
    void assignRoleToUser_fails_ifUserDoesNotExist() {
        UserRoleDTO userRole = createUserRoleDto();

        assertThrowsWithMessage(
                ResourceNotFoundException.class,
                "User with ID '" + userRole.getUserId() + "' not found.",
                () -> userService.assignRoleToUser(userRole.getUserId(), userRole.getRoleId()));
    }

    @Test
    void assignRoleToUser_fails_ifRoleDoesNotExist() {
        UserRoleDTO userRole = createUserRoleDto();
        Optional<UserEntity> userEntity = Optional.of(createUserEntity());

        when(userRepository.findById(userEntity.get().getId())).thenReturn(userEntity);

        assertThrowsWithMessage(
                ResourceNotFoundException.class,
                "Role with ID '" + userRole.getRoleId() + "' not found.",
                () -> userService.assignRoleToUser(userRole.getUserId(), userRole.getRoleId()));
    }

    // Remove Role from User Tests
    @Test
    void removeRoleToUser_assignsRoleIfValid() {
        UserRoleDTO userRole = createUserRoleDto();

        Optional<RoleEntity> roleEntity = Optional.of(createRoleEntity());
        roleEntity.get().setId(userRole.getRoleId());

        UserEntity user = createUserEntity();
        user.setRoles(List.of(roleEntity.get()));
        Optional<UserEntity> userEntity = Optional.of(user);

        when(userRepository.findById(user.getId())).thenReturn(userEntity);
        when(roleRepository.findById(roleEntity.get().getId())).thenReturn(roleEntity);

        userService.removeRoleFromUser(userRole.getUserId(), userRole.getRoleId());

        verify(userRepository).save(userCaptor.capture());
        UserEntity capturedUser = userCaptor.getValue();

        assertEquals(0, capturedUser.getRoles().size());
    }

    @Test
    void removeRoleToUser_fails_ifUserDoesNotExist() {
        UserRoleDTO userRole = createUserRoleDto();

        assertThrowsWithMessage(
                ResourceNotFoundException.class,
                "User with ID 'ca8cfd1b-8643-4185-ba7f-8c8fbc9a7da6' not found.",
                () -> userService.removeRoleFromUser(userRole.getUserId(), userRole.getRoleId()));
    }

    @Test
    void removeRoleToUser_fails_ifRoleDoesNotExist() {
        UserRoleDTO userRole = createUserRoleDto();
        UserEntity user = createUserEntity();
        Optional<RoleEntity> roleEntity = Optional.of(createRoleEntity());

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(roleRepository.findById(roleEntity.get().getId())).thenReturn(Optional.empty());

        assertThrowsWithMessage(
                ResourceNotFoundException.class,
                "Role with ID 'af102616-4207-4850-adc4-0bf91058a261' not found.",
                () -> userService.removeRoleFromUser(userRole.getUserId(), userRole.getRoleId()));
    }

    @Test
    void removeRoleToUser_fails_ifRoleIsNotAssignedToUser() {
        UserRoleDTO userRole = createUserRoleDto();
        Optional<UserEntity> userEntity = Optional.of(createUserEntity());
        Optional<RoleEntity> roleEntity = Optional.of(createRoleEntity());

        when(userRepository.findById(userEntity.get().getId())).thenReturn(userEntity);
        when(roleRepository.findById(roleEntity.get().getId())).thenReturn(roleEntity);

        assertThrowsWithMessage(
                BusinessLogicException.class,
                "The role requested does not exist: ca8cfd1b-8643-4185-ba7f-8c8fbc9a7da6.",
                () -> userService.removeRoleFromUser(userRole.getUserId(), userRole.getRoleId()));
    }

    // Get Users Tests
    @Test
    void getUserList_returnsUserListIfValid() {
        List<UserEntity> userEntities = List.of(createUserEntity());
        userEntities.get(0).setRoles(List.of(createRoleEntity()));

        Pageable pageable = PageRequest.of(0, 10);
        Page<UserEntity> pagedUserEntities =
                new PageImpl<>(userEntities, pageable, userEntities.size());

        when(userRepository.findAll(pageable)).thenReturn(pagedUserEntities);

        List<UserEntity> res = userService.getUserList(pageable).getContent();

        assertEquals(userEntities, res);
    }

    @Test
    void getUserList_fails_ifNoUsersExist() {
        Page<UserEntity> users = userService.getUserList(PageRequest.of(0, 10));

        assertNull(users);
    }

    // Get User by ID Tests.
    @Test
    void getUserById_returnsUserIfValid() {
        Optional<UserEntity> userEntity = Optional.of(createUserEntity());
        userEntity.get().setRoles(List.of(createRoleEntity()));
        when(userRepository.findByIdLoaded(userEntity.get().getId())).thenReturn(userEntity);

        Optional<UserEntity> res = userService.getUserByIdLoaded(userEntity.get().getId());

        assert (res.isPresent());
        assertEquals(userEntity.get(), res.get());
    }

    @Test
    void getUserByIdLoaded_WithProfiles() {
        Optional<UserEntity> userEntity = Optional.of(createUserEntity());
        userEntity.get().setRoles(List.of(createRoleEntity()));
        when(userRepository.findByIdLoaded(userEntity.get().getId())).thenReturn(userEntity);

        IndividualProfile individualProfile =
                IndividualProfile.builder().id(UUID.randomUUID()).build();
        when(userRepository.findProfileByUserId(userEntity.get().getId()))
                .thenReturn(Optional.of(individualProfile));

        Optional<UserEntity> res = userService.getUserByIdLoaded(userEntity.get().getId());

        assert (res.isPresent());
        assertEquals(individualProfile, ((PublicUser) userEntity.get()).getProfile());
    }

    @Test
    void getUserById_fails_ifNoUserExists() {
        Optional<UserEntity> userEntity = Optional.of(createUserEntity());

        assertTrue(userService.getUserByIdLoaded(userEntity.get().getId()).isEmpty());
    }

    @Test
    void getUserByIdLoaded() {
        UserEntity user = createUserEntity();
        when(userRepository.findByIdLoaded(user.getId())).thenReturn(Optional.of(user));
        Optional<UserEntity> result = userService.getUserByIdLoaded(user.getId());
        assertTrue(result.isPresent());
        assertEquals(user, result.get());
    }

    @Test
    void deleteUser_UserExists_UserDeleted() {
        UserEntity user = createUserEntity();
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        userService.deleteUser(user.getId());
        verify(userRepository, times(1)).findById(user.getId());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void deleteUser_UserDoesNotExist_ExceptionThrown() {
        UserEntity user = createUserEntity();

        when(userRepository.findById(user.getId())).thenReturn(Optional.empty());

        assertThrowsWithMessage(
                ResourceNotFoundException.class,
                null, // You can leave the expected message as null or customize it as needed
                () -> userService.deleteUser(user.getId()));

        verify(userRepository, times(1)).findById(user.getId());
        // verify(userRepository, never()).delete(any());
    }

    @Test
    void removeRoleFromUserWithEmptyRoles() {
        RoleEntity role = createRoleEntity();
        role.setId(UUID.randomUUID());
        UserEntity user = createUserEntity();
        user.setRoles(null);

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(roleRepository.findById(role.getId())).thenReturn(Optional.of(role));

        assertThrowsWithMessage(
                BusinessLogicException.class,
                null, // You can leave the expected message as null or customize it as needed
                () -> userService.removeRoleFromUser(user.getId(), role.getId()));
    }

    @Test
    void updateUserFailWithMissingUserId() {
        UserEntity user = createUserEntity();
        UserUpdateRequest updateRequest = new UserUpdateRequest();
        user.setId(null);

        assertThrowsWithMessage(
                ResourceNotFoundException.class,
                null, // You can leave the expected message as null or customize it as needed
                () -> userService.updateUserById(user.getId(), updateRequest));
    }

    private void assertThrowsWithMessage(
            Class<? extends Throwable> expectedException,
            String expectedMessage,
            Executable executable) {
        Throwable thrownException = assertThrows(expectedException, executable);

        if (expectedMessage != null) {
            assertEquals(expectedMessage, thrownException.getMessage());
        }
    }

    @Test
    void isDuplicateExternalUserException_False() {
        NullPointerException runtimeException = new NullPointerException();

        boolean result = userService.isDuplicateExternalUserException(runtimeException);

        assertFalse(result);
    }

    @Test
    void testIsDuplicateExternalUserException_True() {
        ConstraintViolationException constraintViolationException =
                new ConstraintViolationException(
                        "message",
                        new SQLException(),
                        "sql",
                        "user_table_identity_provider_external_id_deleted_deleted_on_key");

        boolean result = userService.isDuplicateExternalUserException(constraintViolationException);

        assertTrue(result);
    }

    @Test
    void testTrimDataToPublicFields() {
        UserEntity user = createUserEntity();
        UserEntity trimmedUser = userService.trimUserDataToPublicFields(user);

        // verify wanted data
        assertNotEquals(user, trimmedUser);
        assertEquals(user.getId(), trimmedUser.getId());
        assertEquals(user.getFirstName(), trimmedUser.getFirstName());
        assertEquals(user.getLastName(), trimmedUser.getLastName());
        assertEquals(user.getEmail(), trimmedUser.getEmail());

        // verify free of unwanted data
        HashSet<String> nonNullFields = new HashSet<>();

        Field[] fields = UserEntity.class.getDeclaredFields();
        for (Field field : fields) {
            Object value = ReflectionTestUtils.getField(trimmedUser, field.getName());
            if (value != null) {
                nonNullFields.add(field.getName());
            }
        }

        assertEquals(4, nonNullFields.size());

        nonNullFields.forEach(
                fieldName -> {
                    assertTrue(List.of("id", "firstName", "lastName", "email").contains(fieldName));
                });
    }

    @Test
    void testTrimDataToPublicFieldsForAgencyUser() {
        AgencyUser user = new AgencyUser();
        user.setId(UUID.randomUUID());
        user.setFirstName("Jane");
        user.setLastName("Doe");
        user.setEmail("jane.doe@example.com");

        UserEntity trimmedUser = userService.trimUserDataToPublicFields(user);

        assertNotSame(user, trimmedUser);
        assertTrue(trimmedUser instanceof AgencyUser);
        assertEquals(user.getId(), trimmedUser.getId());
        assertEquals(user.getFirstName(), trimmedUser.getFirstName());
        assertEquals(user.getLastName(), trimmedUser.getLastName());
        assertEquals(user.getEmail(), trimmedUser.getEmail());
    }

    @Test
    void testTrimDataToPublicFieldsForUnsupportedUserType() {
        UserEntity user = new UserEntity() {};

        assertThrows(
                IllegalArgumentException.class, () -> userService.trimUserDataToPublicFields(user));
    }

    @Test
    void testAddRoleToUser() {

        UserEntity user = createUserEntity();
        RoleEntity role = createRoleEntity();

        userService.addRoleToUser(user, role);

        assertEquals(1, user.getRoles().size());
        assertEquals(role, user.getRoles().get(0));

        verify(userRepository, times(1)).save(user);
    }

    @Test
    void testDeleteRoleFromUser() {
        UserEntity user = createUserEntity();
        RoleEntity role = createRoleEntity();
        List<RoleEntity> roles = new ArrayList<>();
        roles.add(role);
        user.setRoles(roles);

        userService.deleteRoleFromUser(user, role);

        assertEquals(0, user.getRoles().size());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void testGetAppRolesByUserId() {

        UserEntity user = createUserEntity();
        RoleEntity role1 = createRoleEntity();
        RoleEntity role2 = createRoleEntity();
        PermissionEntity permission1 = createPermissionEntity("AA");
        PermissionEntity permission2 = createPermissionEntity("bb");
        PermissionEntity permission3 = createPermissionEntity("AA");
        PermissionEntity permission4 = createPermissionEntity("cc");
        role1.setPermissions(Arrays.asList(permission1, permission2, null));
        role2.setPermissions(Arrays.asList(permission3, permission4, null));
        user.setRoles(Arrays.asList(role1, role2, null));

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        // act
        List<String> appRoles = userService.getAppRolesByUserId(user.getId());

        // verify
        assertEquals(3, appRoles.size());
        assertTrue(appRoles.contains("AA"));
        assertTrue(appRoles.contains("bb"));
        assertTrue(appRoles.contains("cc"));

        verify(userRepository).findById(user.getId());
    }

    // Helper methods

    private PermissionEntity createPermissionEntity(String applicationRole) {

        PermissionEntity permission = new PermissionEntity();
        permission.setId(UUID.randomUUID());
        permission.setApplicationRole(applicationRole);

        return permission;
    }

    private RoleDTO createRoleDto() {
        RoleDTO role = new RoleDTO();
        role.setId(UUID.fromString("af102616-4207-4850-adc4-0bf91058a261"));
        role.setName("ROLE_TO_TEST");
        role.setPermissions(Collections.emptyList());
        return role;
    }

    private RoleEntity createRoleEntity() {
        RoleEntity roleEntity = new RoleEntity();
        roleEntity.setName("ROLE_TO_TEST");
        roleEntity.setId(UUID.fromString("af102616-4207-4850-adc4-0bf91058a261"));
        roleEntity.setUsers(new ArrayList<>());
        roleEntity.setDefaultUserTypes(new HashSet<>());
        return roleEntity;
    }

    private UserEntity createUserEntity() {
        UserEntity userEntity = new PublicUser();
        userEntity.setFirstName("John");
        userEntity.setMiddleName("Locke");
        userEntity.setLastName("Doe");
        userEntity.setEmail("Skipper@theIsland.com");
        userEntity.setPhoneNumber("555-555-5555");
        userEntity.setUserType(UserType.AGENCY);
        userEntity.setExternalId("TestExternalId1234");
        userEntity.setId(UUID.fromString("ca8cfd1b-8643-4185-ba7f-8c8fbc9a7da6"));
        userEntity.setRoles(new ArrayList<>());
        return userEntity;
    }

    private UserRoleDTO createUserRoleDto() {
        UserRoleDTO userRoleDto = new UserRoleDTO();
        userRoleDto.setRoleId(createRoleDto().getId());
        userRoleDto.setUserId(createUserEntity().getId());
        return userRoleDto;
    }

    /*** Helper function for use when validating that the update user function works as expected.
     * Asserts should cause the test to fail, removing the need for any return value as success is expected.
     *
     * @param originalEntity a UserEntity object that contains the original values to be compared against
     * @param updateRequest a UserUpdateRequest object that contains the values to confirm as changed
     * @param newEntity a UserEntity object that will be tested against originalEntity and updateRequest for changes
     */
    private void validateUserUpdate(
            UserEntity originalEntity, UserUpdateRequest updateRequest, UserEntity newEntity) {

        // Check first name
        if (updateRequest.getFirstName() != null) {
            // In update request, should be changed
            assertEquals(updateRequest.getFirstName(), newEntity.getFirstName());
        } else {
            // Not in update request, should match original
            assertEquals(originalEntity.getFirstName(), newEntity.getFirstName());
        }

        // Check email
        if (updateRequest.getEmail() != null) {
            // In update request, should be changed
            assertEquals(updateRequest.getEmail(), newEntity.getEmail());
        } else {
            // Not in update request, should match original
            assertEquals(originalEntity.getEmail(), newEntity.getEmail());
        }

        // Also validate that other parameters exist and are unchanged
        assertEquals(newEntity.getId(), originalEntity.getId());
        assertEquals(0, newEntity.getRoles().size());
    }
}
