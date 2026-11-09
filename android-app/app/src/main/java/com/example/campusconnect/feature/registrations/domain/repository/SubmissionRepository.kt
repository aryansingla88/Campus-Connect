package com.example.campusconnect.feature.registrations.domain.repository

import com.example.campusconnect.feature.registrations.model.SubmissionReceipt
import com.example.campusconnect.feature.registrations.model.SubmissionRequest
import com.example.campusconnect.feature.registrations.model.TeamQuestionSet

/** Shared answer entry and final registration submission. */
interface SubmissionRepository {
    suspend fun getTeamQuestions(eventId: Int, registrationId: Int): Result<TeamQuestionSet>
    suspend fun submit(request: SubmissionRequest): Result<SubmissionReceipt>
}
