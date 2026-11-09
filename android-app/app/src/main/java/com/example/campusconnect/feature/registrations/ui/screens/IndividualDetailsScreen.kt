package com.example.campusconnect.feature.registrations.ui.screens

import com.example.campusconnect.feature.registrations.ui.components.*

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.campusconnect.feature.registrations.ui.model.ParticipantQuestionUiModel
import com.example.campusconnect.feature.registrations.model.UserRegistrationStep

/** Step 1/3: the participant's individual-type questions (profile fields come prefilled). */
@Composable
fun IndividualDetailsScreen(
    questions: List<ParticipantQuestionUiModel>,
    answers: Map<Int, String>,
    showErrors: Boolean,
    isSaving: Boolean,
    transientError: String?,
    onErrorShown: () -> Unit,
    onAnswerChange: (questionId: Int, value: String) -> Unit,
    onNext: () -> Unit,
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
        topBar = {
            FlowTopBar(
                title = "Event Registration",
                onBack = onBack,
                step = UserRegistrationStep.DETAILS.progress,
            )
        },
        bottomBar = { NextBottomBar(onClick = onNext, enabled = !isSaving, loading = isSaving, label = if (isSaving) "Saving..." else "Next") },
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
                icon = Icons.Outlined.PersonOutline,
                title = "Participant Questions",
                subtitle = "Please answer the following questions.",
            )
            questions.forEach { question ->
                ParticipantQuestionField(
                    question = question,
                    value = answers[question.backendId].orEmpty(),
                    onValueChange = { onAnswerChange(question.backendId, it) },
                    showError = showErrors,
                )
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}


