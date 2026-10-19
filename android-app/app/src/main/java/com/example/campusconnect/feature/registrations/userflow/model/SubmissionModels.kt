package com.example.campusconnect.feature.registrations.userflow.model

import java.util.Locale

data class AttachedFile(
    val uri: String,
    val name: String,
    val sizeBytes: Long,
) {
    val readableSize: String
        get() = when {
            sizeBytes <= 0 -> ""
            sizeBytes < 1024 * 1024 -> String.format(Locale.US, "%.0f KB", sizeBytes / 1024.0)
            else -> String.format(Locale.US, "%.1f MB", sizeBytes / (1024.0 * 1024.0))
        }
}

/** Everything the earlier steps decided, passed into the submission flow. */
data class SubmissionContext(
    val eventId: Int,
    val eventName: String,
    val mode: ParticipationMode,
    /** Team the participant created or joined, if any. */
    val teamId: Int? = null,
    /** Answers from the Individual Details step, by question id. */
    val participantAnswers: Map<Int, String> = emptyMap(),
)

data class SubmissionRequest(
    val context: SubmissionContext,
    val teamAnswers: Map<Int, String>,
    val files: Map<Int, AttachedFile>,
)

data class SubmissionReceipt(
    /** e.g. "#REG12345". */
    val registrationCode: String,
    /** e.g. "Completed". */
    val status: String,
)
