package com.campus.Campus_Connect.common.academic;

import java.time.Clock;
import java.time.LocalDate;

/**
 * Canonical academic status rule: after the July session rollover, derive the academic
 * year from admission year; status is Alumni only when that year exceeds course duration.
 */
public final class AcademicStatusCalculator {
    private AcademicStatusCalculator() { }

    public static AcademicStatus currentStatus(Integer admissionYear, Integer courseDurationYears, Clock clock) {
        if (admissionYear == null || courseDurationYears == null || courseDurationYears < 1) return null;
        int academicYear = academicYear(admissionYear, LocalDate.now(clock));
        return academicYear < 1 ? null : statusForYear(academicYear, courseDurationYears);
    }

    public static AcademicStatus statusForYear(int calculatedAcademicYear, int courseDurationYears) {
        if (courseDurationYears < 1) throw new IllegalArgumentException("Course duration must be positive.");
        if (calculatedAcademicYear < 1) throw new IllegalArgumentException("Calculated academic year must be positive.");
        int year = calculatedAcademicYear;
        return year > courseDurationYears ? AcademicStatus.alumniStatus() : AcademicStatus.year(year);
    }

    public static int academicYear(Integer admissionYear, LocalDate today) {
        int year = today.getYear() - admissionYear;
        if (today.getMonthValue() >= 7) year++;
        return year;
    }
}
