package com.example.campusconnect.feature.registrations.data.mapper

import com.example.campusconnect.feature.registrations.data.remote.dto.ParticipantRegistrationDto
import com.example.campusconnect.feature.registrations.data.remote.dto.SaveAnswerDto
import com.example.campusconnect.feature.registrations.data.remote.dto.SaveAnswersRequest
import com.example.campusconnect.feature.registrations.model.SubmissionReceipt
import com.example.campusconnect.feature.registrations.model.RegistrationSnapshot

fun Map<Int, String>.toSaveAnswersRequest(type: String): SaveAnswersRequest = SaveAnswersRequest(
    type = type,
    answers = map { (fieldId, answer) -> SaveAnswerDto(fieldId, answer) },
)

fun ParticipantRegistrationDto.requireRegistrationId(): Int =
    registrationId ?: error("The server returned no registration ID.")

fun ParticipantRegistrationDto.answersFor(type: String): Map<Int, String> =
    answers.orEmpty()
        .filter { it.type.equals(type, ignoreCase = true) }
        .mapNotNull { answer -> answer.fieldId?.let { id -> id to answer.answer.orEmpty() } }
        .toMap()

fun ParticipantRegistrationDto.toRegistrationSnapshot(eventId: Int): RegistrationSnapshot = RegistrationSnapshot(
    eventId = this.eventId ?: eventId,
    registrationId = registrationId,
    status = status,
    teamId = teamId,
    teamName = teamName,
    canResume = canResume == true,
    individualAnswers = answersFor("INDIVIDUAL"),
    teamAnswers = answersFor("TEAM"),
)

fun ParticipantRegistrationDto.toSubmissionReceipt(): SubmissionReceipt {
    val id = requireRegistrationId()
    val displayStatus = when (status?.uppercase()) {
        "CONFIRMED" -> "Confirmed"
        "WAITLISTED" -> "Pending Review"
        "PENDING" -> "Awaiting Team Leader"
        "REJECTED" -> "Rejected"
        else -> error("The server returned an unknown registration status.")
    }
    return SubmissionReceipt("#$id", displayStatus)
}
