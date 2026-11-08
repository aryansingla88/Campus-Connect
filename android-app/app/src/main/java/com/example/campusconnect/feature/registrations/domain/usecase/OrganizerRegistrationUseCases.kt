package com.example.campusconnect.feature.registrations.domain.usecase

import com.example.campusconnect.feature.registrations.domain.repository.RegistrationRepository
import com.example.campusconnect.feature.registrations.model.RegistrationConfigLoadResult
import com.example.campusconnect.feature.registrations.model.RegistrationQuestion
import com.example.campusconnect.feature.registrations.model.RegistrationSettings

data class RegistrationQuestionUseCases(
    val get: GetRegistrationQuestions,
    val save: SaveRegistrationQuestions,
)

class GetRegistrationSetup(private val repository: RegistrationRepository) {
    suspend operator fun invoke(eventId: Int): RegistrationConfigLoadResult = repository.loadSettings(eventId)
}

class SaveRegistrationSettings(private val repository: RegistrationRepository) {
    suspend operator fun invoke(eventId: Int, settings: RegistrationSettings, exists: Boolean): Result<Unit> =
        repository.saveSettings(eventId, settings, exists)
}

class GetRegistrationQuestions(private val repository: RegistrationRepository) {
    suspend operator fun invoke(eventId: Int): Result<List<RegistrationQuestion>> = repository.loadQuestions(eventId)
}

class SaveRegistrationQuestions(private val repository: RegistrationRepository) {
    suspend operator fun invoke(eventId: Int, questions: List<RegistrationQuestion>): Result<List<RegistrationQuestion>> =
        repository.saveQuestions(eventId, questions)
}

class SaveQuickFields(private val repository: RegistrationRepository) {
    suspend operator fun invoke(eventId: Int, mask: Long): Result<RegistrationSettings> =
        repository.saveQuickFields(eventId, mask)
}

