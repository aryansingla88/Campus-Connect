package com.example.campusconnect.feature.registrations.data.repo

import com.example.campusconnect.core.network.RetrofitClient
import com.example.campusconnect.feature.registrations.data.mapper.toSaveAnswersRequest
import com.example.campusconnect.feature.registrations.data.mapper.toSubmissionReceipt
import com.example.campusconnect.feature.registrations.data.remote.RegistrationApi
import com.example.campusconnect.feature.registrations.data.remote.requireRegistrationData
import com.example.campusconnect.feature.registrations.domain.repository.RegistrationRepository
import com.example.campusconnect.feature.registrations.domain.repository.SubmissionRepository
import com.example.campusconnect.feature.registrations.model.QuestionScope
import com.example.campusconnect.feature.registrations.model.ParticipationMode
import com.example.campusconnect.feature.registrations.model.SubmissionReceipt
import com.example.campusconnect.feature.registrations.model.SubmissionRequest
import com.example.campusconnect.feature.registrations.model.TeamQuestionSet

class NetworkSubmissionRepository(
    private val api: RegistrationApi = RetrofitClient.registrationApi,
    private val registrations: RegistrationRepository = NetworkRegistrationRepository(api),
) : SubmissionRepository {

    override suspend fun getTeamQuestions(eventId: Int, registrationId: Int): Result<TeamQuestionSet> = runCatching {
        val questions = registrations.loadQuestions(eventId, QuestionScope.TEAM).getOrThrow()
        val saved = registrations.getRegistration(eventId).getOrThrow().teamAnswers
        TeamQuestionSet(questions, saved)
    }

    override suspend fun submit(request: SubmissionRequest): Result<SubmissionReceipt> = runCatching {
        require(request.files.isEmpty()) {
            "File upload questions are not supported yet. Your registration was not submitted. Remove the file answer or contact the event host."
        }
        if (request.context.mode != ParticipationMode.JOIN_TEAM && request.teamAnswers.isNotEmpty()) {
            api.saveParticipantAnswers(
                request.context.registrationId,
                request.teamAnswers.toSaveAnswersRequest("TEAM"),
            ).requireRegistrationData("Could not save team answers.")
        }
        api.finalizeParticipantRegistration(request.context.registrationId)
            .requireRegistrationData("Could not submit registration.")
            .toSubmissionReceipt()
    }
}
