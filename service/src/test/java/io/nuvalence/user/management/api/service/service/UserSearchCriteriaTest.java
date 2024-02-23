package io.nuvalence.user.management.api.service.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.nuvalence.auth.access.AuthorizationHandler;
import io.nuvalence.user.management.api.service.entity.PublicUser;
import io.nuvalence.user.management.api.service.entity.RoleEntity;
import io.nuvalence.user.management.api.service.entity.UserEntity;
import io.nuvalence.user.management.api.service.enums.UserType;
import io.nuvalence.user.management.api.service.repository.RoleRepository;
import io.nuvalence.user.management.api.service.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Transactional
class UserSearchCriteriaTest {
    private static final String LAST_NAME = "Simpson";
    @MockBean private AuthorizationHandler authorizationHandler;

    @Autowired private UserRepository repository;
    @Autowired private RoleRepository roleRepository;

    private Pageable defaultPageable;
    private UserEntity user1;
    private UserEntity user2;
    private UserEntity user3;
    private UserEntity user4;
    private RoleEntity role1;
    private RoleEntity role2;

    @BeforeEach
    void setUp() {
        role1 = roleRepository.save(createNewRole("NUCLEAR_TECH"));
        role2 = roleRepository.save(createNewRole("STUDENT"));
        defaultPageable = PageRequest.of(0, 10, Sort.by("email").ascending());
        user1 =
                repository.save(
                        createNewUser(
                                "Homer",
                                "Jay",
                                LAST_NAME,
                                "555-555-555",
                                "hsimpson@springfieldnuclear.com",
                                "EXT-00001",
                                "springfield-nuclear",
                                UserType.AGENCY,
                                false,
                                List.of(role1)));
        user2 =
                repository.save(
                        createNewUser(
                                "Lisa",
                                "Marie",
                                LAST_NAME,
                                "666-666-666",
                                "lsimpson@springfieldelementary.edu",
                                "EXT-00002",
                                "springfield-elementary",
                                UserType.PUBLIC,
                                false,
                                List.of(role2)));
        user3 =
                repository.save(
                        createNewUser(
                                "Barth",
                                "Jojo",
                                LAST_NAME,
                                "777-777-777",
                                "ccarlson@springfieldnuclear.com",
                                "EXT-00003",
                                "springfield-nuclear",
                                UserType.AGENCY,
                                false,
                                List.of(role1)));
        user4 =
                repository.save(
                        createNewUser(
                                "maggie",
                                "Dojo",
                                "Simptra",
                                "777-777-777",
                                "msimpson@springfieldnuclear.com",
                                "EXT-00004",
                                "springfield-nuclear",
                                UserType.AGENCY,
                                true,
                                List.of(role1)));
    }

    @AfterEach
    void cleanUp() {
        repository.deleteAll();
        roleRepository.delete(role1);
        roleRepository.delete(role2);
    }

    @Test
    void searchByEmail() {
        final UserSearchCriteria criteria =
                UserSearchCriteria.builder().email(user1.getEmail()).build();

        final Page<UserEntity> results = repository.findAll(criteria, defaultPageable);

        assertEquals(1, results.getTotalElements());
        assertEquals(user1.getId(), results.toList().get(0).getId());
    }

    @Test
    void searchByFirstName() {
        final UserSearchCriteria criteria =
                UserSearchCriteria.builder().name(user1.getFirstName()).build();

        final Page<UserEntity> results = repository.findAll(criteria, defaultPageable);

        assertEquals(1, results.getTotalElements());
        assertEquals(user1.getFirstName(), results.toList().get(0).getFirstName());
        assertEquals(user1.getId(), results.toList().get(0).getId());
    }

    @Test
    void searchByLastName() {
        final UserSearchCriteria criteria =
                UserSearchCriteria.builder().name(user1.getLastName()).build();

        final Page<UserEntity> results = repository.findAll(criteria, defaultPageable);

        assertEquals(3, results.getTotalElements());
        // Extract last names from UserEntity objects in the results
        List<String> lastNamesInResults =
                results.getContent().stream().map(UserEntity::getLastName).toList();

        // Validate that LAST_NAME exists in all the extracted last names
        for (String lastName : lastNamesInResults) {
            assertEquals(LAST_NAME, lastName);
        }
    }

    @Test
    void searchByFullName() {
        final UserSearchCriteria criteria =
                UserSearchCriteria.builder()
                        .name(user1.getFirstName().concat(" ").concat("Simp"))
                        .build();

        final Page<UserEntity> results = repository.findAll(criteria, defaultPageable);

        assertEquals(3, results.getTotalElements());
        // Extract last names from UserEntity objects in the results
        List<String> lastNamesInResults =
                results.getContent().stream().map(UserEntity::getLastName).toList();

        // Validate that last name starts with Simp all the extracted last names
        for (String lastName : lastNamesInResults) {
            assertTrue(lastName.startsWith("Simp"));
        }
    }

    @Test
    void searchByIdentityProviderAndExternalId() {
        final UserSearchCriteria criteria =
                UserSearchCriteria.builder()
                        .identityProvider(user2.getIdentityProvider())
                        .externalId(user2.getExternalId())
                        .build();

        final Page<UserEntity> results = repository.findAll(criteria, defaultPageable);

        assertEquals(1, results.getTotalElements());
        assertEquals(user2.getId(), results.toList().get(0).getId());
    }

    @Test
    void searchByIdentityProvider() {
        final UserSearchCriteria criteria =
                UserSearchCriteria.builder().identityProvider(user1.getIdentityProvider()).build();

        final Page<UserEntity> results = repository.findAll(criteria, defaultPageable);

        assertEquals(2, results.getTotalElements());
        assertEquals(user3.getId(), results.toList().get(0).getId());
        assertEquals(user1.getId(), results.toList().get(1).getId());
    }

    @Test
    void searchByRoleName() {
        final UserSearchCriteria criteria =
                UserSearchCriteria.builder().roleNames(List.of(role1.getName())).build();

        final Page<UserEntity> results = repository.findAll(criteria, defaultPageable);

        assertEquals(2, results.getTotalElements());
        assertEquals(user3.getId(), results.toList().get(0).getId());
        assertEquals(user1.getId(), results.toList().get(1).getId());
    }

    @Test
    void searchByRoleId() {
        final UserSearchCriteria criteria =
                UserSearchCriteria.builder().roleIds(List.of(role2.getId().toString())).build();

        final Page<UserEntity> results = repository.findAll(criteria, defaultPageable);

        assertEquals(1, results.getTotalElements());
        assertEquals(user2.getId(), results.toList().get(0).getId());
    }

    @Test
    void searchByRoleIdPreferredOverName() {
        final UserSearchCriteria criteria =
                UserSearchCriteria.builder()
                        .roleIds(List.of(role2.getId().toString()))
                        .roleNames(List.of(role1.getName()))
                        .build();

        final Page<UserEntity> results = repository.findAll(criteria, defaultPageable);

        assertEquals(1, results.getTotalElements());
        assertEquals(user2.getId(), results.toList().get(0).getId());
    }

    @Test
    void searchByincludeDeletedUsers() {
        final UserSearchCriteria criteria =
                UserSearchCriteria.builder().includeDeleted(true).build();

        final Page<UserEntity> results = repository.findAll(criteria, defaultPageable);

        assertEquals(4, results.getTotalElements());
    }

    @Test
    void searchByincludeDeletedUsersFalse() {
        final UserSearchCriteria criteria =
                UserSearchCriteria.builder().includeDeleted(false).build();

        final Page<UserEntity> results = repository.findAll(criteria, defaultPageable);

        assertEquals(3, results.getTotalElements());
    }

    private UserEntity createNewUser(
            final String firstName,
            final String middleName,
            final String lastName,
            final String phoneNumber,
            final String email,
            final String externalId,
            final String identityProvider,
            final UserType type,
            final boolean deleted,
            final List<RoleEntity> roles) {
        final UserEntity user = new PublicUser();
        user.setFirstName(firstName);
        user.setMiddleName(middleName);
        user.setLastName(lastName);
        user.setPhoneNumber(phoneNumber);
        user.setEmail(email);
        user.setExternalId(externalId);
        user.setIdentityProvider(identityProvider);
        user.setUserType(type);
        user.setRoles(roles);
        user.setDeleted(deleted);
        user.setDeletedOn(null);

        return user;
    }

    private RoleEntity createNewRole(final String name) {
        final RoleEntity role = new RoleEntity();
        role.setName(name);

        return role;
    }
}
