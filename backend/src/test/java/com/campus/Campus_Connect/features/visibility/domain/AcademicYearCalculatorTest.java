package com.campus.Campus_Connect.features.visibility.domain;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class AcademicYearCalculatorTest {

    @Test
    void followsTheExistingJulyRolloverAndCanBeTestedWithAFixedClock() {
        ZoneId campusZone = ZoneId.of("Asia/Kolkata");
        Clock beforeJuly = Clock.fixed(Instant.parse("2026-06-30T12:00:00Z"), campusZone);
        Clock fromJuly = Clock.fixed(Instant.parse("2026-07-01T12:00:00Z"), campusZone);

        assertEquals(3, AcademicYearCalculator.currentAcademicYear(2023, beforeJuly));
        assertEquals(4, AcademicYearCalculator.currentAcademicYear(2023, fromJuly));
        assertNull(AcademicYearCalculator.currentAcademicYear(null, beforeJuly));
    }
}
