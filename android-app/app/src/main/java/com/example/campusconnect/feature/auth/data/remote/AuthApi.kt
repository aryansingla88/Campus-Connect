package com.example.campusconnect.feature.auth.data.remote

import com.example.campusconnect.core.network.ApiResponse
import com.example.campusconnect.feature.auth.data.remote.request.RegisterRequest
import com.example.campusconnect.feature.auth.data.remote.request.SendOtpRequest
import com.example.campusconnect.feature.auth.data.remote.request.VerifyOtpRequest
import com.example.campusconnect.feature.auth.data.remote.response.RegisterResponse
import com.example.campusconnect.feature.auth.data.remote.response.SendOtpResponse
import com.example.campusconnect.feature.auth.data.remote.response.VerifyOtpResponse

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {

    @POST("auth/send-otp")
    suspend fun sendOtp(

        @Body request: SendOtpRequest

    ): Response<ApiResponse<SendOtpResponse>>



    @POST("auth/verify-otp")
    suspend fun verifyOtp(

        @Body request: VerifyOtpRequest

    ): Response<ApiResponse<VerifyOtpResponse>>



    @POST("auth/register")
    suspend fun register(

        @Body request: RegisterRequest

    ): Response<ApiResponse<RegisterResponse>>
}