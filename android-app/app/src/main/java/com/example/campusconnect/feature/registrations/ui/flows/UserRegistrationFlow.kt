package com.example.campusconnect.feature.registrations.ui.flows

import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.campusconnect.feature.registrations.RegistrationDependencies
import com.example.campusconnect.feature.registrations.model.ParticipationMode
import com.example.campusconnect.feature.registrations.model.TeamInvite
import com.example.campusconnect.feature.registrations.model.TeamSummary
import com.example.campusconnect.feature.registrations.model.UserRegistrationStep
import com.example.campusconnect.feature.registrations.model.SubmissionContext
import com.example.campusconnect.feature.registrations.ui.mapper.toParticipantQuestionUiModel
import com.example.campusconnect.feature.registrations.ui.screens.*
import com.example.campusconnect.feature.registrations.ui.components.FlowLoadingScreen
import com.example.campusconnect.feature.registrations.ui.components.SubmittingRegistrationScreen
import com.example.campusconnect.feature.registrations.viewmodel.CreateState
import com.example.campusconnect.feature.registrations.viewmodel.ParticipantRegistrationViewModel
import com.example.campusconnect.feature.registrations.viewmodel.TeamPortalViewModel
import com.example.campusconnect.feature.registrations.viewmodel.SubmissionViewModel
import com.example.campusconnect.feature.registrations.viewmodel.SubmitPhase
import com.example.campusconnect.feature.registrations.model.SubmissionReceipt

/** Registration screens coordinated by the participant-session owner and the team-portal owner. */
@Composable
fun UserRegistrationFlow(
    eventId: Int,
    onExit: () -> Unit,
    onCreateTeam: (registrationId: Int, teamId: Int, answers: Map<Int, String>) -> Unit = { _, _, _ -> },
    onContinueSolo: (registrationId: Int, answers: Map<Int, String>) -> Unit = { _, _ -> },
    onJoinRequested: (TeamSummary, registrationId: Int, answers: Map<Int, String>) -> Unit = { _, _, _ -> },
    onInviteAccepted: (TeamInvite, registrationId: Int, answers: Map<Int, String>) -> Unit = { _, _, _ -> },
    onTeamJoined: (TeamSummary, registrationId: Int, answers: Map<Int, String>) -> Unit = { _, _, _ -> },
    onViewRegistration: (SubmissionReceipt) -> Unit = {},
    onBackToEvent: () -> Unit = onExit,
    viewModel: ParticipantRegistrationViewModel = viewModel(factory = RegistrationDependencies.userRegistrationViewModelFactory),
    teamViewModel: TeamPortalViewModel = viewModel(factory = RegistrationDependencies.teamPortalViewModelFactory),
    submissionViewModel: SubmissionViewModel = viewModel(factory = RegistrationDependencies.submissionViewModelFactory),
) {
    val state by viewModel.state.collectAsState()
    val teamState by teamViewModel.state.collectAsState()
    val submissionState by submissionViewModel.state.collectAsState()
    val session = state.session

    LaunchedEffect(eventId) { viewModel.start(eventId) }
    LaunchedEffect(state.session) {
        state.session?.let(teamViewModel::syncSession)
    }
    LaunchedEffect(state.step, state.session) {
        val portalSession = state.session?.takeIf { state.step == UserRegistrationStep.TEAM_PORTAL } ?: return@LaunchedEffect
        teamViewModel.open(portalSession)
    }
    LaunchedEffect(state.session, state.answers) {
        val submissionSession = state.session?.takeIf { it.progress == UserRegistrationStep.SUBMISSION } ?: return@LaunchedEffect
        val context = SubmissionContext(
            eventId = submissionSession.eventId,
            registrationId = submissionSession.registrationId,
            eventName = "Event #${submissionSession.eventId}",
            mode = submissionSession.mode,
            teamId = submissionSession.teamId,
            participantAnswers = state.answers,
        )
        submissionViewModel.start(context)
    }

    BackHandler { if (!viewModel.back()) onExit() }

    val draft = (state.create as? CreateState.Ready)?.draft
    Crossfade(targetState = if (state.step == UserRegistrationStep.SUBMISSION) UserRegistrationStep.SUBMISSION else state.step, label = "userRegistrationStep") { step ->
        when {
            step == UserRegistrationStep.CREATING || draft == null -> CreatingRegistrationScreen(
                error = (state.create as? CreateState.Failed)?.message,
                onBack = onExit,
                onRetry = viewModel::retryCreate,
            )

            step == UserRegistrationStep.DETAILS -> IndividualDetailsScreen(
                questions = draft.questions.mapNotNull { it.toParticipantQuestionUiModel() },
                answers = state.answers,
                showErrors = state.showErrors,
                isSaving = state.savingDetails,
                transientError = state.transientError,
                onErrorShown = viewModel::consumeError,
                onAnswerChange = viewModel::onAnswerChange,
                onNext = {
                    viewModel.submitDetails {
                        if (!draft.allowTeam && draft.allowSolo) viewModel.soloDetailsSaved()
                    }
                },
                onBack = onExit,
            )

            step == UserRegistrationStep.PARTICIPATION -> ParticipationChoiceScreen(
                allowSolo = draft.allowSolo,
                selected = state.mode,
                teamName = teamState.teamName,
                teamNameAvailable = teamState.teamNameAvailable,
                checkingTeamName = teamState.checkingTeamName,
                creatingTeam = teamState.creatingTeam,
                transientError = teamState.transientError ?: state.transientError,
                onTeamNameChange = teamViewModel::onTeamNameChange,
                onCheckTeamName = { teamViewModel.checkTeamName() },
                onSelect = viewModel::selectMode,
                onNext = {
                    when (state.mode) {
                        ParticipationMode.CREATE_TEAM -> teamViewModel.createTeam { created ->
                            viewModel.attachTeam(created.teamId, ParticipationMode.CREATE_TEAM)
                            onCreateTeam(created.registrationId, created.teamId, state.answers)
                        }
                        ParticipationMode.SOLO -> if (draft.allowSolo) {
                            viewModel.readyForSubmission()
                        }
                        ParticipationMode.JOIN_TEAM -> {
                            viewModel.setProgress(UserRegistrationStep.TEAM_PORTAL)
                            viewModel.state.value.session?.let(teamViewModel::open)
                        }
                        null -> Unit
                    }
                },
                onBack = { viewModel.back() },
            )

            step == UserRegistrationStep.SUBMISSION -> {
                val phase = submissionState.phase
                when (phase) {
                    SubmitPhase.Loading -> FlowLoadingScreen(step = 3, onBack = { viewModel.setProgress(if (session?.mode == ParticipationMode.SOLO) UserRegistrationStep.DETAILS else UserRegistrationStep.PARTICIPATION) }, loadingLabel = "Loading shared questions")
                    is SubmitPhase.LoadFailed -> FlowLoadingScreen(step = 3, onBack = { viewModel.setProgress(if (session?.mode == ParticipationMode.SOLO) UserRegistrationStep.DETAILS else UserRegistrationStep.PARTICIPATION) }, error = phase.message, errorTitle = "Couldn't load your registration", onRetry = submissionViewModel::retryLoad)
                    SubmitPhase.Form -> TeamDetailsFormScreen(
                        questions = submissionState.questions, answers = submissionState.answers, files = submissionState.files,
                        fileErrors = submissionState.fileErrors, showErrors = submissionState.showErrors,
                        transientError = submissionState.transientError, onAnswerChange = submissionViewModel::onAnswerChange,
                        onFilePicked = submissionViewModel::onFilePicked, onFileRemoved = submissionViewModel::onFileRemoved,
                        onErrorShown = submissionViewModel::consumeError, onSubmit = submissionViewModel::submit,
                        onBack = { viewModel.setProgress(if (session?.mode == ParticipationMode.SOLO) UserRegistrationStep.DETAILS else UserRegistrationStep.PARTICIPATION) },
                    )
                    SubmitPhase.Submitting -> SubmittingRegistrationScreen()
                    is SubmitPhase.Success -> RegistrationSuccessScreen(
                        eventName = "Event #${session?.eventId ?: eventId}", receipt = phase.receipt,
                        onViewRegistration = { onViewRegistration(phase.receipt) },
                        onViewTeamPortal = if (session?.mode == ParticipationMode.CREATE_TEAM) ({
                            viewModel.openTeamPortalAfterSubmission()
                        }) else null,
                        onBackToEvent = onBackToEvent,
                    )
                }
            }

            else -> TeamPortalScreen(
                portal = teamState.portal,
                query = teamState.query,
                onQueryChange = teamViewModel::onQueryChange,
                tagFilter = teamState.tagFilter,
                onTagFilterChange = teamViewModel::onTagFilterChange,
                requestedTeamIds = teamState.requestedTeamIds,
                pendingTeamIds = teamState.pendingTeamIds,
                respondedInviteIds = teamState.respondedInviteIds,
                pendingInviteIds = teamState.pendingInviteIds,
                pendingJoinRequestIds = teamState.pendingJoinRequestIds,
                pendingInviteSendIds = teamState.pendingInviteSendIds,
                onRequestToJoin = { team -> teamViewModel.requestToJoin(team) { requested ->
                    val currentSession = viewModel.state.value.session ?: return@requestToJoin
                    onJoinRequested(requested, currentSession.registrationId, viewModel.state.value.answers)
                } },
                onInviteResponse = { invite, accept -> teamViewModel.respondToInvite(invite, accept) { accepted ->
                    val currentSession = viewModel.state.value.session ?: return@respondToInvite
                    viewModel.attachTeam(accepted.teamId, ParticipationMode.JOIN_TEAM)
                    onInviteAccepted(accepted, currentSession.registrationId, viewModel.state.value.answers)
                } },
                onJoinRequestResponse = { request, accept -> teamViewModel.respondToJoinRequest(request.requestId, accept) },
                onInviteCandidate = { candidate -> teamViewModel.inviteParticipant(candidate.registrationId) },
                onContinueWithTeam = { team ->
                    val currentSession = viewModel.state.value.session ?: return@TeamPortalScreen
                    viewModel.attachTeam(team.id, currentSession.mode)
                    onTeamJoined(team, currentSession.registrationId, viewModel.state.value.answers)
                },
                onRetry = teamViewModel::refresh,
                transientError = teamState.transientError,
                onErrorShown = teamViewModel::consumeError,
                onBack = { viewModel.back() },
            )
        }
    }
}
