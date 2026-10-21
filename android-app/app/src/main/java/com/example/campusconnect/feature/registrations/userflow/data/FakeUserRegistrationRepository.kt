package com.example.campusconnect.feature.registrations.userflow.data

import com.example.campusconnect.feature.registrations.ui.screens.FieldType
import com.example.campusconnect.feature.registrations.ui.screens.FormField
import com.example.campusconnect.feature.registrations.userflow.model.RegistrationDraft
import com.example.campusconnect.feature.registrations.userflow.model.TeamPortalData
import com.example.campusconnect.feature.registrations.ui.screens.RegistrationQuestionContext
import kotlinx.coroutines.delay

class FakeUserRegistrationRepository : UserRegistrationRepository {

    override fun selectParticipant(userId: Int?) {
        FakeRegistrationStore.selectParticipant(userId ?: 3)
    }

    /**
     * Profile-backed quick fields (ids 1-3) are fixed; custom questions mimic the backend's
     * registration-fields response (`individual = true`) and go through the shared mapper.
     */
    private val backendFields = listOf(
        FormField(
            id = 4, backendId = 4, label = "Branch", fieldType = FieldType.SELECT, isRequired = true,
            individual = true, fieldOrder = 0,
            options = listOf(
                "Computer Science and Engineering",
                "Information Technology",
                "Electronics and Communication",
                "Electrical Engineering",
                "Mechanical Engineering",
                "Civil Engineering",
            ),
        ),
        FormField(
            id = 5, backendId = 5, label = "Year", fieldType = FieldType.SELECT, isRequired = true,
            individual = true, fieldOrder = 1, options = listOf("1st Year", "2nd Year", "3rd Year", "4th Year"),
        ),
        FormField(
            id = 6, backendId = 6, label = "Skills", fieldType = FieldType.MULTISELECT, isRequired = false,
            individual = true, fieldOrder = 2, options = listOf("Android", "Web", "ML", "UI/UX", "Backend"),
        ),
    )

    override suspend fun createRegistration(eventId: Int): Result<RegistrationDraft> {
        delay(1500)
        val persona = FakeRegistrationStore.currentPersona()
        val profileQuestions = listOf(
            FormField(1, label = "Full Name", isRequired = true, registrationContext = RegistrationQuestionContext.INDIVIDUAL),
            FormField(2, label = "Email Address", isRequired = true, readOnly = true, registrationContext = RegistrationQuestionContext.INDIVIDUAL),
            FormField(3, label = "Phone Number", isRequired = true, registrationContext = RegistrationQuestionContext.INDIVIDUAL),
        )
        val questions = profileQuestions +
            backendFields.filter { it.individual }.sortedBy { it.fieldOrder }.map {
                it.copy(registrationContext = RegistrationQuestionContext.INDIVIDUAL)
            }
        return Result.success(
            RegistrationDraft(
                registrationId = 1,
                questions = questions,
                initialAnswers = mapOf(
                    1 to persona.name,
                    2 to persona.email,
                    3 to persona.phone,
                    4 to "Computer Science and Engineering",
                    5 to "3rd Year",
                ),
                allowSolo = true,
                allowTeam = persona.id != 3,
            )
        )
    }

    override suspend fun isTeamNameAvailable(eventId: Int, name: String): Result<Boolean> =
        runCatching { FakeRegistrationStore.isTeamNameAvailable(name) }

    override suspend fun createTeam(eventId: Int, name: String): Result<Unit> = Result.success(Unit)

    override suspend fun getTeams(eventId: Int): Result<TeamPortalData> {
        delay(600)
        return Result.success(FakeRegistrationStore.teamPortalData())
    }

    override suspend fun requestToJoin(eventId: Int, teamId: Int): Result<Unit> {
        delay(700)
        return runCatching { FakeRegistrationStore.requestToJoin(teamId) }
    }

    override suspend fun cancelJoinRequest(eventId: Int, teamId: Int): Result<Unit> =
        runCatching { FakeRegistrationStore.cancelJoinRequest(teamId) }

    override suspend fun respondToInvite(eventId: Int, inviteId: Int, accept: Boolean): Result<Unit> {
        delay(700)
        return runCatching { FakeRegistrationStore.respondToInvite(inviteId, accept) }
    }

    override suspend fun respondToJoinRequest(eventId: Int, requestId: Int, accept: Boolean): Result<Unit> =
        runCatching { FakeRegistrationStore.respondToJoinRequest(requestId, accept) }

    override suspend fun inviteMember(eventId: Int, registrationId: Int): Result<Unit> =
        runCatching { FakeRegistrationStore.inviteMember(registrationId) }

    override suspend fun cancelInvitation(eventId: Int, invitationId: Int): Result<Unit> =
        runCatching { FakeRegistrationStore.cancelInvitation(invitationId) }
}
