package com.example.campusconnect.feature.registrations.model

/** Everything the earlier steps decided, passed into the submission flow. */
data class SubmissionContext(
    val eventId: Int,
    val registrationId: Int,
    val eventName: String,
    val mode: ParticipationMode,
    /** Explicit fake participant persona selected from the test screen; never sourced from Auth Me. */
    val participantId: Int? = null,
    /** Team the participant created or joined, if any. */
    val teamId: Int? = null,
    /** Answers from the Individual Details step, by question id. */
    val participantAnswers: Map<Int, String> = emptyMap(),
    val teamName: String? = null,
    /** Unsaved shared-question answers retained by the parent flow if its submission UI is recreated. */
    val initialTeamAnswers: Map<Int, String> = emptyMap(),
)

data class SubmissionRequest(
    val context: SubmissionContext,
    val teamAnswers: Map<Int, String>,
)

data class SubmissionReceipt(
    /** Real backend registration identifier. */
    val registrationCode: String,
    /** e.g. "Completed". */
    val status: String,
    /** Numeric identifier used by the existing registration-response detail route. */
    val registrationId: Int = 0,
)

data class SavedRegistrationDetails(
    val registrationId: Int,
    val participantId: Int,
    val receipt: SubmissionReceipt,
    val eventName: String,
    val mode: ParticipationMode,
    val participantAnswers: Map<Int, String>,
    val teamAnswers: Map<Int, String>,
    val teamName: String?,
    val teamId: Int?,
)

data class TeamQuestionSet(
    val questions: List<RegistrationQuestion>,
    val savedAnswers: Map<Int, String> = emptyMap(),
)
