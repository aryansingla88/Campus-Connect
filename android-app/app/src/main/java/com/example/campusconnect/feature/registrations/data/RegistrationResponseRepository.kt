package com.example.campusconnect.feature.registrations.data

import com.example.campusconnect.feature.registrations.model.RegistrationDetailResponse
import com.example.campusconnect.feature.registrations.model.RegistrationResponseItem

/**
 * Source of detailed registration answers. The short list
 * ([com.example.campusconnect.feature.registrations.model.RegistrationResponseItem])
 * keeps supplying identity, status, type and submission metadata.
 *
 * Backed by the existing host registrations and registration detail endpoints.
 */
interface RegistrationResponseRepository {
    suspend fun getRegistrations(eventId: Int): Result<List<RegistrationResponseItem>>
    suspend fun getDetails(eventId: Int, registrationId: Int): Result<RegistrationDetailResponse>
}
