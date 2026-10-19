package com.example.campusconnect.feature.registrations.userflow.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.campusconnect.feature.registrations.ui.components.CardBg
import com.example.campusconnect.feature.registrations.ui.components.OrangePrimary
import com.example.campusconnect.feature.registrations.ui.components.TextSecondary
import com.example.campusconnect.feature.registrations.userflow.model.ParticipationMode
import com.example.campusconnect.feature.registrations.userflow.model.SubmissionContext
import com.example.campusconnect.feature.registrations.userflow.model.SubmissionReceipt
import com.example.campusconnect.feature.registrations.userflow.viewmodel.RegistrationSubmitViewModel
import com.example.campusconnect.feature.registrations.userflow.viewmodel.SubmitPhase

private enum class SubmitScreenKey { LOADING, LOAD_FAILED, FORM, SUBMITTING, SUCCESS }

/**
 * Final part of the participant flow: Team Details form (3/3) → Submitting → Success.
 * Call it once the participant has created a team, been accepted/joined, or chosen solo.
 * If the event has no team questions it submits immediately.
 *
 * ```
 * RegistrationSubmitFlow(
 *     eventId = id, eventName = "TechNova 2025", mode = ParticipationMode.CREATE_TEAM,
 *     participantAnswers = answers,
 *     onExit = { nav.popBackStack() },
 *     onViewRegistration = { receipt -> ... },
 *     onBackToEvent = { ... },
 * )
 * ```
 */
@Composable
fun RegistrationSubmitFlow(
    eventId: Int,
    eventName: String,
    mode: ParticipationMode,
    onExit: () -> Unit,
    onViewRegistration: (SubmissionReceipt) -> Unit,
    onBackToEvent: () -> Unit,
    teamId: Int? = null,
    participantAnswers: Map<Int, String> = emptyMap(),
    viewModel: RegistrationSubmitViewModel = viewModel(),
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(eventId, eventName, mode, teamId, participantAnswers) {
        viewModel.start(SubmissionContext(eventId, eventName, mode, teamId, participantAnswers))
    }

    val phase = state.phase
    val key = when (phase) {
        SubmitPhase.Loading -> SubmitScreenKey.LOADING
        is SubmitPhase.LoadFailed -> SubmitScreenKey.LOAD_FAILED
        SubmitPhase.Form -> SubmitScreenKey.FORM
        SubmitPhase.Submitting -> SubmitScreenKey.SUBMITTING
        is SubmitPhase.Success -> SubmitScreenKey.SUCCESS
    }

    BackHandler {
        when (phase) {
            SubmitPhase.Submitting -> Unit // don't abandon a submission in flight
            is SubmitPhase.Success -> onBackToEvent()
            else -> onExit()
        }
    }

    Crossfade(targetState = key, label = "submitStep") { screen ->
        when (screen) {
            SubmitScreenKey.LOADING -> Column(
                Modifier.fillMaxSize().background(CardBg),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) { CircularProgressIndicator(color = OrangePrimary) }

            SubmitScreenKey.LOAD_FAILED -> Column(
                Modifier.fillMaxSize().background(CardBg).padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    (phase as? SubmitPhase.LoadFailed)?.message ?: "Something went wrong.",
                    fontSize = 14.sp,
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = viewModel::retryLoad,
                    colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary, contentColor = Color.White),
                ) { Text("Try again") }
            }

            SubmitScreenKey.FORM -> TeamDetailsFormScreen(
                questions = state.questions,
                answers = state.answers,
                files = state.files,
                fileErrors = state.fileErrors,
                showErrors = state.showErrors,
                transientError = state.transientError,
                onAnswerChange = viewModel::onAnswerChange,
                onFilePicked = viewModel::onFilePicked,
                onFileRemoved = viewModel::onFileRemoved,
                onErrorShown = viewModel::consumeError,
                onSubmit = viewModel::submit,
                onBack = onExit,
            )

            SubmitScreenKey.SUBMITTING -> SubmittingRegistrationScreen()

            SubmitScreenKey.SUCCESS -> {
                val receipt = (phase as? SubmitPhase.Success)?.receipt
                if (receipt != null) {
                    RegistrationSuccessScreen(
                        eventName = eventName,
                        receipt = receipt,
                        onViewRegistration = { onViewRegistration(receipt) },
                        onBackToEvent = onBackToEvent,
                    )
                }
            }
        }
    }
}
