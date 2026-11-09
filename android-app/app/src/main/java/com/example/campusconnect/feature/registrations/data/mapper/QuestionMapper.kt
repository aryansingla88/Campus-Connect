package com.example.campusconnect.feature.registrations.data.mapper

import com.example.campusconnect.feature.registrations.data.remote.dto.ParticipantQuestionDto
import com.example.campusconnect.feature.registrations.data.remote.dto.RegistrationFieldOptionRequest
import com.example.campusconnect.feature.registrations.data.remote.dto.RegistrationFieldResponse
import com.example.campusconnect.feature.registrations.data.remote.dto.RegistrationFieldRequest
import com.example.campusconnect.feature.registrations.model.QuestionScope
import com.example.campusconnect.feature.registrations.model.QuestionType
import com.example.campusconnect.feature.registrations.model.RegistrationQuestion

private fun String.toQuestionType(): QuestionType =
    QuestionType.Known.entries.firstOrNull { it.apiValue.equals(this, ignoreCase = true) }
        ?: QuestionType.Unknown(this)

fun RegistrationFieldResponse.toRegistrationQuestion(): RegistrationQuestion = RegistrationQuestion(
    backendId = id,
    localId = "backend:$id",
    label = fieldLabel,
    type = fieldType.toQuestionType(),
    isRequired = required,
    placeholder = placeholder.orEmpty(),
    options = options.sortedBy { it.optionOrder }.map { it.optionValue },
    scope = if (individual) QuestionScope.INDIVIDUAL else QuestionScope.TEAM,
    fieldOrder = fieldOrder,
)

fun ParticipantQuestionDto.toRegistrationQuestionOrNull(): RegistrationQuestion? {
    val id = id ?: return null
    return RegistrationQuestion(
        backendId = id,
        localId = "backend:$id",
        label = fieldLabel.orEmpty(),
        type = fieldType.orEmpty().toQuestionType(),
        isRequired = required == true,
        placeholder = placeholder.orEmpty(),
        options = options.orEmpty().sortedBy { it.optionOrder ?: 0 }.mapNotNull { it.optionValue },
        scope = if (individual != false) QuestionScope.INDIVIDUAL else QuestionScope.TEAM,
        fieldOrder = fieldOrder ?: 0,
    )
}

fun RegistrationQuestion.toRegistrationFieldRequest(order: Int = fieldOrder): RegistrationFieldRequest =
    RegistrationFieldRequest(
        id = backendId,
        fieldLabel = label,
        fieldType = type.apiValue,
        required = isRequired,
        placeholder = placeholder.takeIf(String::isNotBlank),
        fieldOrder = order,
        individual = scope == QuestionScope.INDIVIDUAL,
        options = options.mapIndexed { index, value ->
            RegistrationFieldOptionRequest(optionValue = value, optionOrder = index)
        },
    )
