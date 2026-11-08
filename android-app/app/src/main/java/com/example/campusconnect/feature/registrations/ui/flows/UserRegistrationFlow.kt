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
import com.example.campusconnect.feature.registrations.model.ParticipationMode
import com.example.campusconnect.feature.registrations.model.TeamInvite
import com.example.campusconnect.feature.registrations.model.TeamSummary
import com.example.campusconnect.feature.registrations.model.UserRegistrationStep
import com.example.campusconnect.feature.registrations.viewmodel.CreateState
import com.example.campusconnect.feature.registrations.viewmodel.UserRegistrationViewModel

/** Entry point for creating a participant registration and choosing how to participate. */
@Composable
fun UserRegistrationFlow(
    eventId: Int,
    onExit: () -> Unit,
    onCreateTeam: (registrationId: Int, teamId: Int, answers: Map<Int, String>) -> Unit = { _, _, _ -> },
    onContinueSolo: (registrationId: Int, answers: Map<Int, String>) -> Unit = { _, _ -> },
    onJoinRequested: (TeamSummary, registrationId: Int, answers: Map<Int, String>) -> Unit = { _, _, _ -> },
    onInviteAccepted: (TeamInvite, registrationId: Int, answers: Map<Int, String>) -> Unit = { _, _, _ -> },
    onTeamJoined: (TeamSummary, registrationId: Int, answers: Map<Int, String>) -> Unit = { _, _, _ -> },
    openTeamPortalOnStart: Boolean = false,
    viewModel: UserRegistrationViewModel = viewModel(),
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(eventId) { viewModel.start(eventId) }
    LaunchedEffect(eventId, openTeamPortalOnStart) {
        if (openTeamPortalOnStart) viewModel.openTeamPortal()
    }

    BackHandler { if (!viewModel.back()) onExit() }

    val draft = (state.create as? CreateState.Ready)?.draft
    Crossfade(targetState = state.step, label = "userRegistrationStep") { step ->
        when {
            step == UserRegistrationStep.CREATING || draft == null -> CreatingRegistrationScreen(
                error = (state.create as? CreateState.Failed)?.message,
                onBack = onExit,
                onRetry = viewModel::retryCreate,
            )

            step == UserRegistrationStep.DETAILS -> IndividualDetailsScreen(
                questions = draft.questions,
                answers = state.answers,
                showErrors = state.showErrors,
                isSaving = state.savingDetails,
                transientError = state.transientError,
                onErrorShown = viewModel::consumeError,
                onAnswerChange = viewModel::onAnswerChange,
                onNext = { viewModel.submitDetails { onContinueSolo(draft.registrationId, viewModel.state.value.answers) } },
                onBack = onExit,
            )

            step == UserRegistrationStep.PARTICIPATION -> ParticipationChoiceScreen(
                allowSolo = draft.allowSolo,
                selected = state.mode,
                teamName = state.teamName,
                teamNameAvailable = state.teamNameAvailable,
                checkingTeamName = state.checkingTeamName,
                creatingTeam = state.creatingTeam,
                transientError = state.transientError,
                onTeamNameChange = viewModel::onTeamNameChange,
                onCheckTeamName = { viewModel.checkTeamName() },
                onSelect = viewModel::selectMode,
                onNext = {
                    when (state.mode) {
                        ParticipationMode.CREATE_TEAM -> viewModel.createTeam { created ->
                            onCreateTeam(created.registrationId, created.teamId, state.answers)
                        }
                        ParticipationMode.SOLO -> if (draft.allowSolo) onContinueSolo(draft.registrationId, state.answers)
                        ParticipationMode.JOIN_TEAM -> viewModel.openTeamPortal()
                        null -> Unit
                    }
                },
                onBack = { viewModel.back() },
            )

            else -> TeamPortalScreen(
                portal = state.portal,
                query = state.query,
                onQueryChange = viewModel::onQueryChange,
                tagFilter = state.tagFilter,
                onTagFilterChange = viewModel::onTagFilterChange,
                requestedTeamIds = state.requestedTeamIds,
                pendingTeamIds = state.pendingTeamIds,
                respondedInviteIds = state.respondedInviteIds,
                pendingInviteIds = state.pendingInviteIds,
                onRequestToJoin = { team ->
                    viewModel.requestToJoin(team) { requested ->
                        onJoinRequested(requested, draft.registrationId, state.answers)
                    }
                },
                onInviteResponse = { invite, accept ->
                    viewModel.respondToInvite(invite, accept) { accepted ->
                        if (accept) onInviteAccepted(accepted, draft.registrationId, state.answers)
                    }
                },
                pendingJoinRequestIds = state.pendingJoinRequestIds,
                pendingInviteSendIds = state.pendingInviteSendIds,
                onJoinRequestResponse = { request, accept -> viewModel.respondToJoinRequest(request.requestId, accept) },
                onInviteCandidate = { candidate -> viewModel.inviteParticipant(candidate.registrationId) },
                onContinueWithTeam = { team -> onTeamJoined(team, draft.registrationId, state.answers) },
                onRetry = viewModel::loadTeams,
                transientError = state.transientError,
                onErrorShown = viewModel::consumeError,
                onBack = { viewModel.back() },
            )
        }
    }
}
