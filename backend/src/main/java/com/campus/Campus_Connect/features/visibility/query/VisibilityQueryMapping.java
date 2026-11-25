package com.campus.Campus_Connect.features.visibility.query;

import com.campus.Campus_Connect.features.visibility.domain.VisibilityScope;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Root;

import java.util.Objects;
import java.util.function.Function;

/**
 * Feature-owned mapping from a resource entity to its owner and persisted visibility fields.
 * The mapping adapts storage representations; visibility meaning remains in
 * {@link VisibilitySpecifications}.
 */
public record VisibilityQueryMapping<T>(
        Function<Root<T>, Expression<Integer>> ownerId,
        Function<Root<T>, Expression<?>> visibilityType,
        Function<Root<T>, Expression<String>> visibilityValue,
        Function<VisibilityScope, Object> scopeTypeValue,
        Object publicTypeValue
) {
    public VisibilityQueryMapping {
        Objects.requireNonNull(ownerId, "ownerId mapping is required");
        Objects.requireNonNull(visibilityType, "visibilityType mapping is required");
        Objects.requireNonNull(visibilityValue, "visibilityValue mapping is required");
        Objects.requireNonNull(scopeTypeValue, "scopeTypeValue mapping is required");
    }
}
