package com.campus.Campus_Connect.features.visibility.validation;

import com.campus.Campus_Connect.common.exception.BadRequestException;
import com.campus.Campus_Connect.features.visibility.domain.VisibilityPolicy;
import com.campus.Campus_Connect.features.visibility.domain.VisibilityScope;

/** Validates policy structure only; it does not check entity existence or viewer eligibility. */
public final class VisibilityPolicyValidator {

    private VisibilityPolicyValidator() {
    }

    public static void validate(VisibilityPolicy policy) {
        normalize(policy);
    }

    /** Validates and returns canonical scope values for existing request normalization flows. */
    public static VisibilityPolicy normalize(VisibilityPolicy policy) {
        if (policy == null || policy.scope() == null) {
            throw new BadRequestException("A visibility scope is required.");
        }

        String value = policy.scopeValue() == null ? null : policy.scopeValue().trim();
        return switch (policy.scope()) {
            case CONNECTIONS -> {
                requireNoValue(policy.scope(), value);
                yield new VisibilityPolicy(policy.scope(), null);
            }
            case CLUB, HOSTEL, COURSE ->
                    new VisibilityPolicy(policy.scope(), normalizePositiveId(policy.scope(), value));
            case ACADEMIC_YEAR ->
                    new VisibilityPolicy(policy.scope(), normalizeAcademicYear(value));
        };
    }

    private static void requireNoValue(VisibilityScope scope, String value) {
        if (value != null && !value.isEmpty()) {
            throw new BadRequestException(scope + " visibility does not accept a value.");
        }
    }

    private static String normalizePositiveId(VisibilityScope scope, String value) {
        if (value == null || value.isEmpty()) {
            throw new BadRequestException(scope + " visibility requires an ID value.");
        }
        try {
            int id = Integer.parseInt(value);
            if (id <= 0) {
                throw new BadRequestException(scope + " visibility ID must be positive.");
            }
            return Integer.toString(id);
        } catch (NumberFormatException exception) {
            throw new BadRequestException(scope + " visibility ID must be a positive integer.");
        }
    }

    private static String normalizeAcademicYear(String value) {
        if (value == null || value.isEmpty()) {
            throw new BadRequestException("ACADEMIC_YEAR visibility requires a year value.");
        }
        if ("ALUMNI".equalsIgnoreCase(value)) {
            return "ALUMNI";
        }
        if (value.equals("1") || value.equals("2") || value.equals("3") || value.equals("4")) {
            return value;
        }
        throw new BadRequestException("Academic year must be 1, 2, 3, 4, or ALUMNI.");
    }
}
