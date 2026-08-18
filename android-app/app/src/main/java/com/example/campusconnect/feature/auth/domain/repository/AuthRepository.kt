package com.example.campusconnect.feature.auth.domain.repository

import com.example.campusconnect.feature.auth.data.remote.request.RegisterRequest
import com.example.campusconnect.feature.auth.data.remote.request.SendOtpRequest
import com.example.campusconnect.feature.auth.data.remote.request.VerifyOtpRequest
import com.example.campusconnect.feature.auth.data.remote.response.AuthResponse
import com.example.campusconnect.feature.auth.data.remote.response.CourseResponse

import com.example.campusconnect.feature.auth.data.remote.response.RegisterResponse
import com.example.campusconnect.feature.auth.data.remote.response.SendOtpResponse
import com.example.campusconnect.feature.auth.data.remote.response.VerifyOtpResponse

interface AuthRepository {

    suspend fun register(
        request: RegisterRequest
    ): Result<AuthResponse>
}