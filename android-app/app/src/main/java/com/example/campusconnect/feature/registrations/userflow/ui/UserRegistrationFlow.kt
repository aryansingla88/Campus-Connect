package com.example.campusconnect.feature.registrations.userflow.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.campusconnect.feature.registrations.userflow.model.ParticipationMode
import com.example.campusconnect.feature.registrations.userflow.model.TeamInvite
import com.example.campusconnect.feature.registrations.userflow.model.TeamSummary
import com.example.campusconnect.feature.registrations.userflow.model.UserRegistrationStep
import com.example.campusconnect.feature.registrations.userflow.viewmodel.CreateState
import com.example.campusconnect.feature.registrations.userflow.viewmodel.UserRegistrationViewModel

/** Entry point for creating a participant registration and choosing how to participate. */
@Composable
fun UserRegistrationFlow(
    eventId: Int,
    participantId: Int? = null,
    onExit: () -> Unit,
    onCreateTeam: (answers: Map<Int, String>, teamName: String) -> Unit,
    onContinueSolo: (answers: Map<Int, String>) -> Unit,
    onJoinRequested: (TeamSummary, answers: Map<Int, String>) -> Unit,
    onInviteAccepted: (TeamInvite, answers: Map<Int, String>) -> Unit,
    onContinueAsMember: (TeamSummary, answers: Map<Int, String>) -> Unit,
    teamName: String,
    onTeamNameChange: (String) -> Unit,
    viewModel: UserRegistrationViewModel = viewModel(),
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(eventId, participantId) { viewModel.start(eventId, participantId) }
    LaunchedEffect(state.create) {
        (state.create as? CreateState.Ready)?.draft?.teamName?.let(onTeamNameChange)
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

            step == UserRegistrationStep.DETAILS -> RegistrationQuestionScreen(
                fields = draft.questions,
                answers = state.answers,
                validationErrors = state.validationErrors,
                config = state.questionScreenConfig,
                onAnswerChange = viewModel::onAnswerChange,
                onAction = { viewModel.submitDetails() },
                onBack = onExit,
            )

            step == UserRegistrationStep.PARTICIPATION -> ParticipationChoiceScreen(
                allowTeam = draft.allowTeam,
                allowSolo = draft.allowSolo,
                selected = state.mode,
                onSelect = viewModel::selectMode,
                teamName = teamName,
                teamNamePromptOpen = state.teamNamePromptOpen,
                teamNameError = state.teamNameError,
                teamNameResultMessage = state.teamNameResultMessage,
                teamNameResultAvailable = state.teamNameResultAvailable,
                isCheckingTeamName = state.isCheckingTeamName,
                teamNameConfirmed = state.teamNameConfirmed,
                onTeamNameChange = {
                    onTeamNameChange(it)
                    viewModel.onTeamNameChanged()
                },
                onEditTeamName = {
                    viewModel.onTeamNameChanged()
                    viewModel.requestTeamName()
                },
                onValidateTeamName = { viewModel.validateTeamName(teamName) },
                onDismissTeamNameResult = viewModel::dismissTeamNameResult,
                onCancelTeamName = viewModel::cancelTeamNamePrompt,
                onNext = {
                    when (state.mode) {
                        ParticipationMode.CREATE_TEAM -> if (!state.teamNameConfirmed) viewModel.requestTeamName()
                            else onCreateTeam(state.answers, teamName)
                        ParticipationMode.SOLO -> onContinueSolo(state.answers)
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
                pendingJoinRequestIds = state.pendingJoinRequestIds,
                pendingSentInviteIds = state.pendingSentInviteIds,
                isSendingInvite = state.isSendingInvite,
                onRequestToJoin = { team ->
                    viewModel.requestToJoin(team) { requested ->
                        onJoinRequested(requested, state.answers)
                    }
                },
                onInviteResponse = { invite, accept ->
                    viewModel.respondToInvite(invite, accept) { accepted ->
                        if (accept) onInviteAccepted(accepted, state.answers)
                    }
                },
                onRespondToJoinRequest = viewModel::respondToJoinRequest,
                onSendInvite = viewModel::inviteMember,
                onCancelInvitation = viewModel::cancelInvitation,
                onCancelJoinRequest = viewModel::cancelJoinRequest,
                onContinueWithTeam = (state.portal as? com.example.campusconnect.feature.registrations.userflow.viewmodel.PortalState.Loaded)
                    ?.data?.myTeam?.takeIf { (state.portal as? com.example.campusconnect.feature.registrations.userflow.viewmodel.PortalState.Loaded)?.data?.myTeamRole == com.example.campusconnect.feature.registrations.userflow.model.TeamRole.MEMBER }
                    ?.let { team -> ({ onContinueAsMember(team, state.answers) }) },
                initialTab = "AVAILABLE",
                onRetry = viewModel::loadTeams,
                transientError = state.transientError,
                onErrorShown = viewModel::consumeError,
                onBack = { viewModel.back() },
            )
        }
    }
}
