package com.example.campusconnect.feature.metadata.courses.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "courses")
data class CourseEntity(

    @PrimaryKey
    val courseId: Int,

    val degree: String,
    val programName: String,
    val courseCode: String?,
    val degreeLevel: String,
    val durationYears: Int
)

