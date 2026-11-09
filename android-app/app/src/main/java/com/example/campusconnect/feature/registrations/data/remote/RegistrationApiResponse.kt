package com.example.campusconnect.feature.registrations.data.remote

import com.example.campusconnect.core.network.ApiResponse
import retrofit2.Response

/** Shared envelope handling for registration calls that require a non-empty data payload. */
internal fun <T : Any> Response<ApiResponse<T>>.requireRegistrationData(
    fallback: String,
    emptyFallback: String? = null,
): T {
    val envelope = body()
    if (!isSuccessful || envelope?.success != true) {
        error(envelope?.message?.takeIf(String::isNotBlank) ?: "$fallback (HTTP ${code()}).")
    }
    return envelope?.data ?: error(
        envelope?.message?.takeIf(String::isNotBlank)
            ?: emptyFallback
            ?: "$fallback (HTTP ${code()})."
    )
}

/** Success handling for endpoints whose successful envelope intentionally has no data payload. */
internal fun <T> Response<ApiResponse<T>>.requireRegistrationSuccess(fallback: String) {
    val envelope = body()
    if (!isSuccessful || envelope?.success != true) {
        error(envelope?.message?.takeIf(String::isNotBlank) ?: "$fallback (HTTP ${code()}).")
    }
}
