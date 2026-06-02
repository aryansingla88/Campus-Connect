package com.example.campusconnect.feature.profile.data.remote.response

import com.google.gson.annotations.SerializedName

data class CourseResponse(
    @SerializedName("course_id")        val courseId      : Int,
    @SerializedName("course_name")      val courseName    : String,
    @SerializedName("duration_years")   val durationYears : Int,
    @SerializedName("has_branch")       val hasBranch     : Boolean,
)