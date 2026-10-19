package com.example.campusconnect.feature.registrations.userflow.data

import com.example.campusconnect.feature.registrations.ui.screens.FieldType
import com.example.campusconnect.feature.registrations.ui.screens.FormField
import com.example.campusconnect.feature.registrations.userflow.model.RegistrationDraft
import com.example.campusconnect.feature.registrations.userflow.model.TeamInvite
import com.example.campusconnect.feature.registrations.userflow.model.TeamPortalData
import com.example.campusconnect.feature.registrations.userflow.model.TeamSummary
import com.example.campusconnect.feature.registrations.userflow.model.UserFieldKind
import com.example.campusconnect.feature.registrations.userflow.model.UserQuestion
import com.example.campusconnect.feature.registrations.userflow.model.toUserQuestion
import kotlinx.coroutines.delay

class FakeUserRegistrationRepository : UserRegistrationRepository {

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
        val profileQuestions = listOf(
            UserQuestion(1, "Full Name", UserFieldKind.TEXT, required = true),
            UserQuestion(2, "Email Address", UserFieldKind.TEXT, required = true, readOnly = true),
            UserQuestion(3, "Phone Number", UserFieldKind.PHONE, required = true),
        )
        val questions = profileQuestions +
            backendFields.filter { it.individual }.sortedBy { it.fieldOrder }.map { it.toUserQuestion() }
        return Result.success(
            RegistrationDraft(
                registrationId = 1,
                questions = questions,
                initialAnswers = mapOf(
                    1 to "Aarav Sharma",
                    2 to "aarav@abc.edu",
                    3 to "+91 98765 43210",
                    4 to "Computer Science and Engineering",
                    5 to "3rd Year",
                ),
            )
        )
    }

    override suspend fun getTeams(eventId: Int): Result<TeamPortalData> {
        delay(600)
        return Result.success(
            TeamPortalData(
                teams = listOf(
                    TeamSummary(1, "Code Crafters", "Riya Malhotra", 3, 4, listOf("Kotlin", "Android", "ML")),
                    TeamSummary(2, "Byte Builders", "Dev Patel", 2, 4, listOf("UI/UX", "Firebase", "Web")),
                    TeamSummary(3, "Syntax Squad", "Isha Rao", 1, 4, listOf("Web", "AI")),
                    TeamSummary(4, "Think Tank", "Kabir Joshi", 3, 4, listOf("ML", "DL", "Data Science")),
                    TeamSummary(5, "Pixel Pioneers", "Meera Nair", 4, 4, listOf("Design", "Web")),
                ),
                invites = listOf(
                    TeamInvite(
                        101, "InnovateX", "Aryan Mehta", 4, 4,
                        sentAgo = "2 hrs ago",
                        memberNames = listOf("Aryan Mehta", "Rohan Das", "Meera Nair", "Tanmay Jain"),
                        teamId = 6,
                    ),
                    TeamInvite(
                        102, "Tech Titans", "Karan Singh", 3, 4,
                        sentAgo = "1 day ago",
                        memberNames = listOf("Karan Singh", "Priya Verma", "Sneha Kapoor"),
                        teamId = 7,
                    ),
                ),
            )
        )
    }

    override suspend fun requestToJoin(eventId: Int, teamId: Int): Result<Unit> {
        delay(700)
        return Result.success(Unit)
    }

    override suspend fun respondToInvite(eventId: Int, inviteId: Int, accept: Boolean): Result<Unit> {
        delay(700)
        return Result.success(Unit)
    }
}
