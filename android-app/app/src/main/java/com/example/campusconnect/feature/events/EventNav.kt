package com.example.campusconnect.feature.events

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.example.campusconnect.feature.events.model.EventStatus
import com.example.campusconnect.feature.events.ui.screen.EventScreen
import com.example.campusconnect.feature.events.ui.screen.EventViewModeScreen
import com.example.campusconnect.feature.events.viewmodel.EventViewModel

const val EVENTS_POSTER_VIEW = "events_poster_view"

fun NavGraphBuilder.eventNav(
    navController: NavController
) {
    navigation(
        startDestination = "events_main",
        route = "events_root"
    ) {

        composable("events_main") {

            EventScreen(
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        // ---------------------------------------------------------
        // Poster / View Mode
        // ---------------------------------------------------------

        composable(EVENTS_POSTER_VIEW) {

            val viewModel: EventViewModel = viewModel()

            val events by viewModel.displayedEvents.collectAsState()

            val activeEvents = events.filter {
                it.status != EventStatus.PAST
            }

            EventViewModeScreen(
                events = activeEvents,
                onBack = {
                    navController.popBackStack()
                },
                onRegister = {},
                onNotify = {},
                onOpenDetail = {}
            )
        }
    }
}