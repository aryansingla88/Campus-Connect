package com.example.campusconnect.feature.registrations.userflow.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.campusconnect.feature.registrations.ui.components.CardBg
import com.example.campusconnect.feature.registrations.ui.components.OrangePrimary
import com.example.campusconnect.feature.registrations.ui.components.RegistrationBottomBar
import com.example.campusconnect.feature.registrations.ui.screens.FormField
import com.example.campusconnect.feature.registrations.userflow.model.RegistrationQuestionScreenConfig
import com.example.campusconnect.feature.registrations.userflow.model.RegistrationQuestionScreenKind

/** One renderer for participant and team questions; business state is supplied by the owning VM. */
@Composable
fun RegistrationQuestionScreen(
    fields: List<FormField>,
    answers: Map<Int, String>,
    validationErrors: Map<Int, String>,
    transientError: String? = null,
    config: RegistrationQuestionScreenConfig,
    onAnswerChange: (fieldId: Int, value: String) -> Unit,
    onErrorShown: () -> Unit = {},
    onAction: () -> Unit,
    onBack: () -> Unit,
    secondaryActionLabel: String? = null,
    onSecondaryAction: (() -> Unit)? = null,
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
        topBar = {
            FlowTopBar(
                title = "Event Registration",
                onBack = onBack,
                step = config.progressStep,
            )
        },
        bottomBar = {
            RegistrationBottomBar {
                if (secondaryActionLabel != null && onSecondaryAction != null) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = onSecondaryAction,
                            modifier = Modifier.weight(1f).height(52.dp),
                        ) { Text(secondaryActionLabel, fontSize = 13.sp) }
                        Button(
                            onClick = onAction,
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary, contentColor = Color.White),
                            modifier = Modifier.weight(1f).height(52.dp),
                        ) {
                            Text(config.actionLabel, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                } else {
                    Button(
                        onClick = onAction,
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary, contentColor = Color.White),
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                    ) {
                        Text(config.actionLabel, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    }
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
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            val individual = config.kind == RegistrationQuestionScreenKind.INDIVIDUAL
            StepHeader(
                icon = if (individual) Icons.Outlined.PersonOutline else Icons.Outlined.Groups,
                title = if (individual) "Individual Details" else "Team Questions",
                subtitle = config.subtitle,
            )
            fields.forEach { field ->
                RegistrationQuestionField(
                    question = field,
                    value = answers[field.id].orEmpty(),
                    error = validationErrors[field.id],
                    onValueChange = { onAnswerChange(field.id, it) },
                )
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}
