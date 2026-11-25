package com.campus.Campus_Connect.features.post.service.impl;

import com.campus.Campus_Connect.common.exception.BadRequestException;

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

        return switch (type) {
            case "PUBLIC" -> {
                requireNoValue(type, value);
                yield new NormalizedVisibility(null, null);
            }
            case "CONNECTIONS" -> {
                requireNoValue(type, value);
                yield new NormalizedVisibility(type, null);
            }
            case "CLUB", "HOSTEL", "COURSE" ->
                    new NormalizedVisibility(type, normalizePositiveId(type, value));
            case "ACADEMIC_YEAR" ->
                    new NormalizedVisibility(type, normalizeAcademicYear(value));
            case "BATCH" -> throw new BadRequestException("BATCH visibility is not supported for post creation.");
            default -> throw new BadRequestException("Unsupported post visibility type.");
        };
    }

    private static void requireNoValue(String type, String value) {
        if (value != null) {
            throw new BadRequestException(type + " visibility does not accept a value.");
        }
    }

    private static String normalizePositiveId(String type, String value) {
        if (value == null) {
            throw new BadRequestException(type + " visibility requires an ID value.");
        }
        try {
            int id = Integer.parseInt(value);
            if (id <= 0) {
                throw new BadRequestException(type + " visibility ID must be positive.");
            }
            return Integer.toString(id);
        } catch (NumberFormatException exception) {
            throw new BadRequestException(type + " visibility ID must be a positive integer.");
        }
    }

    private static String normalizeAcademicYear(String value) {
        if (value == null) {
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

    record NormalizedVisibility(String type, String value) {
    }
}
