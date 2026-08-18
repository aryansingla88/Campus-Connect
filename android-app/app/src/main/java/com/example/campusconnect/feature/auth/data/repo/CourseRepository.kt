package com.example.campusconnect.feature.auth.data.repo

import com.example.campusconnect.feature.auth.data.remote.response.CourseResponse

interface CourseRepository {

    suspend fun getCourses(): Result<List<CourseResponse>>
}