package com.example.campusconnect.feature.auth.data.repo

import com.example.campusconnect.core.network.RetrofitClient
import com.example.campusconnect.feature.auth.data.remote.AuthApi
import com.example.campusconnect.feature.auth.data.remote.request.RegisterRequest
import com.example.campusconnect.feature.auth.data.remote.response.AuthResponse

class ApiAuthRepository(
    private val api: AuthApi = RetrofitClient.authApi
) : AuthRepository {

    override suspend fun register(
        request: RegisterRequest
    ): Result<AuthResponse> {

        return try {



            val response = api.register(request)


            if (
                response.isSuccessful &&
                response.body()?.data != null
            ) {

                Result.success(
                    response.body()!!.data!!
                )

            } else {

                val message =
                    response.body()?.message
                        ?: response.errorBody()?.string()
                        ?: "Registration failed. HTTP ${response.code()}"

                Result.failure(
                    Exception(message)
                )
            }

        } catch (e: Exception) {

            Result.failure(e)
        }
    }
}