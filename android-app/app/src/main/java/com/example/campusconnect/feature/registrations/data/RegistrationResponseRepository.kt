package com.example.campusconnect.feature.registrations.data

import com.example.campusconnect.feature.registrations.model.RegistrationDetailResponse

/**
 * Source of detailed registration answers. The short list
 * ([com.example.campusconnect.feature.registrations.model.RegistrationResponseItem])
 * keeps supplying identity, status, type and submission metadata.
 *
 * Swap [FakeRegistrationResponseRepository] for a network-backed implementation
 * once the real endpoint is available; nothing else needs to change.
 */
interface RegistrationResponseRepository {
    suspend fun getDetails(registrationId: Int): Result<RegistrationDetailResponse>
}
