package com.example.campusconnect.feature.registrations.userflow.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.campusconnect.feature.registrations.ui.components.CardBg
import com.example.campusconnect.feature.registrations.userflow.model.UserQuestion
import com.example.campusconnect.feature.registrations.userflow.model.UserRegistrationStep

/** Step 1/3: the participant's individual-type questions (profile fields come prefilled). */
@Composable
fun IndividualDetailsScreen(
    questions: List<UserQuestion>,
    answers: Map<Int, String>,
    showErrors: Boolean,
    onAnswerChange: (questionId: Int, value: String) -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit,
) {
    Scaffold(
        containerColor = CardBg,
        topBar = {
            FlowTopBar(
                title = "Event Registration",
                onBack = onBack,
                step = UserRegistrationStep.DETAILS.progress,
            )
        },
        bottomBar = { NextBottomBar(onClick = onNext) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            StepHeader(
                icon = Icons.Outlined.PersonOutline,
                title = "Individual Details",
                subtitle = "Please answer the following questions.",
            )
            questions.forEach { question ->
                UserQuestionField(
                    question = question,
                    value = answers[question.id].orEmpty(),
                    onValueChange = { onAnswerChange(question.id, it) },
                    showError = showErrors,
                )
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}
