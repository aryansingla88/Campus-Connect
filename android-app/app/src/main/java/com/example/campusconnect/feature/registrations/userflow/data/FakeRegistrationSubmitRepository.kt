package com.example.campusconnect.feature.registrations.userflow.data

import com.example.campusconnect.feature.registrations.ui.screens.FieldType
import com.example.campusconnect.feature.registrations.ui.screens.FormField
import com.example.campusconnect.feature.registrations.userflow.model.SubmissionReceipt
import com.example.campusconnect.feature.registrations.userflow.model.SubmissionRequest
import com.example.campusconnect.feature.registrations.ui.screens.RegistrationQuestionContext
import kotlinx.coroutines.delay

class FakeRegistrationSubmitRepository : RegistrationSubmitRepository {

    override fun getSavedRegistration(participantId: Int) = FakeRegistrationStore.registrationDetails(participantId)

    private val commonQuestions = listOf(
        FormField(
            id = 10, backendId = 10, label = "What do you hope to get from this event?",
            fieldType = FieldType.TEXT, isRequired = true,
            individual = false, fieldOrder = -1, placeholder = "Share your goals for participating.",
            registrationContext = RegistrationQuestionContext.TEAM_COMMON,
        ),
    )

    /** Shaped like the backend's registration-fields response (team questions have `individual = false`). */
    private val backendFields = listOf(
        FormField(
            id = 11, backendId = 11, label = "What is your team's domain?", fieldType = FieldType.SELECT,
            isRequired = true, individual = false, fieldOrder = 0,
            options = listOf("Education", "Healthcare", "FinTech", "Sustainability", "Other"),
        ),
        FormField(
            id = 12, backendId = 12, label = "Briefly describe your idea", fieldType = FieldType.TEXT,
            isRequired = true, individual = false, fieldOrder = 1,
            placeholder = "What problem are you solving?",
        ),
        FormField(
            id = 13, backendId = 13, label = "Technologies you plan to use", fieldType = FieldType.MULTISELECT,
            isRequired = false, individual = false, fieldOrder = 2,
            options = listOf("Android", "Web", "AI / ML", "IoT", "Blockchain"),
        ),
    )

    override suspend fun getTeamQuestions(eventId: Int): Result<List<FormField>> {
        delay(500)
        val teamQuestions = backendFields.sortedBy { it.fieldOrder }.map { it.copy(
            registrationContext = RegistrationQuestionContext.TEAM_CREATION,
        ) }
        return Result.success(commonQuestions + teamQuestions)
    }

    override suspend fun submit(request: SubmissionRequest): Result<SubmissionReceipt> = runCatching {
        delay(2000)
        request.context.participantId?.let(FakeRegistrationStore::selectParticipant)
        val normalizedTeamAnswers = request.teamAnswers.mapValues { (id, value) ->
            if (id == 13) value.split(',', '\n').map(String::trim).filter(String::isNotEmpty).joinToString(",") else value
        }
        val normalizedParticipantAnswers = request.context.participantAnswers.mapValues { (id, value) ->
            if (id == 6) value.split(',', '\n').map(String::trim).filter(String::isNotEmpty).joinToString(",") else value
        }
        val saved = request.copy(
            context = request.context.copy(
                participantAnswers = normalizedParticipantAnswers + normalizedTeamAnswers.filterKeys { it == 10 },
            ),
            teamAnswers = normalizedTeamAnswers.filterKeys { it != 10 },
        )
        val createdTeam = FakeRegistrationStore.createTeam(saved)
        val registrationId = FakeRegistrationStore.allocateRegistrationId()
        val receipt = SubmissionReceipt(
            registrationCode = "#REG$registrationId",
            status = "CONFIRMED",
            registrationId = registrationId,
        )
        FakeRegistrationStore.saveRegistration(saved, receipt, createdTeam?.id)
        receipt
    }
}
