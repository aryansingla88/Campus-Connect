package com.example.campusconnect.feature.registrations.data.repo

import com.example.campusconnect.core.network.RetrofitClient
import com.example.campusconnect.feature.events.data.remote.EventsApi
import com.example.campusconnect.feature.registrations.data.mapper.RegistrationEventMapper
import com.example.campusconnect.feature.registrations.model.RegistrationEventHeader

/** Reads event details needed by host registration screens; event discovery stays with EventsApi. */
class RegistrationEventRepository(
    private val api: EventsApi = RetrofitClient.eventsApi,
) {
    suspend fun getHeader(eventId: Int): Result<RegistrationEventHeader> = try {
        val response = api.getEvent(eventId)
        val body = response.body()
        val event = body?.data
        if (response.isSuccessful && body?.success == true && event != null) {
            Result.success(RegistrationEventMapper.toHeader(event))
        } else {
            Result.failure(IllegalStateException(
                body?.message?.takeIf(String::isNotBlank)
                    ?: "Could not load event details (HTTP ${response.code()}).",
            ))
        }
    } catch (error: Exception) {
        Result.failure(error)
    }
}
