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
import com.example.campusconnect.feature.registrations.model.RegistrationQuickFields
import com.example.campusconnect.feature.registrations.ui.screens.RegistrationResponseDetailScreen
import com.example.campusconnect.feature.registrations.viewmodel.RegistrationFormViewModel
import com.example.campusconnect.feature.registrations.viewmodel.RegistrationResponsesViewModel
import com.example.campusconnect.feature.registrations.viewmodel.RegistrationConfigViewModel
import com.example.campusconnect.feature.registrations.viewmodel.OrganizerRegistrationWorkflowViewModel
import com.example.campusconnect.feature.registrations.ui.screens.FormBuilderScreen
import com.example.campusconnect.feature.registrations.ui.screens.RegistrationSetupScreen
import com.example.campusconnect.feature.registrations.ui.screens.RegistrationResponsesScreen
import com.example.campusconnect.feature.registrations.ui.screens.TeamQuestionsScreen
import com.example.campusconnect.feature.registrations.model.ParticipationMode
import com.example.campusconnect.feature.registrations.model.UserRegistrationStep
import com.example.campusconnect.feature.registrations.model.QuestionScope
import com.example.campusconnect.feature.registrations.ui.flows.UserRegistrationFlow

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
        val registrationViewModel: com.example.campusconnect.feature.registrations.viewmodel.ParticipantRegistrationViewModel = viewModel(
            viewModelStoreOwner = backStackEntry,
            factory = RegistrationDependencies.userRegistrationViewModelFactory,
        )
        ParticipantRegistrationHost(
            eventId = eventId,
            navController = navController,
            viewModel = registrationViewModel,
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
        val eventName = navController.previousBackStackEntry
            ?.savedStateHandle?.get<String>("registration_event_name") ?: "Event #$eventId"
        val eventPublished = navController.previousBackStackEntry
            ?.savedStateHandle?.get<Boolean>("registration_event_published") ?: false
        backStackEntry.savedStateHandle.set("registration_event_name", eventName)
        backStackEntry.savedStateHandle.set("registration_event_published", eventPublished)
        val configViewModel: RegistrationConfigViewModel = viewModel(
            viewModelStoreOwner = backStackEntry,
            factory = RegistrationDependencies.registrationConfigViewModelFactory,
        )
        val formViewModel = rememberFormViewModel(navController, backStackEntry)
        val configState by configViewModel.state.collectAsState()
        val workflow: OrganizerRegistrationWorkflowViewModel = viewModel(
            viewModelStoreOwner = remember(backStackEntry) {
                runCatching { navController.getBackStackEntry("registration_setup/{eventId}") }.getOrDefault(backStackEntry)
            },
            factory = RegistrationDependencies.organizerRegistrationWorkflowViewModelFactory,
        )
        val workflowState by workflow.state.collectAsState()
        LaunchedEffect(eventId) { configViewModel.load(eventId) }

        RegistrationSetupScreen(
            eventName = eventName,
            isPublished = eventPublished,
            initialSettings = configState.settings,
            isLoading = configState.loading,
            isSaving = configState.saving || workflowState.saving,
            publishError = workflowState.error ?: configState.publishError ?: configState.error,
            loadFailed = configState.loadFailed,
            publishSucceeded = workflowState.saved && configState.publishSucceeded,
            onRetryLoad = configViewModel::retryLoad,
            onSettingsChange = configViewModel::updateSettings,

            onBack = {
                navController.popBackStack()
            },

            onContinue = { settings ->
                configViewModel.updateSettings(settings)
                navController.navigateToFormBuilder(eventId)
            },

            onPublish = { settings ->
                configViewModel.updateSettings(settings)
                workflow.publishAfterSave(configViewModel, formViewModel, eventId) { saved ->
                    if (saved) configViewModel.markPublishedSaveSucceeded() else configViewModel.markPublishedSaveFailed()
                }
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

        val header = registrationHeader(navController, backStackEntry, eventId)

        val formViewModel = rememberFormViewModel(navController, backStackEntry)
        val formsState by formViewModel.state.collectAsState()
        val teamFields by formViewModel.teamFields.collectAsState()
        val individualFields by formViewModel.individualFields.collectAsState()
        val configViewModel = rememberRegistrationConfigViewModel(navController, backStackEntry)
        val configState by configViewModel.state.collectAsState()
        val workflow: OrganizerRegistrationWorkflowViewModel = viewModel(
            viewModelStoreOwner = remember(backStackEntry) {
                runCatching { navController.getBackStackEntry("registration_setup/{eventId}") }.getOrDefault(backStackEntry)
            },
            factory = RegistrationDependencies.organizerRegistrationWorkflowViewModelFactory,
        )
        val workflowState by workflow.state.collectAsState()
        val formState by formViewModel.builderState.collectAsState()
        LaunchedEffect(eventId) { formViewModel.load(eventId) }
        TeamQuestionsScreen(
            eventName = header.eventName,
            isPublished = header.isPublished,
            fields = teamFields.filter { it.scope == QuestionScope.TEAM },
            onFieldsChange = formViewModel::setTeamFields,
            movableQuestions = individualFields.filter { it.scope == QuestionScope.INDIVIDUAL },
            onMoveQuestions = formViewModel::moveToTeam,
            onPublish = { _ -> workflow.saveFormChanges(configViewModel, formViewModel, eventId) },
            onBack = { navController.popBackStack() },
            isLoading = formsState.loading || configState.loading,
            isLoaded = formsState.loaded,
            isSaving = formsState.saving || configState.saving || workflowState.saving,
            saveError = workflowState.error ?: formState.error ?: configState.error ?: formsState.error,
            saveSucceeded = workflowState.saved,
            onRetry = {
                when {
                    configState.loadFailed -> configViewModel.retryLoad()
                    !formsState.loaded -> formViewModel.retry(eventId)
                    else -> workflow.saveFormChanges(configViewModel, formViewModel, eventId)
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

        val header = registrationHeader(navController, backStackEntry, eventId)
        val responsesViewModel: RegistrationResponsesViewModel = viewModel(
            viewModelStoreOwner = backStackEntry,
            factory = RegistrationDependencies.registrationResponsesViewModelFactory,
        )

        RegistrationResponsesScreen(
            eventId = eventId,
            eventName = header.eventName,
            isPublished = header.isPublished,
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
        val viewModel: RegistrationResponsesViewModel = viewModel(
            viewModelStoreOwner = listEntry,
            factory = RegistrationDependencies.registrationResponsesViewModelFactory,
        )
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

        val header = registrationHeader(navController, backStackEntry, eventId)

        val formViewModel = rememberFormViewModel(navController, backStackEntry)
        val formsState by formViewModel.state.collectAsState()
        val fields by formViewModel.individualFields.collectAsState()
        val configViewModel = rememberRegistrationConfigViewModel(navController, backStackEntry)
        val configState by configViewModel.state.collectAsState()
        LaunchedEffect(eventId) { formViewModel.load(eventId) }
        val workflow: OrganizerRegistrationWorkflowViewModel = viewModel(
            viewModelStoreOwner = remember(backStackEntry) {
                runCatching { navController.getBackStackEntry("registration_setup/{eventId}") }.getOrDefault(backStackEntry)
            },
            factory = RegistrationDependencies.organizerRegistrationWorkflowViewModelFactory,
        )
        val workflowState by workflow.state.collectAsState()
        val formState by formViewModel.builderState.collectAsState()
        FormBuilderScreen(
            eventId = eventId,
            eventName = header.eventName,
            isPublished = header.isPublished,
            fields = fields.filter { it.scope == QuestionScope.INDIVIDUAL },
            onFieldsChange = formViewModel::setIndividualFields,

            onPublish = { _ -> workflow.saveFormChanges(configViewModel, formViewModel, eventId) },

            isLoading = formsState.loading || configState.loading,
            isLoaded = formsState.loaded,
            isSaving = formsState.saving || configState.saving || workflowState.saving,
            saveError = workflowState.error ?: formState.error ?: configState.error ?: formsState.error,
            saveSucceeded = workflowState.saved,
            initialQuickFieldIds = RegistrationQuickFields.ids(configState.settings.quickFieldMask),
            onQuickFieldsChanged = { ids -> configViewModel.updateQuickFields(RegistrationQuickFields.mask(ids)) },
            onRetry = {
                when {
                    configState.loadFailed -> configViewModel.retryLoad()
                    !formsState.loaded -> formViewModel.retry(eventId)
                    else -> workflow.saveFormChanges(configViewModel, formViewModel, eventId)
                }
            },

            onBack = {
                navController.popBackStack()
            },
        )
    }
}

@Composable
private fun ParticipantRegistrationHost(
    eventId: Int,
    navController: NavController,
    viewModel: com.example.campusconnect.feature.registrations.viewmodel.ParticipantRegistrationViewModel,
) {
    val leaveFlow: () -> Unit = { navController.popBackStack() }
    UserRegistrationFlow(
        eventId = eventId,
        onExit = leaveFlow,
        viewModel = viewModel,
        onViewRegistration = { viewModel.resumeSubmission(); leaveFlow() },
        onBackToEvent = leaveFlow,
    )
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
    return viewModel(
        viewModelStoreOwner = owner,
        factory = RegistrationDependencies.registrationFormViewModelFactory,
    )
}

private data class RegistrationHeader(val eventName: String, val isPublished: Boolean)

private fun registrationHeader(
    navController: NavController,
    entry: NavBackStackEntry,
    eventId: Int,
): RegistrationHeader {
    val owner = runCatching { navController.getBackStackEntry("registration_setup/{eventId}") }
        .getOrDefault(entry)
    return RegistrationHeader(
        eventName = owner.savedStateHandle.get<String>("registration_event_name") ?: "Event #$eventId",
        isPublished = owner.savedStateHandle.get<Boolean>("registration_event_published") ?: false,
    )
}

@Composable
private fun rememberRegistrationConfigViewModel(
    navController: NavController,
    entry: NavBackStackEntry,
): RegistrationConfigViewModel {
    val owner = remember(entry) {
        runCatching { navController.getBackStackEntry("registration_setup/{eventId}") }.getOrDefault(entry)
    }
    return viewModel(
        viewModelStoreOwner = owner,
        factory = RegistrationDependencies.registrationConfigViewModelFactory,
    )
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
