package com.example.campusconnect.core.utils

import com.example.campusconnect.feature.metadata.courses.Course
import java.time.LocalDate

object AcademicUtils {


    fun getCourseName(course: Course): String {
        return listOfNotNull(
            course.degree,
            course.courseCode
        ).joinToString(" ")
    }


    fun buildSubtitle(
        course: Course,
        admissionYear: Int
    ): String {

        val academicStatus = getAcademicStatus(
            admissionYear = admissionYear,
            durationYears = course.durationYears
        )

        return "${getCourseName(course)} • $academicStatus"
    }


    fun getAcademicStatus(
        admissionYear: Int,
        durationYears: Int
    ): String {

        val studyYear =
            getCurrentAcademicYear() - admissionYear

        if (studyYear > durationYears) {
            return "Alumni"
        }

        val validStudyYear = maxOf(1, studyYear)

        return "$validStudyYear${getOrdinal(validStudyYear)} Year"
    }


    fun getBatch(
        admissionYear: Int,
        durationYears: Int
    ): String {

        val graduationYear =
            admissionYear + durationYears

        return "$admissionYear–$graduationYear"
    }


    private fun getCurrentAcademicYear(): Int {

        val today = LocalDate.now()

        var academicYear = today.year

        if (today.monthValue >= 7) {
            academicYear++
        }

        return academicYear
    }


    private fun getOrdinal(number: Int): String {
        return when {
            number % 100 in 11..13 -> "th"
            number % 10 == 1 -> "st"
            number % 10 == 2 -> "nd"
            number % 10 == 3 -> "rd"
            else -> "th"
        }
    }
}