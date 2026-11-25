package com.campus.Campus_Connect.features.visibility.validation;

import com.campus.Campus_Connect.common.exception.BadRequestException;
import com.campus.Campus_Connect.features.visibility.domain.VisibilityPolicy;
import com.campus.Campus_Connect.features.visibility.domain.VisibilityScope;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class VisibilityPolicyValidatorTest {

    @Test
    void acceptsSupportedPolicyShapesAndCanonicalizesValues() {
        assertEquals(new VisibilityPolicy(VisibilityScope.CONNECTIONS, null),
                VisibilityPolicyValidator.normalize(new VisibilityPolicy(VisibilityScope.CONNECTIONS, null)));
        assertEquals(new VisibilityPolicy(VisibilityScope.CLUB, "12"),
                VisibilityPolicyValidator.normalize(new VisibilityPolicy(VisibilityScope.CLUB, " 012 ")));
        assertEquals(new VisibilityPolicy(VisibilityScope.HOSTEL, "3"),
                VisibilityPolicyValidator.normalize(new VisibilityPolicy(VisibilityScope.HOSTEL, "3")));
        assertEquals(new VisibilityPolicy(VisibilityScope.COURSE, "41"),
                VisibilityPolicyValidator.normalize(new VisibilityPolicy(VisibilityScope.COURSE, "41")));

        for (String year : new String[]{"1", "2", "3", "4"}) {
            assertEquals(new VisibilityPolicy(VisibilityScope.ACADEMIC_YEAR, year),
                    VisibilityPolicyValidator.normalize(new VisibilityPolicy(VisibilityScope.ACADEMIC_YEAR, year)));
        }
        assertEquals(new VisibilityPolicy(VisibilityScope.ACADEMIC_YEAR, "ALUMNI"),
                VisibilityPolicyValidator.normalize(new VisibilityPolicy(VisibilityScope.ACADEMIC_YEAR, " alumni ")));
    }

    @Test
    void rejectsMissingAndInvalidScopeValues() {
        assertThrows(BadRequestException.class, () -> VisibilityPolicyValidator.validate(null));
        assertThrows(BadRequestException.class,
                () -> VisibilityPolicyValidator.validate(new VisibilityPolicy(null, null)));
        assertThrows(BadRequestException.class,
                () -> VisibilityPolicyValidator.validate(new VisibilityPolicy(VisibilityScope.CONNECTIONS, "1")));

        for (VisibilityScope scope : new VisibilityScope[]{
                VisibilityScope.CLUB, VisibilityScope.HOSTEL, VisibilityScope.COURSE
        }) {
            assertThrows(BadRequestException.class,
                    () -> VisibilityPolicyValidator.validate(new VisibilityPolicy(scope, null)));
            assertThrows(BadRequestException.class,
                    () -> VisibilityPolicyValidator.validate(new VisibilityPolicy(scope, "abc")));
            assertThrows(BadRequestException.class,
                    () -> VisibilityPolicyValidator.validate(new VisibilityPolicy(scope, "0")));
            assertThrows(BadRequestException.class,
                    () -> VisibilityPolicyValidator.validate(new VisibilityPolicy(scope, "-2")));
        }

        for (String year : new String[]{null, "0", "5", "UNDERGRAD"}) {
            assertThrows(BadRequestException.class,
                    () -> VisibilityPolicyValidator.validate(
                            new VisibilityPolicy(VisibilityScope.ACADEMIC_YEAR, year)));
        }
    }
}
