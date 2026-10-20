package com.example.campusconnect.feature.registrations.data.repo

import com.example.campusconnect.core.network.RetrofitClient
import com.example.campusconnect.feature.registrations.data.mapper.RegistrationResponseMapper
import com.example.campusconnect.feature.registrations.data.remote.RegistrationApi
import com.example.campusconnect.feature.registrations.model.*
import java.util.concurrent.ConcurrentHashMap

class NetworkRegistrationResponseRepository(
    private val api: RegistrationApi = RetrofitClient.registrationApi,
) : RegistrationResponseRepository {
    private val fieldOrdersByEvent = ConcurrentHashMap<Int, Map<Int, Int>>()

    override suspend fun getRegistrations(eventId: Int): Result<List<RegistrationResponseItem>> = try {
        val response = api.getEventRegistrations(eventId)
        val body = response.body()
        if (!response.isSuccessful || body?.success != true || body.data == null) {
            Result.failure(IllegalStateException(
                body?.message?.takeIf(String::isNotBlank)
                    ?: "Could not load registrations (HTTP ${response.code()})."
            ))
        } else {
            // Keep field ordering scoped by event so detail requests never depend on
            // which response list was most recently loaded.
            loadFieldOrder(eventId, refresh = true)
            Result.success(body.data.mapNotNull(RegistrationResponseMapper::toModel))
        }
    } catch (error: Exception) {
        Result.failure(error)
    }

    override suspend fun getDetails(eventId: Int, registrationId: Int): Result<RegistrationDetailResponse> = try {
        val response = api.getEventRegistration(eventId, registrationId)
        val body = response.body()
        if (!response.isSuccessful || body?.success != true || body.data == null) {
            Result.failure(IllegalStateException(
                body?.message?.takeIf(String::isNotBlank)
                    ?: "Could not load this registration's responses (HTTP ${response.code()})."
            ))
        } else {
            Result.success(RegistrationResponseMapper.toDetailModel(body.data, loadFieldOrder(eventId)))
        }
    } catch (error: Exception) {
        Result.failure(error)
    }

    override suspend fun approve(eventId: Int, registrationId: Int): Result<Unit> = updateStatus(eventId, registrationId, true)

    override suspend fun reject(eventId: Int, registrationId: Int): Result<Unit> = updateStatus(eventId, registrationId, false)

    private suspend fun updateStatus(eventId: Int, registrationId: Int, approve: Boolean): Result<Unit> = try {
        val response = if (approve) api.approveRegistration(eventId, registrationId) else api.rejectRegistration(eventId, registrationId)
        val body = response.body()
        if (!response.isSuccessful || body?.success != true) Result.failure(IllegalStateException(
            body?.message?.takeIf(String::isNotBlank) ?: "Could not update registration review (HTTP ${response.code()})."
        )) else Result.success(Unit)
    } catch (error: Exception) { Result.failure(error) }

    private suspend fun loadFieldOrder(eventId: Int, refresh: Boolean = false): Map<Int, Int> {
        if (!refresh) fieldOrdersByEvent[eventId]?.let { return it }
        val response = runCatching { api.getRegistrationFields(eventId) }.getOrNull() ?: run {
            fieldOrdersByEvent.remove(eventId)
            return emptyMap()
        }
        val body = response.body()
        if (!response.isSuccessful || body?.success != true) {
            fieldOrdersByEvent.remove(eventId)
            return emptyMap()
        }
        val fieldOrder = body.data.orEmpty().associate { it.id to it.fieldOrder }
        // Replace metadata for this event after a successful refresh. A previous
        // list load must not leave stale field positions in a detail request.
        fieldOrdersByEvent[eventId] = fieldOrder
        return fieldOrder
    }
}
