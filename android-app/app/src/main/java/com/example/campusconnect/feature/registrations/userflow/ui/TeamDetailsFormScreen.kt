package com.example.campusconnect.feature.registrations.userflow.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.campusconnect.feature.registrations.ui.components.CardBg
import com.example.campusconnect.feature.registrations.ui.components.OrangePrimary
import com.example.campusconnect.feature.registrations.ui.components.RegistrationBottomBar
import com.example.campusconnect.feature.registrations.userflow.model.AttachedFile
import com.example.campusconnect.feature.registrations.userflow.model.UserFieldKind
import com.example.campusconnect.feature.registrations.userflow.model.UserQuestion

private const val TOTAL_FORM_STEP = 3

/** Step 3/3: team-level questions, rendered exactly like the individual step via [UserQuestionField]. */
@Composable
fun TeamDetailsFormScreen(
    questions: List<UserQuestion>,
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
        containerColor = CardBg,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = { FlowTopBar(title = "Event Registration", onBack = onBack, step = TOTAL_FORM_STEP) },
        bottomBar = {
            RegistrationBottomBar {
                Button(
                    onClick = onSubmit,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary, contentColor = Color.White),
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                ) {
                    Text("Submit Registration", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        },
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
                icon = Icons.Outlined.Groups,
                title = "Team Details",
                subtitle = "Please answer the following questions about your team.",
            )

            questions.forEach { question ->
                UserQuestionField(
                    question = question,
                    value = answers[question.id].orEmpty(),
                    onValueChange = { onAnswerChange(question.id, it) },
                    showError = showErrors,
                    attachment = if (question.kind == UserFieldKind.FILE) {
                        QuestionAttachment(
                            file = files[question.id],
                            error = fileErrors[question.id],
                            onPicked = { onFilePicked(question.id, it) },
                            onRemoved = { onFileRemoved(question.id) },
                        )
                    } else null,
                )
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}