package com.example.campusconnect.feature.registrations.data.repo

import com.example.campusconnect.core.network.RetrofitClient
import com.example.campusconnect.feature.registrations.data.mapper.RegistrationFieldMapper
import com.example.campusconnect.feature.registrations.data.remote.RegistrationApi
import com.example.campusconnect.feature.registrations.model.FormField

class RegistrationFieldRepository(
    private val api: RegistrationApi = RetrofitClient.registrationApi,
) {
    suspend fun load(eventId: Int): Result<List<FormField>> = try {
        val response = api.getRegistrationFields(eventId)
        val body = response.body()
        if (!response.isSuccessful || body?.success != true) {
            Result.failure(IllegalStateException(
                body?.message?.takeIf(String::isNotBlank)
                    ?: "Could not load registration questions (HTTP ${response.code()})."
            ))
        } else {
            val data = body.data
            if (data == null) Result.failure(IllegalStateException("The server returned no registration questions."))
            else Result.success(data.sortedBy { it.fieldOrder }.map(RegistrationFieldMapper::toModel))
        }
    } catch (error: Exception) {
        Result.failure(error)
    }

    /** The PUT endpoint updates fields with IDs, creates fields without IDs, and soft-deletes omitted IDs. */
    suspend fun save(eventId: Int, fields: List<FormField>): Result<List<FormField>> = try {
        // The editor's current list order is authoritative for both existing and new fields.
        val ordered = fields.mapIndexed { order, field -> RegistrationFieldMapper.toRequest(field, order) }
        val response = api.updateRegistrationFields(eventId, ordered)
        val body = response.body()
        if (!response.isSuccessful || body?.success != true || body.data == null) {
            Result.failure(IllegalStateException(
                body?.message?.takeIf(String::isNotBlank)
                    ?: "Could not save registration questions (HTTP ${response.code()})."
            ))
        } else {
            Result.success(body.data.sortedBy { it.fieldOrder }.map(RegistrationFieldMapper::toModel))
        }
    } catch (error: Exception) {
        Result.failure(error)
    }

}
