package com.example.campusconnect.feature.posts.data.remote

import com.example.campusconnect.core.network.ApiResponse
import com.google.gson.Gson
import retrofit2.Response

object PostApiErrorParser {

    private val gson = Gson()

    fun message(response: Response<*>): String {
        response.body()?.let { body ->
            val message = (body as? ApiResponse<*>)?.message
            if (!message.isNullOrBlank()) return message
        }

        val raw = response.errorBody()?.string()
        if (!raw.isNullOrBlank()) {
            runCatching {
                gson.fromJson(raw, ApiResponse::class.java)?.message
            }.getOrNull()?.takeIf { it.isNotBlank() }?.let { return it }
        }

        return when (response.code()) {
            400 -> "Invalid post data."
            401 -> "Please log in to create a post."
            403 -> "You are not allowed to create this post."
            413 -> "The selected image is too large."
            else -> "Request failed (${response.code()})."
        }
    }
}
