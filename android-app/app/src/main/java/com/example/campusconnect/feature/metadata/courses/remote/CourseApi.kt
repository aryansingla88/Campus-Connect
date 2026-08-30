package com.example.campusconnect.feature.metadata.courses.remote

import com.example.campusconnect.core.network.ApiResponse
import retrofit2.Response
import retrofit2.http.GET

interface CourseApi {

    @GET("metadata/courses/details")
    suspend fun getCourses():
            Response<ApiResponse<List<CourseResponse>>>
}