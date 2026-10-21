package com.example.campusconnect.feature.registrations.ui.screens

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.runtime.Composable

/**
 * Team questions: the shared [QuestionBuilderScreen] without Quick Fields, and
 * with a second "Move Existing Questions" button that opens the move sheet.
 */
@Composable
fun TeamQuestionsScreen(
    eventName: String,
    isPublished: Boolean,
    fields: List<FormField>,
    onFieldsChange: (List<FormField>) -> Unit,
    movableQuestions: List<FormField>,
    onMoveQuestions: (ids: List<Int>) -> Unit,
    onPublish: (fields: List<FormField>) -> Unit,
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
            title = "Team Questions",
            subtitle = "Questions answered once per team",
        ),
        addQuestionLabel = "Add Team Question",
        emptyTitle = "No team questions yet",
        emptyIcon = Icons.Outlined.Groups,
        emptyMessage = "You can create new questions or move existing individual questions to the team. " +
                "If you don't add any, all questions will be answered by every member.",
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
