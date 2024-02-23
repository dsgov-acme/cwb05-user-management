package io.nuvalence.user.management.api.service.enums;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Enumeration of user types.
 */
public enum PreferredCommunicationMethod {
    SMS("Sms"),
    EMAIL("Email"),
    NOT_ALLOWED("Not Allowed");

    @JsonValue private final String value;

    PreferredCommunicationMethod(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return this.value;
    }

    /**
     * Converts between strings and PreferredCommunicationMethod enum.
     *
     * @param text the text representation of the enum.
     * @return an enum value.
     */
    public static PreferredCommunicationMethod fromText(String text) {
        for (PreferredCommunicationMethod preferredCommunicationMethod :
                PreferredCommunicationMethod.values()) {
            if (preferredCommunicationMethod.toString().equalsIgnoreCase(text)) {
                return preferredCommunicationMethod;
            }
        }

        return NOT_ALLOWED;
    }
}
