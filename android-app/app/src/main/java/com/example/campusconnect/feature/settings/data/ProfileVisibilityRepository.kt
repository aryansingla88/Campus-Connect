package com.example.campusconnect.feature.settings.data

import com.example.campusconnect.core.network.RetrofitClient
import com.example.campusconnect.feature.profile.data.remote.ProfileApi
import com.example.campusconnect.feature.profile.data.remote.response.ProfileVisibilityPreferencesResponse

class ProfileVisibilityRepository(
    private val api: ProfileApi = RetrofitClient.profileApi
) {
    suspend fun get(): Result<ProfileVisibilityPreferencesResponse> = runCatching {
        val response = api.getUserPreferences()
        require(response.isSuccessful) { "Couldn't load profile visibility settings" }
        response.body()?.data ?: error("Profile visibility settings are empty")
    }

    suspend fun update(
        request: UpdateProfileVisibilityPreferencesRequest
    ): Result<ProfileVisibilityPreferencesResponse> = runCatching {
        val response = api.updateUserPreferences(request)
        require(response.isSuccessful) { "Couldn't save profile visibility settings" }
        response.body()?.data ?: error("Updated profile visibility settings are empty")
    }
}
