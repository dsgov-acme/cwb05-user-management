package io.nuvalence.user.management.api.service.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import io.nuvalence.user.management.api.service.entity.profile.EmployerProfile;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.UUID;

@SuppressWarnings("PMD.AvoidDuplicateLiterals")
class EmployerProfileTest {
    @Test
    void testEqualsWithSameFields() {
        UUID id = UUID.randomUUID();
        EmployerProfile employer1 =
                EmployerProfile.builder()
                        .id(id)
                        .fein("123456789")
                        .legalName("Acme Corporation")
                        .otherNames(Collections.singletonList("Acme"))
                        .type("LLC")
                        .industry("Retail")
                        .summaryOfBusiness("Retail Business")
                        .businessPhone("123-456-7890")
                        .build();

        EmployerProfile employer2 =
                EmployerProfile.builder()
                        .id(id)
                        .fein("123456789")
                        .legalName("Acme Corporation")
                        .otherNames(Collections.singletonList("Acme"))
                        .type("LLC")
                        .industry("Retail")
                        .summaryOfBusiness("Retail Business")
                        .businessPhone("123-456-7890")
                        .build();

        assertEquals(employer1, employer2);
    }

    @Test
    void testNotEqualsWithDifferentFields() {
        EmployerProfile employer1 =
                EmployerProfile.builder().id(UUID.randomUUID()).fein("123456789").build();

        EmployerProfile employer2 =
                EmployerProfile.builder().id(UUID.randomUUID()).fein("987654321").build();

        assertNotEquals(employer1, employer2);
    }

    @Test
    void testHashCodeConsistency() {
        EmployerProfile employer =
                EmployerProfile.builder().id(UUID.randomUUID()).fein("123456789").build();

        int initialHashCode = employer.hashCode();
        int subsequentHashCode = employer.hashCode();

        assertEquals(initialHashCode, subsequentHashCode);
    }
}
