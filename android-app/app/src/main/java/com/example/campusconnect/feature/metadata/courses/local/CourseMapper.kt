package com.example.campusconnect.feature.metadata.courses.local

import com.example.campusconnect.feature.metadata.courses.Course
import com.example.campusconnect.feature.metadata.courses.remote.CourseResponse

fun CourseEntity.toCourse(): Course {
    return Course(
        courseId = courseId,
        degree = degree,
        programName = programName,
        courseCode = courseCode,
        degreeLevel = degreeLevel,
        durationYears = durationYears
    )

}

fun Course.toEntity(): CourseEntity {
    return CourseEntity(
        courseId = courseId,
        degree = degree,
        programName = programName,
        courseCode = courseCode,
        degreeLevel = degreeLevel,
        durationYears = durationYears
    )
}

fun CourseResponse.toCourse(): Course {
    return Course(
        courseId = courseId,
        degree = degree,
        programName = programName,
        courseCode = courseCode,
        degreeLevel = degreeLevel,
        durationYears = durationYears
    )
}

