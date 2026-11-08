package com.example.campusconnect.feature.registrations

import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.campusconnect.feature.registrations.data.RegistrationConfigRepository
import com.example.campusconnect.feature.registrations.ui.screens.RegistrationResponseDetailScreen
import com.example.campusconnect.feature.registrations.viewmodel.RegistrationFormViewModel
import com.example.campusconnect.feature.registrations.viewmodel.RegistrationResponsesViewModel
import com.example.campusconnect.feature.registrations.viewmodel.RegistrationConfigViewModel
import com.example.campusconnect.feature.registrations.viewmodel.RegistrationEventViewModel
import com.example.campusconnect.feature.registrations.ui.screens.FormBuilderScreen
import com.example.campusconnect.feature.registrations.ui.screens.RegistrationSetupScreen
import com.example.campusconnect.feature.registrations.ui.screens.RegistrationResponsesScreen
import com.example.campusconnect.feature.registrations.ui.screens.TeamQuestionsScreen
import com.example.campusconnect.feature.registrations.model.ParticipationMode
import com.example.campusconnect.feature.registrations.ui.flows.UserRegistrationFlow
import com.example.campusconnect.feature.registrations.ui.flows.RegistrationSubmitFlow

fun NavGraphBuilder.RegistrationNav(
    navController: NavController,
) {

    // Participant entry point from event previews. Keep registration state scoped to
    // the selected event while switching between the details and submission stages.
    composable(
        route = "user_registration/{eventId}",
        arguments = listOf(navArgument("eventId") { type = NavType.IntType }),
    ) { backStackEntry ->
        val eventId = backStackEntry.arguments?.getInt("eventId") ?: return@composable
        ParticipantRegistrationHost(
            eventId = eventId,
            navController = navController,
        )
    }

    composable(
        route = "registration_setup/{eventId}",
        arguments = listOf(
            navArgument("eventId") {
                type = NavType.IntType
            }
        ),
    ) { backStackEntry ->

        val eventId = backStackEntry.arguments?.getInt("eventId") ?: return@composable
        val eventViewModel: RegistrationEventViewModel = viewModel(backStackEntry)
        val eventState by eventViewModel.state.collectAsState()
        LaunchedEffect(eventId) { eventViewModel.load(eventId) }
        val configViewModel: RegistrationConfigViewModel = viewModel(backStackEntry)
        val configState by configViewModel.state.collectAsState()
        LaunchedEffect(eventId) { configViewModel.load(eventId) }

        RegistrationSetupScreen(
            eventName = eventState.title ?: "Event #$eventId",
            isPublished = eventState.isPublished,
            initialSettings = configState.settings,
            isLoading = configState.loading,
            isSaving = configState.saving,
            publishError = configState.error,
            loadFailed = configState.loadFailed,
            publishSucceeded = configState.saved,
            onRetryLoad = configViewModel::retryLoad,

            onBack = {
                navController.popBackStack()
            },

            onContinue = { settings ->
                configViewModel.updateSettings(settings)
                navController.navigateToFormBuilder(eventId)
            },

            onPublish = { settings ->
                configViewModel.updateSettings(settings)
                configViewModel.save()
            },


            onTeamQuestions = { settings ->
                configViewModel.updateSettings(settings)
                navController.navigateToTeamQuestions(eventId)
            },

            onViewResponses = {
                navController.navigateToRegistrationResponses(eventId)
            },
        )
    }


    composable(
        route = "team_questions/{eventId}",
        arguments = listOf(
            navArgument("eventId") {
                type = NavType.IntType
            }
        ),
    ) { backStackEntry ->

        val eventId =
            backStackEntry.arguments?.getInt("eventId")
                ?: return@composable

        val eventViewModel = rememberRegistrationEventViewModel(navController, backStackEntry)
        val eventState by eventViewModel.state.collectAsState()

        val formViewModel = rememberFormViewModel(navController, backStackEntry)
        val formsState by formViewModel.state.collectAsState()
        val teamFields by formViewModel.teamFields.collectAsState()
        val individualFields by formViewModel.individualFields.collectAsState()
        val configViewModel = rememberRegistrationConfigViewModel(navController, backStackEntry)
        val configState by configViewModel.state.collectAsState()
        LaunchedEffect(eventId) { formViewModel.load(eventId) }
        val saveAll = { configViewModel.save { saved -> if (saved) formViewModel.save(eventId) } }

        TeamQuestionsScreen(
            eventName = eventState.title ?: "Event #$eventId",
            isPublished = eventState.isPublished,
            fields = teamFields.filterNot { it.individual },
            onFieldsChange = formViewModel::setTeamFields,
            movableQuestions = individualFields.filter { it.individual },
            onMoveQuestions = formViewModel::moveToTeam,
            onPublish = { _ -> saveAll() },
            onBack = { navController.popBackStack() },
            isLoading = formsState.loading || configState.loading,
            isLoaded = formsState.loaded,
            isSaving = formsState.saving || configState.saving,
            saveError = configState.error ?: formsState.error,
            saveSucceeded = formsState.saved && configState.saved,
            onRetry = {
                when {
                    configState.loadFailed -> configViewModel.retryLoad()
                    !formsState.loaded -> formViewModel.retry(eventId)
                    else -> saveAll()
                }
            },
        )
    }

    composable(
        route = "registration_responses/{eventId}",
        arguments = listOf(
            navArgument("eventId") {
                type = NavType.IntType
            }
        ),
    ) { backStackEntry ->

        val eventId =
            backStackEntry.arguments?.getInt("eventId")
                ?: return@composable

        val eventViewModel = rememberRegistrationEventViewModel(navController, backStackEntry)
        val eventState by eventViewModel.state.collectAsState()
        val responsesViewModel: RegistrationResponsesViewModel = viewModel(backStackEntry)

        RegistrationResponsesScreen(
            eventId = eventId,
            eventName = eventState.title ?: "Event #$eventId",
            isPublished = eventState.isPublished,
            viewModel = responsesViewModel,
            onRefresh = { responsesViewModel.loadRegistrations(eventId, force = true) },
            onBack = { navController.popBackStack() },
            onFullResponse = { response ->
                navController.navigateToRegistrationResponseDetail(eventId, response.id)
            },
        )
    }

    composable(
        route = "registration_response_detail/{eventId}/{registrationId}",
        arguments = listOf(
            navArgument("eventId") { type = NavType.IntType },
            navArgument("registrationId") { type = NavType.IntType },
        ),
    ) { backStackEntry ->

        val registrationId =
            backStackEntry.arguments?.getInt("registrationId")
                ?: return@composable

        // Share the list's ViewModel so loaded responses are reused, not refetched.
        val listEntry = remember(backStackEntry) {
            navController.getBackStackEntry("registration_responses/{eventId}")
        }
        val viewModel: RegistrationResponsesViewModel = viewModel(listEntry)
        val listState by viewModel.listState.collectAsState()
        val response = listState.registrations.firstOrNull { it.id == registrationId }

        RegistrationResponseDetailScreen(
            eventId = backStackEntry.arguments?.getInt("eventId") ?: return@composable,
            registrationId = registrationId,
            response = response,
            viewModel = viewModel,
            onBack = { navController.popBackStack() },
        )
    }

    composable(
        route = "form_builder/{eventId}",
        arguments = listOf(
            navArgument("eventId") {
                type = NavType.IntType
            }
        ),
    ) { backStackEntry ->

        val eventId =
            backStackEntry.arguments?.getInt("eventId")
                ?: return@composable

        val eventViewModel = rememberRegistrationEventViewModel(navController, backStackEntry)
        val eventState by eventViewModel.state.collectAsState()

        val formViewModel = rememberFormViewModel(navController, backStackEntry)
        val formsState by formViewModel.state.collectAsState()
        val fields by formViewModel.individualFields.collectAsState()
        val teamFields by formViewModel.teamFields.collectAsState()
        val configViewModel = rememberRegistrationConfigViewModel(navController, backStackEntry)
        val configState by configViewModel.state.collectAsState()
        LaunchedEffect(eventId) { formViewModel.load(eventId) }
        val saveAll = {
            configViewModel.saveQuickFields {
                // Save question changes independently; a Quick Fields failure must not
                // prevent successful custom-question edits from being persisted.
                formViewModel.save(eventId)
            }
        }

        FormBuilderScreen(
            eventId = eventId,
            eventName = eventState.title ?: "Event #$eventId",
            isPublished = eventState.isPublished,
            fields = fields.filter { it.individual },
            onFieldsChange = formViewModel::setIndividualFields,

            onPublish = { _ -> saveAll() },

            isLoading = formsState.loading || configState.loading,
            isLoaded = formsState.loaded,
            isSaving = formsState.saving || configState.saving,
            saveError = configState.error ?: formsState.error,
            saveSucceeded = formsState.saved && configState.saved,
            initialQuickFieldIds = RegistrationConfigRepository.quickFieldIds(configState.settings.quickFieldMask),
            onQuickFieldsChanged = { ids ->
                configViewModel.updateSettings(configState.settings.copy(
                    quickFieldMask = RegistrationConfigRepository.quickFieldMask(ids),
                ))
            },
            onRetry = {
                when {
                    configState.loadFailed -> configViewModel.retryLoad()
                    !formsState.loaded -> formViewModel.retry(eventId)
                    else -> saveAll()
                }
            },

            onBack = {
                navController.popBackStack()
            },
        )
    }
}

private enum class ParticipantRegistrationStage { DETAILS, SUBMISSION }

@Composable
private fun ParticipantRegistrationHost(
    eventId: Int,
    navController: NavController,
) {
    var stage by remember { mutableStateOf(ParticipantRegistrationStage.DETAILS) }
    var mode by remember { mutableStateOf<ParticipationMode?>(null) }
    var participantAnswers by remember { mutableStateOf<Map<Int, String>>(emptyMap()) }
    var teamId by remember { mutableStateOf<Int?>(null) }
    var registrationId by remember { mutableStateOf<Int?>(null) }
    var openPortalAfterSubmission by remember { mutableStateOf(false) }

    val leaveFlow: () -> Unit = { navController.popBackStack() }
    val continueToSubmission = { selectedMode: ParticipationMode, selectedRegistrationId: Int,
                                 answers: Map<Int, String>, selectedTeamId: Int? ->
        mode = selectedMode
        registrationId = selectedRegistrationId
        participantAnswers = answers.toMap()
        teamId = selectedTeamId
        stage = ParticipantRegistrationStage.SUBMISSION
    }

    when (stage) {
        ParticipantRegistrationStage.DETAILS -> UserRegistrationFlow(
            eventId = eventId,
            openTeamPortalOnStart = openPortalAfterSubmission,
            onExit = leaveFlow,
            onCreateTeam = { regId, createdTeamId, answers ->
                continueToSubmission(ParticipationMode.CREATE_TEAM, regId, answers, createdTeamId)
            },
            onContinueSolo = { regId, answers ->
                continueToSubmission(ParticipationMode.SOLO, regId, answers, null)
            },
            onJoinRequested = { team, regId, answers ->
                // A request remains pending in the portal until a leader approves it.
                mode = ParticipationMode.JOIN_TEAM
                registrationId = regId
                participantAnswers = answers.toMap()
                teamId = team.id
            },
            onInviteAccepted = { invite, regId, answers ->
                continueToSubmission(ParticipationMode.JOIN_TEAM, regId, answers, invite.teamId)
            },
            onTeamJoined = { team, regId, answers ->
                continueToSubmission(ParticipationMode.JOIN_TEAM, regId, answers, team.id)
            },
        )

        ParticipantRegistrationStage.SUBMISSION -> RegistrationSubmitFlow(
            eventId = eventId,
            registrationId = registrationId ?: return,
            eventName = "Event #$eventId",
            mode = mode ?: ParticipationMode.SOLO,
            teamId = teamId,
            participantAnswers = participantAnswers,
            onExit = { stage = ParticipantRegistrationStage.DETAILS },
            // There is no participant registration-details destination in the current app.
            onViewRegistration = { leaveFlow() },
            onViewTeamPortal = {
                openPortalAfterSubmission = true
                stage = ParticipantRegistrationStage.DETAILS
            },
            onBackToEvent = { leaveFlow() },
        )
    }
}

/**
 * Both builders share one [RegistrationFormViewModel], scoped to the setup screen's
 * back-stack entry (falling back to the current entry if setup isn't on the stack).
 */
@Composable
private fun rememberFormViewModel(
    navController: NavController,
    entry: NavBackStackEntry,
): RegistrationFormViewModel {
    val owner = remember(entry) {
        runCatching { navController.getBackStackEntry("registration_setup/{eventId}") }.getOrDefault(entry)
    }
    return viewModel(owner)
}

@Composable
private fun rememberRegistrationEventViewModel(
    navController: NavController,
    entry: NavBackStackEntry,
): RegistrationEventViewModel {
    val owner = remember(entry) {
        runCatching { navController.getBackStackEntry("registration_setup/{eventId}") }.getOrDefault(entry)
    }
    return viewModel(owner)
}

@Composable
private fun rememberRegistrationConfigViewModel(
    navController: NavController,
    entry: NavBackStackEntry,
): RegistrationConfigViewModel {
    val owner = remember(entry) {
        runCatching { navController.getBackStackEntry("registration_setup/{eventId}") }.getOrDefault(entry)
    }
    return viewModel(owner)
}

fun NavController.navigateToRegistrationSetup(eventId: Int) {
    navigate("registration_setup/$eventId")
}

fun NavController.navigateToFormBuilder(eventId: Int) {
    navigate("form_builder/$eventId")
}

fun NavController.navigateToTeamQuestions(eventId: Int) {
    navigate("team_questions/$eventId")
}

fun NavController.navigateToRegistrationResponses(eventId: Int) {
    navigate("registration_responses/$eventId")
}

fun NavController.navigateToRegistrationResponseDetail(eventId: Int, registrationId: Int) {
    navigate("registration_response_detail/$eventId/$registrationId")
}
