package com.example.campusconnect.feature.registrations.ui.model

import com.example.campusconnect.feature.registrations.model.AttachedFile

/** Participant form presentation, including editing state and widget-specific constraints. */
enum class ParticipantQuestionKind { TEXT, LONG_TEXT, PHONE, NUMBER, DROPDOWN, MULTISELECT, DATE, FILE, UNSUPPORTED }

data class ParticipantQuestionUiModel(
    val backendId: Int,
    val label: String,
    val kind: ParticipantQuestionKind,
    val required: Boolean,
    val readOnly: Boolean = false,
    val placeholder: String = "",
    val options: List<String> = emptyList(),
    val maxLength: Int = 300,
    val maxFileBytes: Long = 10L * 1024 * 1024,
)

fun ParticipantQuestionUiModel.isMissing(answer: String?, attachment: AttachedFile? = null): Boolean =
    required && !readOnly && if (kind == ParticipantQuestionKind.FILE) {
        attachment == null
    } else {
        answer.isNullOrBlank()
    }
