package com.example.campusconnect.feature.registrations.data

import com.example.campusconnect.core.network.RetrofitClient
import com.example.campusconnect.feature.events.data.remote.EventsApi
import com.example.campusconnect.feature.registrations.data.remote.SaveAnswerDto
import com.example.campusconnect.feature.registrations.data.remote.SaveAnswersRequest
import com.example.campusconnect.feature.registrations.model.ParticipationMode
import com.example.campusconnect.feature.registrations.model.SubmissionReceipt
import com.example.campusconnect.feature.registrations.model.SubmissionRequest
import com.example.campusconnect.feature.registrations.model.UserQuestion
import com.example.campusconnect.feature.registrations.data.TeamQuestionSet
import com.example.campusconnect.feature.registrations.model.toUserQuestion
import retrofit2.Response

class NetworkRegistrationSubmitRepository(
    private val api: EventsApi = RetrofitClient.eventsApi,
) : RegistrationSubmitRepository {
    override suspend fun getTeamQuestions(eventId: Int, registrationId: Int): Result<TeamQuestionSet> = runCatching {
        val questions = api.getParticipantRegistrationQuestions(eventId, individual = false)
            .requireData("Could not load team questions.")
            .mapNotNull { dto ->
                val id = dto.id ?: return@mapNotNull null
                val type = dto.fieldType.orEmpty()
                val known = com.example.campusconnect.feature.registrations.ui.screens.FieldType.entries
                    .firstOrNull { it.name.equals(type, ignoreCase = true) }
                com.example.campusconnect.feature.registrations.ui.screens.FormField(
                    id = id,
                    backendId = id,
                    label = dto.fieldLabel.orEmpty(),
                    fieldType = known ?: com.example.campusconnect.feature.registrations.ui.screens.FieldType.TEXT,
                    rawFieldType = if (known == null) type else null,
                    placeholder = dto.placeholder.orEmpty(),
                    isRequired = dto.required == true,
                    options = dto.options.orEmpty().sortedBy { it.optionOrder ?: 0 }.mapNotNull { it.optionValue },
                    individual = false,
                    fieldOrder = dto.fieldOrder ?: 0,
                ).toUserQuestion()
            }
        val saved = api.getParticipantRegistration(eventId).requireData("Could not restore team answers.")
            .answers.orEmpty().filter { it.type.equals("TEAM", true) }
            .mapNotNull { answer -> answer.fieldId?.let { id -> id to answer.answer.orEmpty() } }.toMap()
        TeamQuestionSet(questions, saved)
    }

    override suspend fun submit(request: SubmissionRequest): Result<SubmissionReceipt> = runCatching {
        require(request.files.isEmpty()) {
            "File upload questions are not supported yet. Your registration was not submitted. Remove the file answer or contact the event host."
        }
        if (request.context.mode == ParticipationMode.CREATE_TEAM && request.teamAnswers.isNotEmpty()) {
            api.saveParticipantAnswers(
                request.context.registrationId,
                SaveAnswersRequest("TEAM", request.teamAnswers.map { (id, value) -> SaveAnswerDto(id, value) }),
            ).requireData("Could not save team answers.")
        }
        val response = api.finalizeParticipantRegistration(request.context.registrationId)
            .requireData("Could not submit registration.")
        val id = response.registrationId ?: error("The server returned no registration ID.")
        val status = when (response.status?.uppercase()) {
            "CONFIRMED" -> "Confirmed"
            "WAITLISTED" -> "Pending Review"
            "PENDING" -> "Awaiting Team Leader"
            "REJECTED" -> "Rejected"
            else -> error("The server returned an unknown registration status.")
        }
        SubmissionReceipt("#$id", status)
    }

    private fun <T : Any> Response<com.example.campusconnect.core.network.ApiResponse<T>>.requireData(
        fallback: String,
    ): T {
        val envelope = body()
        if (!isSuccessful || envelope?.success != true) {
            error(envelope?.message?.takeIf(String::isNotBlank) ?: "$fallback (HTTP ${this.code()}).")
        }
        return envelope?.data ?: error(envelope?.message?.takeIf(String::isNotBlank) ?: fallback)
    }
}
