package com.campus.Campus_Connect.features.visibility.query;

import com.campus.Campus_Connect.features.connection.entity.ConnectionStatus;
import com.campus.Campus_Connect.features.connection.entity.UserConnection;
import com.campus.Campus_Connect.features.visibility.domain.VisibilityContext;
import com.campus.Campus_Connect.features.visibility.domain.VisibilityScope;
import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import java.util.ArrayList;
import java.util.List;

/** Builds composable database-side predicates for centralized visibility policies. */
public final class VisibilitySpecifications {

    private static final int MIN_SUPPORTED_ACADEMIC_YEAR = 1;
    private static final int MAX_SUPPORTED_ACADEMIC_YEAR = 4;

    private VisibilitySpecifications() {
    }

    /**
     * Returns eligible resources for the supplied trusted viewer context. Combine this
     * Specification with feature filters and pass it to a {@code JpaSpecificationExecutor}
     * along with a {@code Pageable}; visibility is then applied before sorting and pagination.
     */
    public static <T> Specification<T> visibleTo(
            VisibilityContext context,
            VisibilityQueryMapping<T> mapping
    ) {
        if (context == null || mapping == null) {
            throw new IllegalArgumentException("Viewer context and query mapping are required.");
        }

        return (root, query, builder) -> {
            Expression<?> type = mapping.visibilityType().apply(root);
            Expression<String> value = mapping.visibilityValue().apply(root);
            List<Predicate> eligiblePolicies = new ArrayList<>();

            Predicate publicPolicy = mapping.publicTypeValue() == null
                    ? builder.isNull(type)
                    : builder.equal(type, mapping.publicTypeValue());
            eligiblePolicies.add(builder.and(publicPolicy, builder.isNull(value)));

            addConnectionsPolicy(eligiblePolicies, root, query, builder, context, mapping, type, value);
            addValuePolicy(eligiblePolicies, builder, mapping, type, value,
                    VisibilityScope.CLUB, clubValues(context));
            addValuePolicy(eligiblePolicies, builder, mapping, type, value,
                    VisibilityScope.HOSTEL, singletonValue(context.hostelId()));
            addValuePolicy(eligiblePolicies, builder, mapping, type, value,
                    VisibilityScope.COURSE, singletonValue(context.courseId()));
            addAcademicYearPolicy(eligiblePolicies, builder, context, mapping, type, value);

            return builder.or(eligiblePolicies.toArray(Predicate[]::new));
        };
    }

    private static <T> void addConnectionsPolicy(
            List<Predicate> policies,
            Root<T> root,
            jakarta.persistence.criteria.CriteriaQuery<?> query,
            CriteriaBuilder builder,
            VisibilityContext context,
            VisibilityQueryMapping<T> mapping,
            Expression<?> type,
            Expression<String> value
    ) {
        Subquery<Integer> connectionExists = query.subquery(Integer.class);
        Root<UserConnection> connection = connectionExists.from(UserConnection.class);
        Path<Integer> senderId = connection.get("id").get("senderId");
        Path<Integer> receiverId = connection.get("id").get("receiverId");
        Expression<Integer> ownerId = mapping.ownerId().apply(root);

        Predicate forwardPair = builder.and(
                builder.equal(senderId, ownerId),
                builder.equal(receiverId, context.viewerId())
        );
        Predicate reversePair = builder.and(
                builder.equal(senderId, context.viewerId()),
                builder.equal(receiverId, ownerId)
        );
        connectionExists.select(builder.literal(1))
                .where(
                        builder.equal(connection.get("status"), ConnectionStatus.CONNECTED),
                        builder.or(forwardPair, reversePair)
                );

        policies.add(builder.and(
                builder.equal(type, requiredTypeValue(mapping, VisibilityScope.CONNECTIONS)),
                builder.isNull(value),
                builder.exists(connectionExists)
        ));
    }

    private static <T> void addValuePolicy(
            List<Predicate> policies,
            CriteriaBuilder builder,
            VisibilityQueryMapping<T> mapping,
            Expression<?> type,
            Expression<String> value,
            VisibilityScope scope,
            List<String> eligibleValues
    ) {
        if (eligibleValues.isEmpty()) {
            return;
        }
        policies.add(builder.and(
                builder.equal(type, requiredTypeValue(mapping, scope)),
                value.in(eligibleValues)
        ));
    }

    private static <T> Object requiredTypeValue(
            VisibilityQueryMapping<T> mapping,
            VisibilityScope scope
    ) {
        Object value = mapping.scopeTypeValue().apply(scope);
        if (value == null) {
            throw new IllegalArgumentException("No persisted visibility type is mapped for " + scope + ".");
        }
        return value;
    }

    private static <T> void addAcademicYearPolicy(
            List<Predicate> policies,
            CriteriaBuilder builder,
            VisibilityContext context,
            VisibilityQueryMapping<T> mapping,
            Expression<?> type,
            Expression<String> value
    ) {
        List<String> eligibleYears = new ArrayList<>(2);
        Integer academicYear = context.academicYear();
        if (academicYear != null
                && academicYear >= MIN_SUPPORTED_ACADEMIC_YEAR
                && academicYear <= MAX_SUPPORTED_ACADEMIC_YEAR) {
            eligibleYears.add(Integer.toString(academicYear));
        }
        if (Boolean.TRUE.equals(context.alumni())) {
            eligibleYears.add("ALUMNI");
        }
        addValuePolicy(policies, builder, mapping, type, value,
                VisibilityScope.ACADEMIC_YEAR, eligibleYears);
    }

    private static List<String> clubValues(VisibilityContext context) {
        return context.approvedClubIds().stream()
                .filter(id -> id != null && id > 0)
                .map(String::valueOf)
                .toList();
    }

    private static List<String> singletonValue(Integer id) {
        return id == null || id <= 0 ? List.of() : List.of(Integer.toString(id));
    }
}
