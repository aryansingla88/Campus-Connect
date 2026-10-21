package com.example.campusconnect.feature.registrations.userflow.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.campusconnect.feature.registrations.userflow.model.RegistrationQuestionScreenConfig
import com.example.campusconnect.feature.registrations.userflow.model.RegistrationQuestionScreenKind
import com.example.campusconnect.feature.registrations.userflow.viewmodel.RegistrationSubmitViewModel
import com.example.campusconnect.feature.registrations.userflow.viewmodel.SubmitPhase
import com.example.campusconnect.feature.registrations.userflow.viewmodel.UserRegistrationViewModel

private enum class SubmitScreenKey { LOADING, LOAD_FAILED, FORM, SUBMITTING, SUCCESS }

/**
 * Final part of the participant flow: shared team-question form (3/3) → Submitting → Success.
 * Call it once the participant has created a team, been accepted/joined, or chosen solo.
 * Submission always requires an explicit action, including when there are no team questions.
 *
 * ```
 * RegistrationSubmitFlow(
 *     eventId = id, eventName = "TechNova 2025", mode = ParticipationMode.CREATE_TEAM,
 *     participantAnswers = answers,
 *     onExit = { nav.popBackStack() },
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
    onBackToEvent: () -> Unit,
    onViewRegistration: (registrationId: Int) -> Unit,
    participantId: Int? = null,
    teamId: Int? = null,
    teamName: String = "",
    participantAnswers: Map<Int, String> = emptyMap(),
    initialTeamAnswers: Map<Int, String> = emptyMap(),
    onTeamAnswersChange: (Map<Int, String>) -> Unit = {},
    viewModel: RegistrationSubmitViewModel = viewModel(),
) {
    val state by viewModel.state.collectAsState()
    var showTeamPortal by remember { mutableStateOf(false) }
    val userFlowViewModel: UserRegistrationViewModel = viewModel()
    val portalState by userFlowViewModel.state.collectAsState()

    LaunchedEffect(eventId, eventName, mode, teamId, participantId, participantAnswers, teamName) {
        viewModel.start(
            SubmissionContext(
                eventId = eventId,
                eventName = eventName,
                mode = mode,
                participantId = participantId,
                teamId = teamId,
                participantAnswers = participantAnswers,
                teamName = teamName.takeIf { mode == ParticipationMode.CREATE_TEAM },
                initialTeamAnswers = initialTeamAnswers,
            ),
        )
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
        if (showTeamPortal) {
            showTeamPortal = false
            return@BackHandler
        }
        when (phase) {
            SubmitPhase.Submitting -> Unit // don't abandon a submission in flight
            is SubmitPhase.Success -> onBackToEvent()
            else -> onExit()
        }
    }

    if (showTeamPortal) {
        LaunchedEffect(eventId, participantId) { userFlowViewModel.startPortal(eventId, participantId) }
        TeamPortalScreen(
            portal = portalState.portal,
            query = portalState.query,
            onQueryChange = userFlowViewModel::onQueryChange,
            tagFilter = portalState.tagFilter,
            onTagFilterChange = userFlowViewModel::onTagFilterChange,
            requestedTeamIds = portalState.requestedTeamIds,
            pendingTeamIds = portalState.pendingTeamIds,
            respondedInviteIds = portalState.respondedInviteIds,
            pendingInviteIds = portalState.pendingInviteIds,
            pendingJoinRequestIds = portalState.pendingJoinRequestIds,
            pendingSentInviteIds = portalState.pendingSentInviteIds,
            isSendingInvite = portalState.isSendingInvite,
            onRequestToJoin = { team -> userFlowViewModel.requestToJoin(team) },
            onInviteResponse = { invite, accept -> userFlowViewModel.respondToInvite(invite, accept) },
            onRespondToJoinRequest = userFlowViewModel::respondToJoinRequest,
            onSendInvite = userFlowViewModel::inviteMember,
            onCancelInvitation = userFlowViewModel::cancelInvitation,
            onCancelJoinRequest = userFlowViewModel::cancelJoinRequest,
            onContinueWithTeam = null,
            onRetry = userFlowViewModel::loadTeams,
            transientError = portalState.transientError,
            onErrorShown = userFlowViewModel::consumeError,
            onBack = { showTeamPortal = false },
            initialTab = "MY_TEAM",
            onDone = { showTeamPortal = false },
        )
    } else Crossfade(targetState = key, label = "submitStep") { screen ->
        when (screen) {
            SubmitScreenKey.LOADING -> SubmittingRegistrationScreen(
                title = "Loading registration questions...",
                message = "Preparing the questions for this step.",
                showSendIcon = false,
            )

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

            SubmitScreenKey.FORM -> RegistrationQuestionScreen(
                fields = state.questions,
                answers = state.answers,
                validationErrors = state.validationErrors,
                transientError = state.transientError,
                config = state.questionScreenConfig,
                onAnswerChange = { questionId, answer ->
                    viewModel.onAnswerChange(questionId, answer)
                    onTeamAnswersChange(viewModel.state.value.answers)
                },
                onErrorShown = viewModel::consumeError,
                onAction = viewModel::submit,
                onBack = onExit,
            )

            SubmitScreenKey.SUBMITTING -> SubmittingRegistrationScreen()

            SubmitScreenKey.SUCCESS -> {
                val receipt = (phase as? SubmitPhase.Success)?.receipt
                if (receipt != null) {
                    RegistrationSuccessScreen(
                        eventName = eventName,
                        receipt = receipt,
                        onViewRegistration = { onViewRegistration(receipt.registrationId) },
                        onBackToEvent = onBackToEvent,
                        teamPortalActionLabel = if (mode == ParticipationMode.CREATE_TEAM) "Go to Team Portal" else null,
                        onTeamPortal = {
                            userFlowViewModel.loadTeams()
                            showTeamPortal = true
                        },
                    )
                }
            }
        }
    }
}
