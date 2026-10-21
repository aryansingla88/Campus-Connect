package com.example.campusconnect.feature.registrations.userflow.data

import com.example.campusconnect.feature.registrations.userflow.model.SubmissionReceipt
import com.example.campusconnect.feature.registrations.userflow.model.SubmissionRequest
import com.example.campusconnect.feature.registrations.userflow.model.SavedRegistrationDetails
import com.example.campusconnect.feature.registrations.ui.screens.FormField

/** Final step of the participant flow. Implement against the real backend; a fake is provided. */
interface RegistrationSubmitRepository {
    /**
     * Team-level questions using the same host [FormField] model as the form builder.
     */
    suspend fun getTeamQuestions(eventId: Int): Result<List<FormField>>

    /** Saved shared answers visible to this participant, empty for members without edit visibility. */
    suspend fun getSavedTeamAnswers(eventId: Int): Result<Map<Int, String>> = Result.success(emptyMap())

    suspend fun submit(request: SubmissionRequest): Result<SubmissionReceipt>

    fun getSavedRegistration(participantId: Int): SavedRegistrationDetails? = null
}
