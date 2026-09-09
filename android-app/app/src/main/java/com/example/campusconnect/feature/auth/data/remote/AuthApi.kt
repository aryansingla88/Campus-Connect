package com.example.campusconnect.feature.auth.data.remote

import com.example.campusconnect.core.network.ApiResponse
import com.example.campusconnect.feature.auth.data.remote.request.LoginRequest
import com.example.campusconnect.feature.auth.data.remote.request.RegisterRequest
import com.example.campusconnect.feature.auth.data.remote.response.AuthResponse
import com.example.campusconnect.feature.auth.data.remote.request.RequestPasswordResetRequest
import com.example.campusconnect.feature.auth.data.remote.request.VerifyPasswordResetOtpRequest
import com.example.campusconnect.feature.auth.data.remote.request.ResetPasswordRequest
import com.example.campusconnect.feature.auth.data.remote.response.VerifyPasswordResetOtpResponse

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

    @POST("auth/password-reset/request")
    suspend fun requestPasswordReset(
        @Body request: RequestPasswordResetRequest
    ): Response<ApiResponse<Void>>

    @POST("auth/password-reset/verify")
    suspend fun verifyPasswordResetOtp(
        @Body request: VerifyPasswordResetOtpRequest
    ): Response<ApiResponse<VerifyPasswordResetOtpResponse>>

    @POST("auth/password-reset/reset")
    suspend fun resetPassword(
        @Body request: ResetPasswordRequest
    ): Response<ApiResponse<Void>>
}