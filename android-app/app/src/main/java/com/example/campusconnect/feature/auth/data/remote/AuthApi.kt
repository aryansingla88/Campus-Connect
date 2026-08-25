package com.example.campusconnect.feature.auth.data.remote

import com.example.campusconnect.core.network.ApiResponse
import com.example.campusconnect.feature.auth.data.remote.request.LoginRequest
import com.example.campusconnect.feature.auth.data.remote.request.RegisterRequest
import com.example.campusconnect.feature.auth.data.remote.response.AuthResponse
import com.example.campusconnect.feature.auth.data.remote.response.CourseResponse

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface AuthApi {

    @POST("auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<ApiResponse<AuthResponse>>

    @POST("auth/register")
    suspend fun register(
        @Body request: RegisterRequest
    ): Response<ApiResponse<AuthResponse>>
    @GET("metadata/courses")
    suspend fun getCourses(): Response<ApiResponse<List<CourseResponse>>>
}