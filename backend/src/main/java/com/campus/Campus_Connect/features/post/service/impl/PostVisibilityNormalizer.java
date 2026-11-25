package com.campus.Campus_Connect.features.post.service.impl;

import com.campus.Campus_Connect.common.exception.BadRequestException;
import com.campus.Campus_Connect.features.visibility.domain.VisibilityPolicy;
import com.campus.Campus_Connect.features.visibility.domain.VisibilityScope;
import com.campus.Campus_Connect.features.visibility.validation.VisibilityPolicyValidator;

import java.util.Locale;

/** Validates the creation-time scope/value pair and converts it to the stored representation. */
final class PostVisibilityNormalizer {

    private PostVisibilityNormalizer() {
    }

    static NormalizedVisibility normalize(boolean eventAssociated, String requestedType, String requestedValue) {
        if (eventAssociated) {
            return new NormalizedVisibility(null, null);
        }

        String type = requestedType == null || requestedType.isBlank()
                ? null
                : requestedType.trim().toUpperCase(Locale.ROOT);
        String value = requestedValue == null || requestedValue.isBlank()
                ? null
                : requestedValue.trim();

        if (type == null) {
            if (value != null) {
                throw new BadRequestException("A visibility value requires a visibility type.");
            }
            return new NormalizedVisibility(null, null);
        }

        if ("PUBLIC".equals(type)) {
            if (value != null) {
                throw new BadRequestException("PUBLIC visibility does not accept a value.");
            }
            return new NormalizedVisibility(null, null);
        }

        VisibilityScope scope;
        try {
            scope = VisibilityScope.valueOf(type);
        } catch (IllegalArgumentException exception) {
            throw new BadRequestException("Unsupported post visibility type.");
        }
        VisibilityPolicy policy = VisibilityPolicyValidator.normalize(new VisibilityPolicy(scope, value));
        return new NormalizedVisibility(policy.scope().name(), policy.scopeValue());
    }

    record NormalizedVisibility(String type, String value) {
    }
}
