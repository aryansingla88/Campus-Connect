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
import com.example.campusconnect.feature.registrations.userflow.viewmodel.DetailsResult
import com.example.campusconnect.feature.registrations.userflow.viewmodel.UserRegistrationViewModel

/** Entry point for creating a participant registration and choosing how to participate. */
@Composable
fun UserRegistrationFlow(
    eventId: Int,
    onExit: () -> Unit,
    onCreateTeam: (answers: Map<Int, String>) -> Unit = {},
    onContinueSolo: (answers: Map<Int, String>) -> Unit = {},
    onJoinRequested: (TeamSummary, answers: Map<Int, String>) -> Unit = { _, _ -> },
    onInviteAccepted: (TeamInvite, answers: Map<Int, String>) -> Unit = { _, _ -> },
    viewModel: UserRegistrationViewModel = viewModel(),
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(eventId) { viewModel.start(eventId) }

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
                onAnswerChange = viewModel::onAnswerChange,
                onNext = {
                    if (viewModel.submitDetails() == DetailsResult.DONE_SOLO) {
                        onContinueSolo(viewModel.state.value.answers)
                    }
                },
                onBack = onExit,
            )

            step == UserRegistrationStep.PARTICIPATION -> ParticipationChoiceScreen(
                allowSolo = draft.allowSolo,
                selected = state.mode,
                onSelect = viewModel::selectMode,
                onNext = {
                    when (state.mode) {
                        ParticipationMode.CREATE_TEAM -> onCreateTeam(state.answers)
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
                onRetry = viewModel::loadTeams,
                transientError = state.transientError,
                onErrorShown = viewModel::consumeError,
                onBack = { viewModel.back() },
            )
        }
    }
}
