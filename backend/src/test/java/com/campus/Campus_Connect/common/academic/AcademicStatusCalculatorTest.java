package com.campus.Campus_Connect.common.academic;

import org.junit.jupiter.api.Test;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class AcademicStatusCalculatorTest {
    private static final ZoneId CAMPUS_ZONE = ZoneId.of("Asia/Kolkata");

    @Test void classifiesFourYearCourse() {
        assertEquals("YEAR_1", AcademicStatusCalculator.statusForYear(1, 4).apiValue());
        assertEquals("YEAR_4", AcademicStatusCalculator.statusForYear(4, 4).apiValue());
        assertEquals("ALUMNI", AcademicStatusCalculator.statusForYear(5, 4).apiValue());
    }

    @Test void classifiesThreeAndTwoYearCourses() {
        assertEquals("YEAR_3", AcademicStatusCalculator.statusForYear(3, 3).apiValue());
        assertEquals("ALUMNI", AcademicStatusCalculator.statusForYear(4, 3).apiValue());
        assertEquals("YEAR_2", AcademicStatusCalculator.statusForYear(2, 2).apiValue());
        assertEquals("ALUMNI", AcademicStatusCalculator.statusForYear(3, 2).apiValue());
    }

    @Test void supportsProgramsLongerThanFourYears() {
        assertEquals("YEAR_4", AcademicStatusCalculator.statusForYear(4, 5).apiValue());
        assertEquals("YEAR_5", AcademicStatusCalculator.statusForYear(5, 5).apiValue());
        assertEquals("ALUMNI", AcademicStatusCalculator.statusForYear(6, 5).apiValue());
    }

    @Test void usesJulyRolloverAtCampusDateBoundary() {
        Clock before = Clock.fixed(Instant.parse("2026-06-30T12:00:00Z"), CAMPUS_ZONE);
        Clock after = Clock.fixed(Instant.parse("2026-07-01T12:00:00Z"), CAMPUS_ZONE);
        assertEquals("YEAR_3", AcademicStatusCalculator.currentStatus(2023, 4, before).apiValue());
        assertEquals("YEAR_4", AcademicStatusCalculator.currentStatus(2023, 4, after).apiValue());
        assertNull(AcademicStatusCalculator.currentStatus(null, 4, before));
        assertNull(AcademicStatusCalculator.currentStatus(2023, null, before));
    }

    @Test void academicYearImmediatelyBeforeAndAfterJulyBoundary() {
        assertEquals(3, AcademicStatusCalculator.academicYear(2023, LocalDate.of(2026, 6, 30)));
        assertEquals(4, AcademicStatusCalculator.academicYear(2023, LocalDate.of(2026, 7, 1)));
    }
}
