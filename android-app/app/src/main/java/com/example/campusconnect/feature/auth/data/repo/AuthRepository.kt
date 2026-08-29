package com.example.campusconnect.feature.auth.data.repo

import com.example.campusconnect.feature.auth.data.remote.request.RegisterRequest
import com.example.campusconnect.feature.auth.data.remote.response.AuthResponse

interface AuthRepository {

    suspend fun register(
        request: RegisterRequest
    ): Result<AuthResponse>
}