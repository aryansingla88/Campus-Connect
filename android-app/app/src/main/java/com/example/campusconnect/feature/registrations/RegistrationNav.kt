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
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import com.example.campusconnect.feature.registrations.data.repo.RegistrationConfigRepository
import com.example.campusconnect.feature.registrations.ui.screens.RegistrationResponseDetailScreen
import com.example.campusconnect.feature.registrations.viewmodel.RegistrationFormViewModel
import com.example.campusconnect.feature.registrations.viewmodel.RegistrationResponsesViewModel
import com.example.campusconnect.feature.registrations.viewmodel.RegistrationConfigViewModel
import com.example.campusconnect.feature.registrations.viewmodel.RegistrationEventViewModel
import com.example.campusconnect.feature.registrations.userflow.data.ParticipantRegistrationRepositories
import com.example.campusconnect.feature.registrations.ui.screens.FormBuilderScreen
import com.example.campusconnect.feature.registrations.ui.screens.RegistrationSetupScreen
import com.example.campusconnect.feature.registrations.ui.screens.RegistrationResponsesScreen
import com.example.campusconnect.feature.registrations.ui.screens.TeamQuestionsScreen
import com.example.campusconnect.feature.registrations.userflow.model.ParticipationMode
import com.example.campusconnect.feature.registrations.userflow.ui.UserRegistrationFlow
import com.example.campusconnect.feature.registrations.userflow.ui.RegistrationSubmitFlow

fun NavGraphBuilder.RegistrationNav(
    navController: NavController,
) {

    // TestScreen's participant entry point. The wrapper keeps participant answers and
    // selected team/mode while switching between the existing flow composables.
    composable(
        route = "user_registration/{eventId}?personaId={personaId}",
        arguments = listOf(
            navArgument("eventId") { type = NavType.IntType },
            navArgument("personaId") { type = NavType.IntType; defaultValue = -1 },
        ),
    ) { backStackEntry ->
        val eventId = backStackEntry.arguments?.getInt("eventId") ?: return@composable
        ParticipantRegistrationHost(
            eventId = eventId,
            participantId = backStackEntry.arguments?.getInt("personaId")?.takeIf { it > 0 },
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
        LaunchedEffect(eventId, configState.settings.allowIndividual) {
            formViewModel.setRegistrationMode(configState.settings.allowIndividual)
            formViewModel.load(eventId)
        }
        val saveAll = { configViewModel.save { saved -> if (saved) formViewModel.save(eventId) } }

        TeamQuestionsScreen(
            eventName = eventState.title ?: "Event #$eventId",
            isPublished = eventState.isPublished,
            fields = teamFields,
            onFieldsChange = formViewModel::setTeamFields,
            movableQuestions = individualFields,
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
        val eventId = backStackEntry.arguments?.getInt("eventId") ?: return@composable

        // Host-originated details share the preceding list's cache. Participant-originated
        // details get a local VM and are backed by that persona's fake submitted record.
        val listEntry = remember(backStackEntry) {
            navController.previousBackStackEntry?.takeIf {
                it.destination.route == "registration_responses/{eventId}"
            }
        }
        val owner = listEntry ?: backStackEntry
        val viewModel: RegistrationResponsesViewModel = viewModel(owner)
        val listState by viewModel.listState.collectAsState()
        val openedFromParticipant = remember(backStackEntry) {
            navController.previousBackStackEntry?.destination?.route?.startsWith("user_registration") == true
        }
        var participantDetails by remember(backStackEntry) { mutableStateOf<Pair<com.example.campusconnect.feature.registrations.model.RegistrationResponseItem, com.example.campusconnect.feature.registrations.model.RegistrationDetailResponse>?>(null) }
        var participantDetailError by remember(backStackEntry) { mutableStateOf<String?>(null) }
        LaunchedEffect(openedFromParticipant, eventId, registrationId) {
            if (openedFromParticipant) {
                ParticipantRegistrationRepositories.user.getOwnedDetails(eventId, registrationId).fold(
                    onSuccess = { participantDetails = it },
                    onFailure = { participantDetailError = it.message ?: "Could not load your registration." },
                )
            }
        }
        val response = listState.registrations.firstOrNull { it.id == registrationId }
            ?: participantDetails?.first

        RegistrationResponseDetailScreen(
            eventId = eventId,
            registrationId = registrationId,
            response = response,
            viewModel = viewModel,
            onBack = { navController.popBackStack() },
            initialDetail = participantDetails?.second,
            initialLoadError = participantDetailError,
            initialLoading = openedFromParticipant && participantDetails == null && participantDetailError == null,
            loadHostDetail = !openedFromParticipant,
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
        LaunchedEffect(eventId, configState.settings.allowIndividual) {
            formViewModel.setRegistrationMode(configState.settings.allowIndividual)
            formViewModel.load(eventId)
        }
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
            fields = if (configState.settings.allowIndividual) fields else teamFields,
            onFieldsChange = { changed ->
                if (configState.settings.allowIndividual) formViewModel.setIndividualFields(changed)
                else formViewModel.setTeamFields(changed)
            },

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
    participantId: Int?,
    navController: NavController,
) {
    var stage by remember { mutableStateOf(ParticipantRegistrationStage.DETAILS) }
    var mode by remember { mutableStateOf<ParticipationMode?>(null) }
    var participantAnswers by remember { mutableStateOf<Map<Int, String>>(emptyMap()) }
    var sharedAnswers by remember { mutableStateOf<Map<Int, String>>(emptyMap()) }
    var teamId by remember { mutableStateOf<Int?>(null) }
    var teamName by remember { mutableStateOf("") }

    val leaveFlow: () -> Unit = { navController.popBackStack() }
    val continueToSubmission = { selectedMode: ParticipationMode, answers: Map<Int, String>, selectedTeamId: Int? ->
        mode = selectedMode
        participantAnswers = answers.toMap()
        teamId = selectedTeamId
        stage = ParticipantRegistrationStage.SUBMISSION
    }

    when (stage) {
        ParticipantRegistrationStage.DETAILS -> UserRegistrationFlow(
            eventId = eventId,
            participantId = participantId,
            teamName = teamName,
            onTeamNameChange = { teamName = it },
            onExit = leaveFlow,
            onCreateTeam = { answers, selectedTeamName ->
                teamName = selectedTeamName
                continueToSubmission(ParticipationMode.CREATE_TEAM, answers, null)
            },
            onContinueSolo = { answers ->
                continueToSubmission(ParticipationMode.SOLO, answers, null)
            },
            onJoinRequested = { team, answers ->
                // A request remains pending in the portal until a leader approves it.
                mode = ParticipationMode.JOIN_TEAM
                participantAnswers = answers.toMap()
                teamId = team.id
            },
            onInviteAccepted = { invite, answers ->
                continueToSubmission(ParticipationMode.JOIN_TEAM, answers, invite.teamId)
            },
            onContinueAsMember = { team, answers ->
                continueToSubmission(ParticipationMode.JOIN_TEAM, answers, team.id)
            },
        )

        ParticipantRegistrationStage.SUBMISSION -> RegistrationSubmitFlow(
            eventId = eventId,
            eventName = "Event #$eventId",
            mode = mode ?: ParticipationMode.SOLO,
            participantId = participantId,
            teamId = teamId,
            teamName = teamName,
            participantAnswers = participantAnswers,
            initialTeamAnswers = sharedAnswers,
            onTeamAnswersChange = { sharedAnswers = it.toMap() },
            onExit = { stage = ParticipantRegistrationStage.DETAILS },
            onBackToEvent = { leaveFlow() },
            onViewRegistration = { registrationId ->
                navController.navigateToRegistrationResponseDetail(eventId, registrationId)
            },
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

fun NavController.navigateToParticipantRegistration(eventId: Int, personaId: Int? = null) {
    val route = "user_registration/$eventId" + (personaId?.let { "?personaId=$it" } ?: "")
    navigate(route)
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
