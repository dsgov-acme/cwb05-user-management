package io.nuvalence.user.management.api.service.models;

import io.nuvalence.user.management.api.service.entity.profile.IndividualProfile;
import io.nuvalence.user.management.api.service.service.UserService;
import jakarta.persistence.criteria.Predicate;
import lombok.Builder;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;

/**
 * Filters for individual profiles.
 */
@Getter
public class IndividualFilters extends BaseFilters {

    private final String ssn;
    private final String email;
    private final String name;
    private final UserService userService;

    @SuppressWarnings("java:S107")
    @Builder
    public IndividualFilters(
            String sortBy,
            String sortOrder,
            Integer pageNumber,
            Integer pageSize,
            String ssn,
            String email,
            String name,
            UserService userService) {
        super(sortBy, sortOrder, pageNumber, pageSize);
        this.name = name;
        this.ssn = ssn;
        this.email = email;
        this.userService = userService;
    }

    public Specification<IndividualProfile> getIndividualProfileSpecification() {
        return (root, query, criteriaBuilder) -> {
            var predicates = new ArrayList<>();

            if (StringUtils.isNotBlank(this.name)) {
                predicates.add(criteriaBuilder.equal(root.get("firstName"), this.name));
                predicates.add(criteriaBuilder.equal(root.get("middleName"), this.name));
                predicates.add(criteriaBuilder.equal(root.get("lastName"), this.name));
            }

            if (StringUtils.isNotBlank(ssn)) {
                predicates.add(criteriaBuilder.equal(root.get("ssn"), this.ssn));
            }

            if (StringUtils.isNotBlank(this.email)) {
                predicates.add(criteriaBuilder.equal(root.get("email"), this.email));
            }
            return !predicates.isEmpty()
                    ? criteriaBuilder.or(predicates.toArray(new Predicate[0]))
                    : null;
        };
    }
}
