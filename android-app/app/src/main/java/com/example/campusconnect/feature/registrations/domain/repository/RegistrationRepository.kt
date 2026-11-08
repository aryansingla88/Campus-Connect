package com.example.campusconnect.feature.registrations.domain.repository

import com.example.campusconnect.feature.registrations.model.RegistrationConfigLoadResult
import com.example.campusconnect.feature.registrations.model.RegistrationDraft
import com.example.campusconnect.feature.registrations.model.RegistrationQuestion
import com.example.campusconnect.feature.registrations.model.RegistrationSettings
import com.example.campusconnect.feature.registrations.model.RegistrationSnapshot
import com.example.campusconnect.feature.registrations.model.QuestionScope

/** Registration setup, lifecycle and participant draft access. */
interface RegistrationRepository {
    suspend fun loadSettings(eventId: Int): RegistrationConfigLoadResult
    suspend fun saveSettings(eventId: Int, settings: RegistrationSettings, exists: Boolean): Result<Unit>
    suspend fun saveQuickFields(eventId: Int, mask: Long): Result<RegistrationSettings>

    /** Null scope loads organizer-configurable fields; a scope loads published participant questions. */
    suspend fun loadQuestions(eventId: Int, scope: QuestionScope? = null): Result<List<RegistrationQuestion>>
    suspend fun saveQuestions(eventId: Int, questions: List<RegistrationQuestion>): Result<List<RegistrationQuestion>>

    /** POST initialization is idempotent for an existing editable draft, then loads its form and saved answers. */
    suspend fun createRegistration(eventId: Int): Result<RegistrationDraft>
    suspend fun getRegistration(eventId: Int): Result<RegistrationSnapshot>
    suspend fun saveIndividualAnswers(registrationId: Int, answers: Map<Int, String>): Result<Unit>
    suspend fun cancelRegistration(eventId: Int): Result<Unit>
}
