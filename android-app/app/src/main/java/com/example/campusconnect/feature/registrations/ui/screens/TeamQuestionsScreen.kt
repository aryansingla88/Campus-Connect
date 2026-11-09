package com.example.campusconnect.feature.registrations.ui.screens

import com.example.campusconnect.feature.registrations.ui.components.*
import com.example.campusconnect.feature.registrations.model.RegistrationQuestion

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.runtime.Composable

/**
 * Shared questions: the [QuestionBuilderScreen] without Quick Fields, and
 * with a second "Move Existing Questions" button that opens the move sheet.
 */
@Composable
fun TeamQuestionsScreen(
    eventName: String,
    isPublished: Boolean,
    fields: List<RegistrationQuestion>,
    onFieldsChange: (List<RegistrationQuestion>) -> Unit,
    movableQuestions: List<RegistrationQuestion>,
    onMoveQuestions: (localIds: List<String>) -> Unit,
    onPublish: (fields: List<RegistrationQuestion>) -> Unit,
    onBack: () -> Unit,
    isLoading: Boolean = false,
    isSaving: Boolean = false,
    isLoaded: Boolean = true,
    saveError: String? = null,
    saveSucceeded: Boolean = false,
    onRetry: () -> Unit = {},
) {
    QuestionBuilderScreen(
        eventName = eventName,
        isPublished = isPublished,
        fields = fields,
        onFieldsChange = onFieldsChange,
        onSave = { onPublish(fields) },
        onBack = onBack,
        heading = BuilderHeading(
            title = "Shared Questions",
            subtitle = "Questions answered once per registration",
        ),
        addQuestionLabel = "Add Shared Question",
        emptyTitle = "No shared questions yet",
        emptyIcon = Icons.Outlined.Groups,
        emptyMessage = "You can create questions answered once by a solo participant or the team leader, " +
                "or move existing participant questions here.",
        showQuickFields = false,
        moveExisting = MoveExistingConfig(
            candidates = movableQuestions,
            onMove = onMoveQuestions,
        ),
        isLoading = isLoading,
        isSaving = isSaving,
        isLoaded = isLoaded,
        saveError = saveError,
        saveSucceeded = saveSucceeded,
        onRetry = onRetry,
    )
}
