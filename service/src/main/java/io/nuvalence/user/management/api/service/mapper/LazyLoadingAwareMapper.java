package io.nuvalence.user.management.api.service.mapper;

import org.hibernate.Hibernate;
import org.mapstruct.Condition;

import java.util.Collection;

/**
 * Adds a condition that blocks mapping of Hibernate lazy loaded collections that have not been loaded.
 */
public interface LazyLoadingAwareMapper {

    /**
     * Blocks mapping of Hibernate lazy loaded collections that have not been loaded.
     *
     * @param sourceCollection collection to evaluate
     * @return true if collection can be mapped, false otherwise
     */
    @Condition
    default boolean isNotLazyLoaded(Collection<?> sourceCollection) {
        // Case: Source field in domain object is lazy: Skip mapping
        // Continue Mapping
        return Hibernate.isInitialized(sourceCollection);

        // Skip mapping
    }
}
