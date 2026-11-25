package com.campus.Campus_Connect.features.post.service.impl;

import com.campus.Campus_Connect.common.exception.BadRequestException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PostVisibilityNormalizerTest {

    @Test
    void normalizesPublicAndNullConfigurationsToNullValues() {
        assertEquals(new PostVisibilityNormalizer.NormalizedVisibility(null, null),
                PostVisibilityNormalizer.normalize(false, "PUBLIC", null));
        assertEquals(new PostVisibilityNormalizer.NormalizedVisibility(null, null),
                PostVisibilityNormalizer.normalize(false, null, null));
        assertEquals(new PostVisibilityNormalizer.NormalizedVisibility(null, null),
                PostVisibilityNormalizer.normalize(true, "CLUB", "9"));
    }

    @Test
    void preservesSupportedNonPublicScopeValues() {
        assertEquals(new PostVisibilityNormalizer.NormalizedVisibility("CONNECTIONS", null),
                PostVisibilityNormalizer.normalize(false, "connections", null));
        assertEquals(new PostVisibilityNormalizer.NormalizedVisibility("CLUB", "12"),
                PostVisibilityNormalizer.normalize(false, "CLUB", " 12 "));
        assertEquals(new PostVisibilityNormalizer.NormalizedVisibility("HOSTEL", "3"),
                PostVisibilityNormalizer.normalize(false, "HOSTEL", "3"));
        assertEquals(new PostVisibilityNormalizer.NormalizedVisibility("COURSE", "41"),
                PostVisibilityNormalizer.normalize(false, "COURSE", "41"));
        assertEquals(new PostVisibilityNormalizer.NormalizedVisibility("ACADEMIC_YEAR", "4"),
                PostVisibilityNormalizer.normalize(false, "ACADEMIC_YEAR", "4"));
        assertEquals(new PostVisibilityNormalizer.NormalizedVisibility("ACADEMIC_YEAR", "ALUMNI"),
                PostVisibilityNormalizer.normalize(false, "ACADEMIC_YEAR", "alumni"));
    }

    @Test
    void rejectsMissingOrIncompatibleVisibilityValues() {
        assertThrows(BadRequestException.class,
                () -> PostVisibilityNormalizer.normalize(false, null, "1"));
        assertThrows(BadRequestException.class,
                () -> PostVisibilityNormalizer.normalize(false, "CONNECTIONS", "1"));
        assertThrows(BadRequestException.class,
                () -> PostVisibilityNormalizer.normalize(false, "CLUB", "0"));
        assertThrows(BadRequestException.class,
                () -> PostVisibilityNormalizer.normalize(false, "HOSTEL", "abc"));
        assertThrows(BadRequestException.class,
                () -> PostVisibilityNormalizer.normalize(false, "ACADEMIC_YEAR", "5"));
        assertThrows(BadRequestException.class,
                () -> PostVisibilityNormalizer.normalize(false, "BATCH", null));
        assertThrows(BadRequestException.class,
                () -> PostVisibilityNormalizer.normalize(false, "UNKNOWN", null));
    }
}
