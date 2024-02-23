package io.nuvalence.user.management.api.service.service;

import io.nuvalence.user.management.api.service.entity.RoleEntity;
import io.nuvalence.user.management.api.service.entity.UserEntity;
import io.nuvalence.user.management.api.service.enums.UserType;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.Builder;
import lombok.Value;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents set of searchable parameters for User queries.
 */
@Value
@Builder
public class UserSearchCriteria implements Specification<UserEntity> {
    private static final long serialVersionUID = -6330775233536615741L;

    String email;
    String externalId;
    String name;
    String identityProvider;
    String userType;
    @Builder.Default Boolean includeDeleted = false;
    List<String> roleIds;
    List<String> roleNames;

    @SuppressWarnings("checkstyle:CyclomaticComplexity")
    @Override
    public Predicate toPredicate(
            Root<UserEntity> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
        List<Predicate> predicates = new ArrayList<>();

        if (StringUtils.isNotBlank(email) && !Objects.equals(email, name)) {
            predicates.add(criteriaBuilder.equal(root.get("email"), email));
        }

        if (StringUtils.isNotBlank(externalId)) {
            predicates.add(criteriaBuilder.equal(root.get("externalId"), externalId));
        }

        if (StringUtils.isNotBlank(name)) {

            List<Predicate> namePredicates = new ArrayList<>();

            if (Objects.equals(email, name)) {
                Predicate startsWithEmail =
                        criteriaBuilder.like(
                                criteriaBuilder.lower(root.get("email")),
                                name.toLowerCase(Locale.ROOT) + "%");
                namePredicates.add(startsWithEmail);
            }

            Predicate startsWithFullName =
                    criteriaBuilder.like(
                            criteriaBuilder.lower(root.get("fullName")),
                            name.toLowerCase(Locale.ROOT) + "%");

            Predicate startsWithFirstName =
                    criteriaBuilder.like(
                            criteriaBuilder.lower(root.get("firstName")),
                            name.toLowerCase(Locale.ROOT) + "%");

            Predicate startsWithLastName =
                    criteriaBuilder.like(
                            criteriaBuilder.lower(root.get("lastName")),
                            name.substring(name.lastIndexOf(" ") + 1).toLowerCase(Locale.ROOT)
                                    + "%");

            namePredicates.addAll(
                    Arrays.asList(startsWithFullName, startsWithFirstName, startsWithLastName));
            predicates.add(criteriaBuilder.or(namePredicates.toArray(new Predicate[0])));
        }

        if (StringUtils.isNotBlank(identityProvider)) {
            predicates.add(criteriaBuilder.equal(root.get("identityProvider"), identityProvider));
        }

        UserType enumUserType = UserType.fromText(userType);
        if (userType != null && enumUserType != null) {
            predicates.add(
                    criteriaBuilder.equal(
                            criteriaBuilder.lower(root.get("userType").as(String.class)),
                            enumUserType.toString()));
        }

        if (includeDeleted != null && includeDeleted) {
            predicates.add(criteriaBuilder.or(root.get("deleted").in(false, true)));
        } else {
            predicates.add(criteriaBuilder.equal(root.get("deleted"), false));
        }

        if (roleIds != null && !roleIds.isEmpty()) {
            Join<RoleEntity, UserEntity> roleUserJoin = root.joinList("roles");
            predicates.add(
                    criteriaBuilder.or(
                            roleIds.stream()
                                    .map(
                                            roleId ->
                                                    criteriaBuilder.equal(
                                                            roleUserJoin.get("id"),
                                                            UUID.fromString(roleId)))
                                    .toArray(Predicate[]::new)));
        } else if (roleNames != null && !roleNames.isEmpty()) {
            Join<RoleEntity, UserEntity> roleUserJoin = root.joinList("roles");
            predicates.add(
                    criteriaBuilder.or(
                            roleNames.stream()
                                    .map(
                                            roleName ->
                                                    criteriaBuilder.equal(
                                                            roleUserJoin.get("name"), roleName))
                                    .toArray(Predicate[]::new)));
        }

        return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
    }
}
