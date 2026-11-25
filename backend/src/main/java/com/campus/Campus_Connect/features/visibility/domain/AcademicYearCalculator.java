package com.campus.Campus_Connect.features.visibility.domain;

import java.time.Clock;
import java.time.LocalDate;

/** Uses Campus Connect's existing July academic-session rollover for derived viewer context. */
public final class AcademicYearCalculator {

    private AcademicYearCalculator() {
    }

    public static Integer currentAcademicYear(Integer admissionYear, Clock clock) {
        if (admissionYear == null) {
            return null;
        }
        return academicYear(admissionYear, LocalDate.now(clock));
    }

    static int academicYear(Integer admissionYear, LocalDate today) {
        int academicYear = today.getYear() - admissionYear;
        if (today.getMonthValue() >= 7) {
            academicYear++;
        }
        return academicYear;
    }
}
