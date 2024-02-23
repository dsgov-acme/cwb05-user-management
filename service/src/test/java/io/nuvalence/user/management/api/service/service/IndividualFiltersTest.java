package io.nuvalence.user.management.api.service.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.nuvalence.user.management.api.service.entity.profile.IndividualProfile;
import io.nuvalence.user.management.api.service.models.IndividualFilters;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
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
class IndividualFiltersTest {
    @InjectMocks private IndividualFilters individualFilters;

    @Mock private Root<IndividualProfile> root;

    @Mock private Path<String> pathExpression;

    @Mock private CriteriaQuery<?> query;

    @Mock private CriteriaBuilder criteriaBuilder;

    @Mock UserService userService;

    @Captor private ArgumentCaptor<Predicate> predicateCaptor;

    @Test
    void testGetIndividualProfileSpecificationWithSsn() {
        individualFilters =
                new IndividualFilters(null, null, null, null, "testssn", null, null, userService);
        Predicate firstPredicate = mock(Predicate.class);
        Predicate finalPredicate = mock(Predicate.class);

        when(root.<String>get("ssn")).thenReturn(pathExpression);
        when(criteriaBuilder.equal(pathExpression, individualFilters.getSsn()))
                .thenReturn(firstPredicate);
        when(criteriaBuilder.or(any())).thenReturn(finalPredicate);

        // Execute
        Specification<IndividualProfile> specification =
                individualFilters.getIndividualProfileSpecification();
        Predicate result = specification.toPredicate(root, query, criteriaBuilder);

        // Verify
        Assertions.assertEquals(finalPredicate, result);
        verify(criteriaBuilder).or(predicateCaptor.capture());

        var value = predicateCaptor.getValue();
        Assertions.assertEquals(firstPredicate, value);
    }

    @Test
    void testGetIndividualProfileSpecificationWithEmail() {
        individualFilters =
                new IndividualFilters(
                        null, null, null, null, null, "email@email.com", null, userService);
        Predicate firstPredicate = mock(Predicate.class);
        Predicate finalPredicate = mock(Predicate.class);

        when(root.<String>get("email")).thenReturn(pathExpression);
        when(criteriaBuilder.equal(pathExpression, individualFilters.getEmail()))
                .thenReturn(firstPredicate);
        when(criteriaBuilder.or(any())).thenReturn(finalPredicate);

        // Execute
        Specification<IndividualProfile> specification =
                individualFilters.getIndividualProfileSpecification();
        Predicate result = specification.toPredicate(root, query, criteriaBuilder);

        // Verify
        Assertions.assertEquals(finalPredicate, result);
        verify(criteriaBuilder).or(predicateCaptor.capture());

        var value = predicateCaptor.getValue();
        Assertions.assertEquals(firstPredicate, value);
    }

    @Test
    void testGetIndividualProfileSpecificationWithName() {
        individualFilters =
                new IndividualFilters(null, null, null, null, null, null, "name", userService);
        Predicate firstPredicate = mock(Predicate.class);
        Predicate finalPredicate = mock(Predicate.class);

        when(root.<String>get("firstName")).thenReturn(pathExpression);
        when(root.<String>get("middleName")).thenReturn(pathExpression);
        when(root.<String>get("lastName")).thenReturn(pathExpression);
        when(criteriaBuilder.equal(pathExpression, individualFilters.getName()))
                .thenReturn(firstPredicate);

        when(criteriaBuilder.or(any())).thenReturn(finalPredicate);

        // Execute
        Specification<IndividualProfile> specification =
                individualFilters.getIndividualProfileSpecification();
        Predicate result = specification.toPredicate(root, query, criteriaBuilder);

        // Verify
        Assertions.assertEquals(finalPredicate, result);
        verify(criteriaBuilder).or(predicateCaptor.capture());

        var value = predicateCaptor.getValue();
        Assertions.assertEquals(firstPredicate, value);
    }
}
