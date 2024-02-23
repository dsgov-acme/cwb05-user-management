package io.nuvalence.user.management.api.service.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import io.nuvalence.user.management.api.service.entity.profile.Address;
import org.junit.jupiter.api.Test;

import java.util.UUID;

@SuppressWarnings("PMD.AvoidDuplicateLiterals")
class AddressTest {
    @Test
    void testEqualsWithSameFields() {
        UUID addressId = UUID.randomUUID();
        Address address1 =
                Address.builder()
                        .id(addressId)
                        .address1("123 Main St")
                        .address2("Apt 4")
                        .city("Springfield")
                        .state("IL")
                        .postalCode("62701")
                        .country("USA")
                        .county("Sangamon")
                        .build();

        Address address2 =
                Address.builder()
                        .id(addressId)
                        .address1("123 Main St")
                        .address2("Apt 4")
                        .city("Springfield")
                        .state("IL")
                        .postalCode("62701")
                        .country("USA")
                        .county("Sangamon")
                        .build();

        assertEquals(address1, address2);
    }

    @Test
    void testNotEqualsWithDifferentFields() {
        Address address1 = Address.builder().id(UUID.randomUUID()).address1("123 Main St").build();

        Address address2 = Address.builder().id(UUID.randomUUID()).address1("456 Elm St").build();

        assertNotEquals(address1, address2);
    }

    @Test
    void testHashCodeConsistency() {
        Address address = Address.builder().id(UUID.randomUUID()).address1("123 Main St").build();

        int initialHashCode = address.hashCode();
        int subsequentHashCode = address.hashCode();

        assertEquals(initialHashCode, subsequentHashCode);
    }
}
