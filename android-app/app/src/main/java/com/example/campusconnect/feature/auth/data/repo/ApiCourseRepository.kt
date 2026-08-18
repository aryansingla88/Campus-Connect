package com.example.campusconnect.feature.auth.data.repo

import com.example.campusconnect.core.network.RetrofitClient
import com.example.campusconnect.feature.auth.data.remote.AuthApi
import com.example.campusconnect.feature.auth.data.remote.response.CourseResponse

class ApiCourseRepository(
    private val api: AuthApi = RetrofitClient.authApi
) : CourseRepository {

    override suspend fun getCourses(): Result<List<CourseResponse>> {

        return try {

            val response = api.getCourses()

            if (
                response.isSuccessful &&
                response.body()?.data != null
            ) {

                Result.success(
                    response.body()!!.data!!
                )

            } else {

                Result.failure(
                    Exception(
                        response.body()?.message
                            ?: "Failed to fetch courses"
                    )
                )
            }

        } catch (e: Exception) {

            Result.failure(e)
        }
    }
}