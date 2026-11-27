package com.example.campusconnect.feature.events

import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.navigation
import com.example.campusconnect.feature.events.model.EventStatus
import com.example.campusconnect.feature.events.model.Event
import com.example.campusconnect.feature.events.ui.screen.EventScreen
import com.example.campusconnect.feature.events.ui.screen.PosterModeScreen
import com.example.campusconnect.feature.events.viewmodel.EventViewModel
import com.example.campusconnect.feature.profile.navigateToUserProfile
import com.example.campusconnect.feature.map.ui.components.controls.LocalCloudTransitionState
import kotlinx.coroutines.launch

const val EVENTS_POSTER_VIEW = "events_poster_view"

fun NavGraphBuilder.eventNav(
    navController: NavController,
    onRegistrationSetup: (event: Event) -> Unit,
    onParticipantRegistration: (eventId: Int) -> Unit,
) {
    navigation(
        startDestination = "events_main",
        route = "events_root"
    ) {

        composable("events_main") { eventsMainEntry ->

            val cloudTransition = LocalCloudTransitionState.current
            val coroutineScope = rememberCoroutineScope()
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val isEventsScreenVisible = navBackStackEntry?.destination?.route == "events_main"
            val accessDialogEventId by eventsMainEntry.savedStateHandle
                .getStateFlow<Int?>("events_access_dialog_event_id", null)
                .collectAsState()
            val returnAccessDialogEventId by eventsMainEntry.savedStateHandle
                .getStateFlow<Int?>("events_return_access_dialog_event_id", null)
                .collectAsState()
            val posterModeOpen by eventsMainEntry.savedStateHandle
                .getStateFlow("events_manage_poster_mode_open", false)
                .collectAsState()

            EventScreen(
                onBack = {
                    if (!cloudTransition.isRunning) {
                        coroutineScope.launch {
                            cloudTransition.run {
                                navController.popBackStack()
                            }
                        }
                    }
                },
                onNavigateToProfile = { userId ->
                    eventsMainEntry.savedStateHandle["events_return_access_dialog_event_id"] = accessDialogEventId
                    eventsMainEntry.savedStateHandle["events_access_dialog_event_id"] = null
                    navController.navigateToUserProfile(userId)
                },
                onRegistration = { event ->
                    onRegistrationSetup(event)
                },
                restoreAccessDialogEventId = if (isEventsScreenVisible) {
                    returnAccessDialogEventId ?: accessDialogEventId
                } else null,
                onAccessDialogEventIdChange = { eventId ->
                    eventsMainEntry.savedStateHandle["events_access_dialog_event_id"] = eventId
                    if (eventId == null) {
                        eventsMainEntry.savedStateHandle["events_return_access_dialog_event_id"] = null
                    } else if (returnAccessDialogEventId == eventId) {
                        eventsMainEntry.savedStateHandle["events_return_access_dialog_event_id"] = null
                    }
                },
                posterModeOpen = posterModeOpen,
                onPosterModeOpenChange = { isOpen ->
                    eventsMainEntry.savedStateHandle["events_manage_poster_mode_open"] = isOpen
                },
                isScreenVisible = isEventsScreenVisible
            )
        }

        // ---------------------------------------------------------
        // Poster / View Mode
        // ---------------------------------------------------------

        composable(EVENTS_POSTER_VIEW) { posterEntry ->

            val viewModel: EventViewModel = viewModel()
            val events by viewModel.displayedEvents.collectAsState()
            val categories by viewModel.categories.collectAsState()
            val clubs by viewModel.clubs.collectAsState()
            val eventPreviews by viewModel.eventPreviews.collectAsState()

            val activeEvents = events.filter {
                it.status != EventStatus.PAST
            }

            val posterEventId by posterEntry.savedStateHandle
                .getStateFlow<Int?>("events_poster_selected_event_id", null)
                .collectAsState()

            PosterModeScreen(
                events = activeEvents,
                initialSelectedEventId = posterEventId,
                onSelectedEventIdChange = { id ->
                    posterEntry.savedStateHandle["events_poster_selected_event_id"] = id
                },
                onBack = {
                    navController.popBackStack()
                },
                onRegister = { event ->
                    onParticipantRegistration(event.id)
                },
                onNotify = {},
                onOpenDetail = {},
                categories = categories,
                clubs = clubs,
                eventPreviews = eventPreviews,
                onLoadEventPreview = viewModel::loadEventPreview,
                onHostClick = { userId ->
                    navController.navigateToUserProfile(userId)
                }
            )
        }
    }
}
