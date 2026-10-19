package com.example.campusconnect.feature.registrations.userflow.data

import com.example.campusconnect.feature.registrations.ui.screens.FieldType
import com.example.campusconnect.feature.registrations.ui.screens.FormField
import com.example.campusconnect.feature.registrations.userflow.model.SubmissionReceipt
import com.example.campusconnect.feature.registrations.userflow.model.SubmissionRequest
import com.example.campusconnect.feature.registrations.userflow.model.UserQuestion
import com.example.campusconnect.feature.registrations.userflow.model.toUserQuestion
import kotlinx.coroutines.delay

class FakeRegistrationSubmitRepository : RegistrationSubmitRepository {

    /** Shaped like the backend's registration-fields response (team questions have `individual = false`). */
    private val backendFields = listOf(
        FormField(
            id = 11, backendId = 11, label = "What is your team's domain?", fieldType = FieldType.SELECT,
            isRequired = true, individual = false, fieldOrder = 0,
            options = listOf("Education", "Healthcare", "FinTech", "Sustainability", "Other"),
        ),
        FormField(
            id = 12, backendId = 12, label = "Briefly describe your idea", fieldType = FieldType.TEXT,
            rawFieldType = "TEXTAREA", isRequired = true, individual = false, fieldOrder = 1,
            placeholder = "What problem are you solving?",
        ),
        FormField(
            id = 13, backendId = 13, label = "Technologies you plan to use", fieldType = FieldType.MULTISELECT,
            isRequired = false, individual = false, fieldOrder = 2,
            options = listOf("Android", "Web", "AI / ML", "IoT", "Blockchain"),
        ),
        FormField(
            id = 14, backendId = 14, label = "Upload a pitch deck", fieldType = FieldType.TEXT,
            rawFieldType = "FILE", isRequired = false, individual = false, fieldOrder = 3,
        ),
    )

    override suspend fun getTeamQuestions(eventId: Int): Result<List<UserQuestion>> {
        delay(500)
        return Result.success(
            backendFields.filterNot { it.individual }.sortedBy { it.fieldOrder }.map { it.toUserQuestion() }
        )
    }

    override suspend fun submit(request: SubmissionRequest): Result<SubmissionReceipt> {
        delay(2000)
        return Result.success(SubmissionReceipt(registrationCode = "#REG12345", status = "Completed"))
    }
}
