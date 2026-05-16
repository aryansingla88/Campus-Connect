package com.example.campusconnect.feature.auth

sealed class VerifyOtpResult {

    object Success : VerifyOtpResult()

    data class Failure(

        val attemptsLeft: Int

    ) : VerifyOtpResult()

    object Expired : VerifyOtpResult()
}