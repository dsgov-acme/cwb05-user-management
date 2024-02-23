package io.nuvalence.user.management.api.service.enums;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Order in which to sort paginated results.
 */
public enum SortOrder {
    ASC("ASC"),
    DESC("DESC");

    @JsonValue private final String order;

    SortOrder(String type) {
        this.order = type;
    }

    @Override
    public String toString() {
        return this.order;
    }

    /**
     * Converts between strings and SortOrder enum.
     *
     * @param text the text representation of the enum.
     * @return an enum value.
     */
    public static SortOrder fromText(String text) {
        for (SortOrder order : SortOrder.values()) {
            if (order.toString().equalsIgnoreCase(text)) {
                return order;
            }
        }

        return ASC;
    }
}
