package com.example.campusconnect.feature.registrations.userflow.data

import com.example.campusconnect.core.network.RetrofitClient
import com.example.campusconnect.feature.registrations.data.mapper.RegistrationFieldMapper
import com.example.campusconnect.feature.registrations.userflow.model.ParticipationMode
import com.example.campusconnect.feature.registrations.userflow.model.SubmissionReceipt
import com.example.campusconnect.feature.registrations.userflow.model.SubmissionRequest
import com.example.campusconnect.feature.registrations.ui.screens.FormField

/** Production participant submission orchestration. All mutations are authorized by the backend session. */
class NetworkRegistrationSubmitRepository(
    private val participantRepository: NetworkUserRegistrationRepository = ParticipantRegistrationRepositories.user,
) : RegistrationSubmitRepository {
    private val api = RetrofitClient.registrationApi

    override suspend fun getTeamQuestions(eventId: Int): Result<List<FormField>> = runCatching {
        val response = api.getParticipantQuestions(eventId, false)
        val body = response.body()
        if (!response.isSuccessful || body?.success != true || body.data == null) {
            error(body?.message?.takeIf(String::isNotBlank) ?: "Could not load participation questions (HTTP ${response.code()}).")
        }
        body.data.sortedBy { it.fieldOrder }.map(RegistrationFieldMapper::toModel)
    }

    override suspend fun getSavedTeamAnswers(eventId: Int): Result<Map<Int, String>> = runCatching {
        val response = api.getParticipantRegistration(eventId)
        val body = response.body()
        if (!response.isSuccessful || body?.success != true || body.data == null) {
            error(body?.message?.takeIf(String::isNotBlank) ?: "Could not restore saved registration answers (HTTP ${response.code()}).")
        }
        body.data.answers.filter { it.type.equals("TEAM", ignoreCase = true) }
            .associate { it.fieldId to it.answer }
    }

    override suspend fun submit(request: SubmissionRequest): Result<SubmissionReceipt> = runCatching {
        val context = request.context
        participantRepository.saveAnswers(context.eventId, "INDIVIDUAL", context.participantAnswers).getOrThrow()
        if (context.mode == ParticipationMode.CREATE_TEAM) {
            val teamName = context.teamName?.trim().takeUnless { it.isNullOrBlank() }
                ?: error("Enter a team name before submitting.")
            if (!participantRepository.hasCreatedTeam(context.eventId)) {
                participantRepository.createTeam(context.eventId, teamName).getOrThrow()
            }
            participantRepository.saveAnswers(context.eventId, "TEAM", request.teamAnswers).getOrThrow()
        } else if (context.mode == ParticipationMode.SOLO) {
            // Shared participation answers belong to the solo draft too; backend accepts this for eligible solo events.
            participantRepository.saveAnswers(context.eventId, "TEAM", request.teamAnswers).getOrThrow()
        }
        val finalized = participantRepository.finalize(context.eventId).getOrThrow()
        val registrationId = finalized.registrationId ?: error("The server did not return a registration ID.")
        SubmissionReceipt("#REG$registrationId", finalized.status ?: "SUBMITTED", registrationId)
    }
}

object ParticipantRegistrationRepositories {
    val user: NetworkUserRegistrationRepository by lazy { NetworkUserRegistrationRepository() }
    val submit: NetworkRegistrationSubmitRepository by lazy { NetworkRegistrationSubmitRepository(user) }
}
