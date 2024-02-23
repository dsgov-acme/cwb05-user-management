package io.nuvalence.user.management.api.service.util.jpa;

import io.nuvalence.user.management.api.service.enums.UserType;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * JPA converter to map UserType class to db column.
 */
@Converter(autoApply = true)
public class UserTypePersistanceConverter implements AttributeConverter<UserType, String> {
    @Override
    public String convertToDatabaseColumn(UserType entityValue) {
        return (entityValue == null) ? null : entityValue.toString();
    }

    @Override
    public UserType convertToEntityAttribute(String databaseValue) {
        return UserType.fromText(databaseValue);
    }
}
