package com.example.campusconnect.feature.registrations.data

import com.example.campusconnect.feature.registrations.model.SubmissionReceipt
import com.example.campusconnect.feature.registrations.model.SubmissionRequest
import com.example.campusconnect.feature.registrations.model.UserQuestion

data class TeamQuestionSet(val questions: List<UserQuestion>, val savedAnswers: Map<Int, String> = emptyMap())

/** Final step of the participant flow. */
interface RegistrationSubmitRepository {
    /**
     * Team-level questions (backend `individual = false` fields, converted with `FormField.toUserQuestion()`).
     * An empty list skips the form.
     */
    suspend fun getTeamQuestions(eventId: Int, registrationId: Int): Result<TeamQuestionSet>

    suspend fun submit(request: SubmissionRequest): Result<SubmissionReceipt>
}
