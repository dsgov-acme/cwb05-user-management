package io.nuvalence.user.management.api.service.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.nuvalence.user.management.api.service.entity.profile.EmployerProfile;
import io.nuvalence.user.management.api.service.models.EmployerFilters;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

@ExtendWith(MockitoExtension.class)
public class EmployerFiltersTest {
    @InjectMocks private EmployerFilters employerFilters;

    @Mock private Root<EmployerProfile> root;

    @Mock private Path<String> pathExpression;

    @Mock private CriteriaQuery<?> query;

    @Mock private CriteriaBuilder criteriaBuilder;

    @Captor private ArgumentCaptor<Predicate> predicateCaptor;

    @Test
    void testGetIndividualProfileSpecificationWithFein() {
        employerFilters = new EmployerFilters(null, null, null, null, "fein", null, null, null);
        Predicate firstPredicate = mock(Predicate.class);
        Predicate finalPredicate = mock(Predicate.class);

        when(root.<String>get("fein")).thenReturn(pathExpression);
        when(criteriaBuilder.equal(pathExpression, employerFilters.getFein()))
                .thenReturn(firstPredicate);
        when(criteriaBuilder.or(any())).thenReturn(finalPredicate);

        // Execute
        Specification<EmployerProfile> specification =
                employerFilters.getEmployerProfileSpecification();
        Predicate result = specification.toPredicate(root, query, criteriaBuilder);

        // Verify
        Assertions.assertEquals(finalPredicate, result);
        verify(criteriaBuilder).or(predicateCaptor.capture());

        var value = predicateCaptor.getValue();
        Assertions.assertEquals(firstPredicate, value);
    }

    @Test
    void testGetIndividualProfileSpecificationWithType() {
        employerFilters = new EmployerFilters(null, null, null, null, null, null, "type", null);
        Predicate firstPredicate = mock(Predicate.class);
        Predicate finalPredicate = mock(Predicate.class);

        when(root.<String>get("type")).thenReturn(pathExpression);
        when(criteriaBuilder.equal(pathExpression, employerFilters.getType()))
                .thenReturn(firstPredicate);
        when(criteriaBuilder.or(any())).thenReturn(finalPredicate);

        // Execute
        Specification<EmployerProfile> specification =
                employerFilters.getEmployerProfileSpecification();
        Predicate result = specification.toPredicate(root, query, criteriaBuilder);

        // Verify
        Assertions.assertEquals(finalPredicate, result);
        verify(criteriaBuilder).or(predicateCaptor.capture());

        var value = predicateCaptor.getValue();
        Assertions.assertEquals(firstPredicate, value);
    }

    @Test
    void testGetIndividualProfileSpecificationWithIndustry() {
        employerFilters = new EmployerFilters(null, null, null, null, null, null, null, "industry");
        Predicate firstPredicate = mock(Predicate.class);
        Predicate finalPredicate = mock(Predicate.class);

        when(root.<String>get("industry")).thenReturn(pathExpression);
        when(criteriaBuilder.like(null, "%industry%")).thenReturn(firstPredicate);
        when(criteriaBuilder.or(any())).thenReturn(finalPredicate);

        // Execute
        Specification<EmployerProfile> specification =
                employerFilters.getEmployerProfileSpecification();
        Predicate result = specification.toPredicate(root, query, criteriaBuilder);

        // Verify
        Assertions.assertEquals(finalPredicate, result);
        verify(criteriaBuilder).or(predicateCaptor.capture());

        var value = predicateCaptor.getValue();
        Assertions.assertEquals(firstPredicate, value);
    }

    @Test
    void testGetIndividualProfileSpecificationWithName() {
        employerFilters = new EmployerFilters(null, null, null, null, null, "name", null, null);
        Predicate firstPredicate = mock(Predicate.class);
        Predicate finalPredicate = mock(Predicate.class);

        when(root.<String>get("legalName")).thenReturn(pathExpression);
        when(criteriaBuilder.like(null, "%name%")).thenReturn(firstPredicate);

        Subquery<String> otherNamesSubquery = mock(Subquery.class);
        when(query.subquery(String.class)).thenReturn(otherNamesSubquery);
        Root<EmployerProfile> subqueryRoot = mock(Root.class);
        when(otherNamesSubquery.correlate(root)).thenReturn(subqueryRoot);
        Join otherNamesJoin = mock(Join.class);
        when(subqueryRoot.join("otherNames", jakarta.persistence.criteria.JoinType.LEFT))
                .thenReturn(otherNamesJoin);
        when(criteriaBuilder.exists(otherNamesSubquery)).thenReturn(finalPredicate);
        when(criteriaBuilder.or(any())).thenReturn(finalPredicate);

        // Execute
        Specification<EmployerProfile> specification =
                employerFilters.getEmployerProfileSpecification();
        Predicate result = specification.toPredicate(root, query, criteriaBuilder);

        // Verify
        Assertions.assertEquals(finalPredicate, result);
        verify(criteriaBuilder).or(predicateCaptor.capture());

        var value = predicateCaptor.getValue();
        Assertions.assertEquals(finalPredicate, value);
    }
}
