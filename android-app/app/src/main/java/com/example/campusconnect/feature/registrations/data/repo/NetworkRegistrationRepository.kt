package com.example.campusconnect.feature.registrations.data.repo

import com.example.campusconnect.core.network.RetrofitClient
import com.example.campusconnect.feature.registrations.data.mapper.answersFor
import com.example.campusconnect.feature.registrations.data.mapper.requireRegistrationId
import com.example.campusconnect.feature.registrations.data.mapper.toParticipationRules
import com.example.campusconnect.feature.registrations.data.mapper.toRegistrationConfigRequest
import com.example.campusconnect.feature.registrations.data.mapper.toRegistrationQuestion
import com.example.campusconnect.feature.registrations.data.mapper.toRegistrationQuestionOrNull
import com.example.campusconnect.feature.registrations.data.mapper.toRegistrationSettings
import com.example.campusconnect.feature.registrations.data.mapper.toRegistrationSnapshot
import com.example.campusconnect.feature.registrations.data.mapper.toRegistrationFieldRequest
import com.example.campusconnect.feature.registrations.data.mapper.toSaveAnswersRequest
import com.example.campusconnect.feature.registrations.data.remote.RegistrationApi
import com.example.campusconnect.feature.registrations.data.remote.requireRegistrationData
import com.example.campusconnect.feature.registrations.data.remote.requireRegistrationSuccess
import com.example.campusconnect.feature.registrations.data.remote.dto.QuickFieldsConfigRequest
import com.example.campusconnect.feature.registrations.domain.repository.RegistrationRepository
import com.example.campusconnect.feature.registrations.model.QuestionScope
import com.example.campusconnect.feature.registrations.model.RegistrationConfigLoadResult
import com.example.campusconnect.feature.registrations.model.RegistrationDraft
import com.example.campusconnect.feature.registrations.model.RegistrationQuestion
import com.example.campusconnect.feature.registrations.model.RegistrationSettings
import com.example.campusconnect.feature.registrations.model.RegistrationSnapshot

class NetworkRegistrationRepository(
    private val api: RegistrationApi = RetrofitClient.registrationApi,
) : RegistrationRepository {

    override suspend fun loadSettings(eventId: Int): RegistrationConfigLoadResult {
        val response = api.getRegistrationConfig(eventId)
        val body = response.body()
        if (!response.isSuccessful) {
            return RegistrationConfigLoadResult.Failure("Could not load registration settings (HTTP ${response.code()}).")
        }
        if (body?.success == true) {
            val config = body.data
                ?: return RegistrationConfigLoadResult.Failure("The server returned an empty registration configuration.")
            return RegistrationConfigLoadResult.Existing(config.toRegistrationSettings())
        }
        val message = body?.message.orEmpty()
        if (message.contains("configuration not found", ignoreCase = true)) {
            return RegistrationConfigLoadResult.Missing
        }
        return RegistrationConfigLoadResult.Failure(message.ifBlank { "Could not load registration settings." })
    }

    override suspend fun saveSettings(
        eventId: Int,
        settings: RegistrationSettings,
        exists: Boolean,
    ): Result<Unit> {
        if (settings.minMembers < 1 || settings.minMembers > RegistrationSettings.MAX_TEAM_SIZE ||
            settings.maxMembers < settings.minMembers || settings.maxMembers > RegistrationSettings.MAX_TEAM_SIZE ||
            (settings.minMembers > 1 && settings.maxMembers < 2)
        ) return Result.failure(IllegalArgumentException("Choose valid minimum and maximum team sizes."))

        return runCatching {
            val request = settings.toRegistrationConfigRequest()
            val response = if (exists) api.updateRegistrationConfig(eventId, request)
            else api.createRegistrationConfig(eventId, request)
            response.requireRegistrationData("Could not save registration settings.")
            Unit
        }
    }

    override suspend fun saveQuickFields(eventId: Int, mask: Long): Result<RegistrationSettings> = runCatching {
        api.updateQuickFieldsConfig(eventId, QuickFieldsConfigRequest(mask))
            .requireRegistrationData("Could not save Quick Fields.")
            .toRegistrationSettings()
    }

    override suspend fun loadQuestions(
        eventId: Int,
        scope: QuestionScope?,
    ): Result<List<RegistrationQuestion>> = runCatching { fetchQuestions(eventId, scope) }

    private suspend fun fetchQuestions(eventId: Int, scope: QuestionScope?): List<RegistrationQuestion> {
        if (scope == null) {
            return api.getRegistrationFields(eventId)
                .requireRegistrationData(
                    fallback = "Could not load registration questions.",
                    emptyFallback = "The server returned no registration questions.",
                )
                .sortedBy { it.fieldOrder }
                .map { it.toRegistrationQuestion() }
        }
        val individual = scope == QuestionScope.INDIVIDUAL
        return api.getParticipantRegistrationQuestions(eventId, individual)
            .requireRegistrationData("Could not load ${scope.name.lowercase()} registration questions.")
            .mapNotNull { dto -> dto.toRegistrationQuestionOrNull()?.copy(scope = scope) }
    }

    override suspend fun saveQuestions(
        eventId: Int,
        questions: List<RegistrationQuestion>,
    ): Result<List<RegistrationQuestion>> = runCatching {
        val ordered = questions.withIndex().sortedWith(
            compareBy<IndexedValue<RegistrationQuestion>> { if (it.value.backendId == null) 1 else 0 }
                .thenBy { if (it.value.backendId == null) it.index else it.value.fieldOrder }
        ).mapIndexed { order, entry -> entry.value.toRegistrationFieldRequest(order) }
        api.updateRegistrationFields(eventId, ordered)
            .requireRegistrationData("Could not save registration questions.")
            .sortedBy { it.fieldOrder }
            .map { it.toRegistrationQuestion() }
    }

    override suspend fun createRegistration(eventId: Int): Result<RegistrationDraft> = runCatching {
        val initialized = api.initializeParticipantRegistration(eventId)
            .requireRegistrationData("Could not start registration.")
        val registrationId = initialized.requireRegistrationId()
        val rules = api.getParticipantRegistrationConfig(eventId)
            .requireRegistrationData("Could not load event registration rules.")
            .toParticipationRules()
        val questions = fetchQuestions(eventId, QuestionScope.INDIVIDUAL)
        val saved = getRegistration(eventId).getOrThrow()
        RegistrationDraft(
            registrationId = registrationId,
            questions = questions,
            initialAnswers = saved.individualAnswers,
            allowSolo = rules.allowSolo,
            allowTeam = rules.allowTeam,
        )
    }

    override suspend fun getRegistration(eventId: Int): Result<RegistrationSnapshot> = runCatching {
        api.getParticipantRegistration(eventId)
            .requireRegistrationData("Could not load registration.")
            .toRegistrationSnapshot(eventId)
    }

    override suspend fun saveIndividualAnswers(
        registrationId: Int,
        answers: Map<Int, String>,
    ): Result<Unit> = runCatching {
        api.saveParticipantAnswers(registrationId, answers.toSaveAnswersRequest("INDIVIDUAL"))
            .requireRegistrationData("Could not save individual answers.")
        Unit
    }

    override suspend fun cancelRegistration(eventId: Int): Result<Unit> = runCatching {
        api.cancelParticipantRegistration(eventId).requireRegistrationSuccess("Could not cancel registration.")
        Unit
    }
}
