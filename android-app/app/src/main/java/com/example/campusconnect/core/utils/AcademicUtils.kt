package com.example.campusconnect.core.utils

import com.example.campusconnect.feature.metadata.courses.Course
import com.example.campusconnect.core.academic.AcademicStatus
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
        admissionYear: Int,
        academicStatus: String? = null
    ): String {

        val statusLabel = (AcademicStatus.fromApiValue(academicStatus)?.displayLabel
            ?: AcademicStatus.current(admissionYear, course.durationYears, LocalDate.now())?.displayLabel)
            .orEmpty()

        return "${getCourseName(course)} • $statusLabel"
    }


    fun getBatch(
        admissionYear: Int,
        durationYears: Int
    ): String {

        val graduationYear =
            admissionYear + durationYears

        return "$admissionYear–$graduationYear"
    }


}
