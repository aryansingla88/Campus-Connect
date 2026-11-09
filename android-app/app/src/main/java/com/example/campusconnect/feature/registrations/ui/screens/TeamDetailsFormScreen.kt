package com.example.campusconnect.feature.registrations.ui.screens

import com.example.campusconnect.feature.registrations.ui.components.*

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.campusconnect.feature.registrations.model.AttachedFile
import com.example.campusconnect.feature.registrations.ui.model.ParticipantQuestionKind
import com.example.campusconnect.feature.registrations.ui.model.ParticipantQuestionUiModel

private const val TOTAL_FORM_STEP = 3

/** Step 3/3: shared registration questions, rendered via [ParticipantQuestionField]. */
@Composable
fun TeamDetailsFormScreen(
    questions: List<ParticipantQuestionUiModel>,
    answers: Map<Int, String>,
    files: Map<Int, AttachedFile>,
    fileErrors: Map<Int, String>,
    showErrors: Boolean,
    transientError: String?,
    onAnswerChange: (questionId: Int, value: String) -> Unit,
    onFilePicked: (questionId: Int, file: AttachedFile) -> Unit,
    onFileRemoved: (questionId: Int) -> Unit,
    onErrorShown: () -> Unit,
    onSubmit: () -> Unit,
    onBack: () -> Unit,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(transientError) {
        if (transientError != null) {
            snackbarHostState.showSnackbar(transientError)
            onErrorShown()
        }
    }

    Scaffold(
        modifier = Modifier.flowPageBackground(),
        containerColor = Color.Transparent,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = { FlowTopBar(title = "Event Registration", onBack = onBack, step = TOTAL_FORM_STEP) },
        bottomBar = {
            FlowActionBar(
                primaryLabel = "Submit Registration",
                primaryIcon = Icons.Outlined.Send,
                iconBeforeLabel = true,
                onPrimary = onSubmit,
                onBack = onBack,
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            StepHeader(
                icon = Icons.Outlined.Groups,
                title = "Shared Questions",
                subtitle = "Answer these once for this registration.",
            )

            questions.forEach { question ->
                ParticipantQuestionField(
                    question = question,
                    value = answers[question.backendId].orEmpty(),
                    onValueChange = { onAnswerChange(question.backendId, it) },
                    showError = showErrors,
                    leadingIcon = if (question.kind == ParticipantQuestionKind.LONG_TEXT || question.kind == ParticipantQuestionKind.TEXT) Icons.Outlined.Edit else null,
                    attachment = if (question.kind == ParticipantQuestionKind.FILE) {
                        QuestionAttachment(
                            file = files[question.backendId],
                            error = fileErrors[question.backendId],
                            onPicked = { onFilePicked(question.backendId, it) },
                            onRemoved = { onFileRemoved(question.backendId) },
                        )
                    } else null,
                )
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

