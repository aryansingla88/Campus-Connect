package com.example.campusconnect.feature.registrations.ui.mapper

import com.example.campusconnect.feature.registrations.model.QuestionType
import com.example.campusconnect.feature.registrations.model.RegistrationQuestion
import com.example.campusconnect.feature.registrations.ui.model.ParticipantQuestionKind
import com.example.campusconnect.feature.registrations.ui.model.ParticipantQuestionUiModel

fun RegistrationQuestion.toParticipantQuestionUiModel(readOnly: Boolean = false): ParticipantQuestionUiModel? {
    val id = backendId ?: return null
    val kind = when (val questionType = type) {
        is QuestionType.Unknown -> ParticipantQuestionKind.UNSUPPORTED
        is QuestionType.Known -> when (questionType) {
            QuestionType.Known.TEXT -> ParticipantQuestionKind.TEXT
            QuestionType.Known.TEXTAREA, QuestionType.Known.LONG_TEXT, QuestionType.Known.PARAGRAPH -> ParticipantQuestionKind.LONG_TEXT
            QuestionType.Known.PHONE -> ParticipantQuestionKind.PHONE
            QuestionType.Known.NUMBER -> ParticipantQuestionKind.NUMBER
            QuestionType.Known.SELECT, QuestionType.Known.DROPDOWN -> ParticipantQuestionKind.DROPDOWN
            QuestionType.Known.MULTISELECT -> ParticipantQuestionKind.MULTISELECT
            QuestionType.Known.DATE -> ParticipantQuestionKind.DATE
            QuestionType.Known.FILE, QuestionType.Known.FILE_UPLOAD -> ParticipantQuestionKind.FILE
        }
    }
    return ParticipantQuestionUiModel(
        backendId = id,
        label = label,
        kind = kind,
        required = isRequired,
        readOnly = readOnly,
        placeholder = placeholder,
        options = options,
        maxLength = validation.maxLength,
        maxFileBytes = validation.maxFileBytes,
    )
}
