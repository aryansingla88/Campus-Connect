package com.example.campusconnect.feature.registrations.ui.flows

import com.example.campusconnect.feature.registrations.ui.screens.*
import com.example.campusconnect.feature.registrations.ui.components.*

import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.campusconnect.feature.registrations.RegistrationDependencies
import com.example.campusconnect.feature.registrations.model.ParticipationMode
import com.example.campusconnect.feature.registrations.model.SubmissionContext
import com.example.campusconnect.feature.registrations.model.SubmissionReceipt
import com.example.campusconnect.feature.registrations.viewmodel.SubmissionViewModel
import com.example.campusconnect.feature.registrations.viewmodel.SubmitPhase

private enum class SubmitScreenKey { LOADING, LOAD_FAILED, FORM, SUBMITTING, SUCCESS }

/**
 * Final part of the participant flow: Team Details form (3/3) → Submitting → Success.
 * Call it once the participant has created a team, been accepted/joined, or chosen solo.
 * If the event has no shared questions it submits immediately.
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
    registrationId: Int,
    eventName: String,
    mode: ParticipationMode,
    onExit: () -> Unit,
    onViewRegistration: (SubmissionReceipt) -> Unit,
    onViewTeamPortal: (() -> Unit)? = null,
    onBackToEvent: () -> Unit,
    teamId: Int? = null,
    participantAnswers: Map<Int, String> = emptyMap(),
    viewModel: SubmissionViewModel = viewModel(
        factory = RegistrationDependencies.submissionViewModelFactory,
    ),
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(eventId, registrationId, eventName, mode, teamId, participantAnswers) {
        viewModel.start(SubmissionContext(eventId, registrationId, eventName, mode, teamId, participantAnswers))
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
            SubmitScreenKey.LOADING -> FlowLoadingScreen(
                step = 3,
                onBack = onExit,
                loadingLabel = "Loading shared questions",
            )

            SubmitScreenKey.LOAD_FAILED -> FlowLoadingScreen(
                step = 3,
                onBack = onExit,
                error = (phase as? SubmitPhase.LoadFailed)?.message ?: "Something went wrong.",
                errorTitle = "Couldn't load your registration",
                onRetry = viewModel::retryLoad,
            )

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
                        onViewTeamPortal = if (mode == ParticipationMode.CREATE_TEAM) onViewTeamPortal else null,
                        onBackToEvent = onBackToEvent,
                    )
                }
            }
        }
    }
}
