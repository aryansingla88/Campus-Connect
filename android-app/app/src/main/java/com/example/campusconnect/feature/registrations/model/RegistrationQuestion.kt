package com.example.campusconnect.feature.registrations.model

import java.util.UUID

/** One business question, shared by organizer configuration and participant registration. */
data class RegistrationQuestion(
    /** Backend identifier. Null means this question has not been persisted yet. */
    val backendId: Int? = null,
    /** Stable client identity used by Compose keys and edit operations. Never sent to the API. */
    val localId: String = UUID.randomUUID().toString(),
    val label: String = "",
    val type: QuestionType = QuestionType.Known.TEXT,
    val isRequired: Boolean = false,
    val placeholder: String = "",
    val options: List<String> = emptyList(),
    val scope: QuestionScope = QuestionScope.INDIVIDUAL,
    val fieldOrder: Int = 0,
    val validation: QuestionValidation = QuestionValidation(),
)

enum class QuestionScope { INDIVIDUAL, TEAM }

/** Pure Kotlin question type retaining unrecognized server values verbatim. */
sealed interface QuestionType {
    val apiValue: String

    enum class Known(override val apiValue: String) : QuestionType {
        TEXT("TEXT"),
        TEXTAREA("TEXTAREA"),
        LONG_TEXT("LONG_TEXT"),
        PARAGRAPH("PARAGRAPH"),
        NUMBER("NUMBER"),
        PHONE("PHONE"),
        SELECT("SELECT"),
        DROPDOWN("DROPDOWN"),
        MULTISELECT("MULTISELECT"),
        DATE("DATE"),
        FILE("FILE"),
        FILE_UPLOAD("FILE_UPLOAD"),
    }

    data class Unknown(override val apiValue: String) : QuestionType
}

/** Existing client-side limits, kept with the business question independently of its UI. */
data class QuestionValidation(
    val maxLength: Int = 300,
    val maxFileBytes: Long = 10L * 1024 * 1024,
)

fun RegistrationQuestion.isMissing(answer: String?, hasAttachment: Boolean = false): Boolean =
    isRequired && if (type.isFileQuestion()) !hasAttachment else answer.isNullOrBlank()

fun QuestionType.isFileQuestion(): Boolean =
    this == QuestionType.Known.FILE || this == QuestionType.Known.FILE_UPLOAD
