package com.example.campusconnect.feature.registrations.userflow.data

import com.example.campusconnect.feature.registrations.userflow.model.SubmissionReceipt
import com.example.campusconnect.feature.registrations.userflow.model.SubmissionRequest
import com.example.campusconnect.feature.registrations.userflow.model.UserQuestion

/** Final step of the participant flow. Implement against the real backend; a fake is provided. */
interface RegistrationSubmitRepository {
    /**
     * Team-level questions (backend `individual = false` fields, converted with `FormField.toUserQuestion()`).
     * An empty list skips the form.
     */
    suspend fun getTeamQuestions(eventId: Int): Result<List<UserQuestion>>

    suspend fun submit(request: SubmissionRequest): Result<SubmissionReceipt>
}
