package io.nuvalence.user.management.api.service.util.jpa;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.nuvalence.user.management.api.service.enums.UserType;
import org.junit.jupiter.api.Test;

class UserTypePersistanceConverterTest {

    private final UserTypePersistanceConverter userTypePersistanceConverter =
            new UserTypePersistanceConverter();

    @Test
    void testConvertToDatabaseColumn() {
        UserType entityValue = UserType.AGENCY;
        String result = userTypePersistanceConverter.convertToDatabaseColumn(entityValue);
        assertEquals(entityValue.toString(), result);
    }

    @Test
    void testConvertToEntityAttribute() {
        String databaseValue = "Agency";
        UserType result = userTypePersistanceConverter.convertToEntityAttribute(databaseValue);
        assertEquals(UserType.AGENCY, result);
    }
}
