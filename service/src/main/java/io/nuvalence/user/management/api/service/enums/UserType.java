package io.nuvalence.user.management.api.service.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * Enumeration of user types.
 */
public enum UserType {
    AGENCY("agency"),
    PUBLIC("public"),
    UNKNOWN("unknown");

    @JsonValue @Getter private final String value;

    UserType(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return this.value;
    }

    /**
     * Converts between strings and UserType enum.
     *
     * @param text the text representation of the enum.
     * @return an enum value.
     */
    public static UserType fromText(String text) {
        for (UserType userType : UserType.values()) {
            if (userType.toString().equalsIgnoreCase(text)) {
                return userType;
            }
        }

        return UNKNOWN;
    }
}
