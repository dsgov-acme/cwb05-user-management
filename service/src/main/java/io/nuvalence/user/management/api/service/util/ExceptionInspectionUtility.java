package io.nuvalence.user.management.api.service.util;

import java.util.Optional;

/**
 * Utility for inspecting exception data.
 */
public class ExceptionInspectionUtility {

    // Private constructor to prevent instantiation
    private ExceptionInspectionUtility() {
        throw new AssertionError("Utility class should not be instantiated");
    }

    /**
     * Returns an Optional containing the throwable cause if a throwable of the provided type exists in the cause chain.
     *
     * @param <T> Target throwable type to search for
     * @param t Throwable to inspect
     * @param targetType Target throwable type to search for
     * @return Optional containing the throwable cause if a throwable of the provided type exists in the cause chain
     */
    public static <T extends Throwable> Optional<T> findCauseOfType(
            final Throwable t, final Class<T> targetType) {
        Throwable current = t;
        while (current != null) {
            if (targetType.isInstance(current)) {
                return Optional.of(targetType.cast(current));
            }
            current = current.getCause();
        }

        return Optional.empty();
    }
}
