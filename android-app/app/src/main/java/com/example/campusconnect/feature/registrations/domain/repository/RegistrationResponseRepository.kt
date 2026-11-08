package com.example.campusconnect.feature.registrations.domain.repository

import com.example.campusconnect.feature.registrations.model.RegistrationDetailResponse
import com.example.campusconnect.feature.registrations.model.RegistrationResponseItem

/** Organizer-facing response listing, details and review. */
interface RegistrationResponseRepository {
    suspend fun getRegistrations(eventId: Int): Result<List<RegistrationResponseItem>>
    suspend fun getDetails(eventId: Int, registrationId: Int): Result<RegistrationDetailResponse>
    suspend fun review(eventId: Int, registrationId: Int, approve: Boolean): Result<Unit>
}
