package io.nuvalence.user.management.api.service.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import io.nuvalence.user.management.api.service.entity.profile.IndividualProfile;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.UUID;

@SuppressWarnings("PMD.AvoidDuplicateLiterals")
class IndividualProfileTest {

    @Test
    void testEqualsWithSameFields() {
        UUID id = UUID.randomUUID();
        OffsetDateTime now = OffsetDateTime.now();

        IndividualProfile individual1 =
                IndividualProfile.builder()
                        .id(id)
                        .ssn("123-45-6789")
                        .firstName("John")
                        .middleName("F.")
                        .lastName("Doe")
                        .email("john.doe@example.com")
                        .phoneNumber("123-456-7890")
                        .createdBy("user1")
                        .lastUpdatedBy("user2")
                        .createdTimestamp(now)
                        .lastUpdatedTimestamp(now)
                        .build();

        IndividualProfile individual2 =
                IndividualProfile.builder()
                        .id(id)
                        .ssn("123-45-6789")
                        .firstName("John")
                        .middleName("F.")
                        .lastName("Doe")
                        .email("john.doe@example.com")
                        .phoneNumber("123-456-7890")
                        .createdBy("user1")
                        .lastUpdatedBy("user2")
                        .createdTimestamp(now)
                        .lastUpdatedTimestamp(now)
                        .build();

        assertEquals(individual1, individual2);
    }

    @Test
    void testNotEqualsWithDifferentFields() {
        IndividualProfile individual1 =
                IndividualProfile.builder().id(UUID.randomUUID()).ssn("123-45-6789").build();

        IndividualProfile individual2 =
                IndividualProfile.builder()
                        .id(UUID.randomUUID()) // Different ID
                        .ssn("987-65-4321") // Different SSN
                        .build();

        assertNotEquals(individual1, individual2);
    }

    @Test
    void testHashCodeConsistency() {
        UUID id = UUID.randomUUID();
        IndividualProfile individual =
                IndividualProfile.builder().id(id).ssn("123-45-6789").build();

        int initialHashCode = individual.hashCode();
        int subsequentHashCode = individual.hashCode();

        assertEquals(initialHashCode, subsequentHashCode);
    }
}
