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
            401 -> "Please log in to continue."
            403 -> "You don't have permission to perform this action."
            404 -> "This post or related content is no longer available. Its visibility may have changed."
            413 -> "The selected image is too large."
            else -> "Request failed (${response.code()})."
        }
    }

    fun exception(response: Response<*>, fallback: String): PostApiException {
        val bodyMessage = (response.body() as? ApiResponse<*>)?.message
            ?.takeIf(String::isNotBlank)
        val parsedMessage = message(response)
        val errorMessage = bodyMessage
            ?: parsedMessage.takeUnless { it.startsWith("Request failed (") }
            ?: fallback
        return PostApiException(response.code(), errorMessage)
    }
}
