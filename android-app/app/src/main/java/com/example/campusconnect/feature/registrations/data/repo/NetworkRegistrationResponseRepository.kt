package com.example.campusconnect.feature.registrations.data.repo

import com.example.campusconnect.core.network.RetrofitClient
import com.example.campusconnect.feature.registrations.data.mapper.toRegistrationDetailResponse
import com.example.campusconnect.feature.registrations.data.mapper.toRegistrationResponseItemOrNull
import com.example.campusconnect.feature.registrations.data.remote.RegistrationApi
import com.example.campusconnect.feature.registrations.data.remote.requireRegistrationData
import com.example.campusconnect.feature.registrations.domain.repository.RegistrationRepository
import com.example.campusconnect.feature.registrations.domain.repository.RegistrationResponseRepository
import com.example.campusconnect.feature.registrations.model.RegistrationDetailResponse
import com.example.campusconnect.feature.registrations.model.RegistrationResponseItem

class NetworkRegistrationResponseRepository(
    private val api: RegistrationApi = RetrofitClient.registrationApi,
    private val registrations: RegistrationRepository = NetworkRegistrationRepository(api),
) : RegistrationResponseRepository {
    @Volatile private var fieldOrderById: Map<Int, Int> = emptyMap()

    override suspend fun getRegistrations(eventId: Int): Result<List<RegistrationResponseItem>> = try {
        fieldOrderById = emptyMap()
        val items = api.getEventRegistrations(eventId)
            .requireRegistrationData("Could not load registrations.")
        // Reuse registration question retrieval/mapping to retain answer ordering.
        registrations.loadQuestions(eventId).getOrNull()?.let { questions ->
            fieldOrderById = questions.associate { question ->
                requireNotNull(question.backendId) to question.fieldOrder
            }
        }
        Result.success(items.mapNotNull { it.toRegistrationResponseItemOrNull() })
    } catch (error: Exception) {
        Result.failure(error)
    }

    override suspend fun getDetails(eventId: Int, registrationId: Int): Result<RegistrationDetailResponse> = try {
        val detail = api.getEventRegistration(eventId, registrationId)
            .requireRegistrationData("Could not load this registration's responses.")
        Result.success(detail.toRegistrationDetailResponse(fieldOrderById))
    } catch (error: Exception) {
        Result.failure(error)
    }

    override suspend fun review(eventId: Int, registrationId: Int, approve: Boolean): Result<Unit> = try {
        val response = if (approve) api.approveRegistration(eventId, registrationId)
        else api.rejectRegistration(eventId, registrationId)
        response.requireRegistrationData("Could not update registration status.")
        Result.success(Unit)
    } catch (error: Exception) {
        Result.failure(error)
    }
}
