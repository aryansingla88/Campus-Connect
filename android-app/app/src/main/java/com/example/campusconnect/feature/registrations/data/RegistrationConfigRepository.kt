package com.example.campusconnect.feature.registrations.data

import com.example.campusconnect.core.network.RetrofitClient
import com.example.campusconnect.feature.registrations.data.remote.RegistrationConfigRequest
import com.example.campusconnect.feature.registrations.data.remote.RegistrationConfigResponse
import com.example.campusconnect.feature.registrations.data.remote.QuickFieldsConfigRequest
import com.example.campusconnect.feature.registrations.model.RegistrationSettings
import java.time.Instant
import java.time.format.DateTimeParseException

class RegistrationConfigRepository {
    companion object {
        /** Temporary until event integration supplies the real event ID. */
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

    private val api = RetrofitClient.eventsApi

    suspend fun load(): LoadResult {
        val response = api.getRegistrationConfig(TEMP_EVENT_ID)
        val body = response.body()
        if (!response.isSuccessful) {
            return LoadResult.Failure("Could not load registration settings (HTTP ${response.code()}).")
        }
        if (body?.success == true) {
            val config = body.data
                ?: return LoadResult.Failure("The server returned an empty registration configuration.")
            return LoadResult.Existing(config.toSettings())
        }
        val message = body?.message.orEmpty()
        if (message.contains("configuration not found", ignoreCase = true)) {
            return LoadResult.Missing
        }
        return LoadResult.Failure(message.ifBlank { "Could not load registration settings." })
    }

    suspend fun save(settings: RegistrationSettings, exists: Boolean): Result<Unit> {
        if (settings.minMembers < 1 || settings.minMembers > RegistrationSettings.MAX_TEAM_SIZE ||
            settings.maxMembers < settings.minMembers || settings.maxMembers > RegistrationSettings.MAX_TEAM_SIZE ||
            (settings.minMembers > 1 && settings.maxMembers < 2)
        ) return Result.failure(IllegalArgumentException("Choose valid minimum and maximum team sizes."))

        val request = RegistrationConfigRequest(
            minTeamMembers = settings.minMembers,
            maxTeamMembers = settings.maxMembers,
            quickFieldMask = settings.quickFieldMask,
            registrationCreatedAt = settings.registrationCreatedAt,
            registrationEndAt = settings.deadline?.let { Instant.ofEpochMilli(it).toString() },
        )
        return try {
            val response = if (exists) {
                api.updateRegistrationConfig(TEMP_EVENT_ID, request)
            } else {
                api.createRegistrationConfig(TEMP_EVENT_ID, request)
            }
            val body = response.body()
            if (response.isSuccessful && body?.success == true && body.data != null) {
                Result.success(Unit)
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

    suspend fun saveQuickFields(mask: Long): Result<RegistrationSettings> = try {
        val response = api.updateQuickFieldsConfig(TEMP_EVENT_ID, QuickFieldsConfigRequest(mask))
        val body = response.body()
        if (response.isSuccessful && body?.success == true && body.data != null) {
            Result.success(body.data.toSettings())
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

    private fun RegistrationConfigResponse.toSettings(): RegistrationSettings {
        val deadline = registrationEndAt?.let {
            try { Instant.parse(it).toEpochMilli() } catch (_: DateTimeParseException) { null }
        }
        return RegistrationSettings(
            minMembers = minTeamMembers,
            maxMembers = maxTeamMembers,
            quickFieldMask = quickFieldMask,
            registrationCreatedAt = registrationCreatedAt,
            deadline = deadline,
        )
    }
}
