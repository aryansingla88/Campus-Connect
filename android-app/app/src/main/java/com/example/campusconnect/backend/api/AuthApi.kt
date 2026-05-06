package com.example.campusconnect.backend.api

import com.example.campusconnect.backend.request.LoginRequest
import com.example.campusconnect.backend.response.LoginResponse

object AuthApi {

    fun loginUser(request: LoginRequest): LoginResponse {

        // Fake backend logic

        return if (
            request.username == "test" &&
            request.password == "1234"
        ) {

            LoginResponse(
                success = true,
                message = "Login successful",
                user_id = 1
            )

        } else {

            LoginResponse(
                success = false,
                message = "Invalid credentials",
                user_id = -1
            )
        }
    }
}