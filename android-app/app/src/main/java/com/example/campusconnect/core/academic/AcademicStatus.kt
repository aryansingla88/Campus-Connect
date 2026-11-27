package com.example.campusconnect.core.academic

import java.time.LocalDate

/** Shared academic session and status rule. The session rolls over on July 1. */
data class AcademicStatus(val academicYear: Int?, val isAlumni: Boolean) {
    init {
        require(if (isAlumni) academicYear == null else academicYear != null && academicYear >= 1)
    }

    val apiValue: String get() = if (isAlumni) "ALUMNI" else "YEAR_$academicYear"
    val displayLabel: String get() = if (isAlumni) "Alumni" else "${academicYear}${ordinal(academicYear!!)} Year"
    val visibilityAudienceValue: String get() = if (isAlumni) "ALUMNI" else academicYear.toString()

    companion object {
        fun fromApiValue(value: String?): AcademicStatus? {
            if (value == null) return null
            if (value.equals("ALUMNI", ignoreCase = true)) return AcademicStatus(null, true)
            val year = value.removePrefix("YEAR_").toIntOrNull()?.takeIf { it >= 1 } ?: return null
            return AcademicStatus(year, false)
        }

        fun forCalculatedYear(calculatedAcademicYear: Int, courseDurationYears: Int): AcademicStatus {
            require(courseDurationYears >= 1)
            require(calculatedAcademicYear >= 1)
            val year = calculatedAcademicYear
            return if (year > courseDurationYears) AcademicStatus(null, true) else AcademicStatus(year, false)
        }

        fun current(admissionYear: Int?, courseDurationYears: Int?, today: LocalDate): AcademicStatus? {
            if (admissionYear == null || courseDurationYears == null || courseDurationYears < 1) return null
            var year = today.year - admissionYear
            if (today.monthValue >= 7) year++
            if (year < 1) return null
            return forCalculatedYear(year, courseDurationYears)
        }

        private fun ordinal(year: Int): String = when {
            year % 100 in 11..13 -> "th"
            year % 10 == 1 -> "st"
            year % 10 == 2 -> "nd"
            year % 10 == 3 -> "rd"
            else -> "th"
        }
    }
}
