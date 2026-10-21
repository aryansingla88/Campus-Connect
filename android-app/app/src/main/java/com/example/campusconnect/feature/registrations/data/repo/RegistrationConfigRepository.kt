package com.example.campusconnect.feature.registrations.data.repo

import com.example.campusconnect.core.network.RetrofitClient
import com.example.campusconnect.feature.registrations.data.mapper.RegistrationConfigMapper
import com.example.campusconnect.feature.registrations.data.remote.RegistrationApi
import com.example.campusconnect.feature.registrations.data.remote.QuickFieldsConfigRequest
import com.example.campusconnect.feature.registrations.model.RegistrationSettings

class RegistrationConfigRepository {
    companion object {
        /** Test-screen-only event; production registration routes pass their event ID. */
        const val TEMP_EVENT_ID = 3

        fun quickFieldIds(mask: Long): Set<String> = quickFieldBits
            .filterValues { bit -> (mask and bit) != 0L }
            .keys

        fun quickFieldMask(ids: Set<String>): Long = ids
            .mapNotNull { quickFieldBits[it] }
            .fold(0L) { mask, bit -> mask or bit }

        private val quickFieldBits = linkedMapOf(
            "full_name" to (1L shl 0), "email" to (1L shl 1), "phone" to (1L shl 2),
            "course" to (1L shl 3), "year_batch" to (1L shl 4), "roll_number" to (1L shl 5),
            "hostel" to (1L shl 6), "hometown" to (1L shl 7), "gender" to (1L shl 8),
            "date_of_birth" to (1L shl 9), "github" to (1L shl 10), "linkedin" to (1L shl 11),
        )
    }

    private val api: RegistrationApi = RetrofitClient.registrationApi

    suspend fun load(eventId: Int): LoadResult {
        val response = api.getRegistrationConfig(eventId)
        val body = response.body()
        if (!response.isSuccessful) {
            return LoadResult.Failure("Could not load registration settings (HTTP ${response.code()}).")
        }
        if (body?.success == true) {
            val config = body.data
                ?: return LoadResult.Failure("The server returned an empty registration configuration.")
            return LoadResult.Existing(RegistrationConfigMapper.toModel(config))
        }
        val message = body?.message.orEmpty()
        if (message.contains("configuration not found", ignoreCase = true)) {
            return LoadResult.Missing
        }
        return LoadResult.Failure(message.ifBlank { "Could not load registration settings." })
    }

    suspend fun save(eventId: Int, settings: RegistrationSettings, exists: Boolean): Result<RegistrationSettings> {
        if (settings.minMembers < 1 || settings.minMembers > RegistrationSettings.MAX_TEAM_SIZE ||
            settings.maxMembers < settings.minMembers || settings.maxMembers > RegistrationSettings.MAX_TEAM_SIZE ||
            (settings.minMembers > 1 && settings.maxMembers < 2)
        ) return Result.failure(IllegalArgumentException("Choose valid minimum and maximum team sizes."))

        val request = RegistrationConfigMapper.toRequest(settings)
        return try {
            val response = if (exists) {
                api.updateRegistrationConfig(eventId, request)
            } else {
                api.createRegistrationConfig(eventId, request)
            }
            val body = response.body()
            if (response.isSuccessful && body?.success == true && body.data != null) {
                Result.success(RegistrationConfigMapper.toModel(body.data))
            } else {
                Result.failure(IllegalStateException(
                    body?.message?.takeIf(String::isNotBlank)
                        ?: "Could not save registration settings (HTTP ${response.code()})."
                ))
            }
        } catch (error: Exception) {
            Result.failure(error)
        }
    }

    suspend fun saveQuickFields(eventId: Int, mask: Long): Result<RegistrationSettings> = try {
        val response = api.updateQuickFieldsConfig(eventId, QuickFieldsConfigRequest(mask))
        val body = response.body()
        if (response.isSuccessful && body?.success == true && body.data != null) {
            Result.success(RegistrationConfigMapper.toModel(body.data))
        } else {
            Result.failure(IllegalStateException(
                body?.message?.takeIf(String::isNotBlank)
                    ?: "Could not save Quick Fields (HTTP ${response.code()})."
            ))
        }
    } catch (error: Exception) {
        Result.failure(error)
    }

    sealed interface LoadResult {
        data class Existing(val settings: RegistrationSettings) : LoadResult
        data object Missing : LoadResult
        data class Failure(val message: String) : LoadResult
    }
}
