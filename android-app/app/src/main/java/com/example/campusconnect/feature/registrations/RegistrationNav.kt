package com.example.campusconnect.feature.registrations

import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.example.campusconnect.feature.registrations.data.FakeRegistrationResponses
import com.example.campusconnect.feature.registrations.data.FakeRegistrationService
import com.example.campusconnect.feature.registrations.ui.screens.RegistrationResponseDetailScreen
import com.example.campusconnect.feature.registrations.viewmodel.RegistrationFormViewModel
import com.example.campusconnect.feature.registrations.viewmodel.RegistrationResponsesViewModel
import com.example.campusconnect.feature.registrations.ui.screens.FormBuilderScreen
import com.example.campusconnect.feature.registrations.ui.screens.RegistrationSetupScreen
import com.example.campusconnect.feature.registrations.ui.screens.RegistrationResponsesScreen
import com.example.campusconnect.feature.registrations.ui.screens.TeamQuestionsScreen

fun NavGraphBuilder.RegistrationNav(
    navController: NavController,
    onQuickFields: (eventId: Int) -> Unit = {},
    onViewResponses: (eventId: Int) -> Unit = {},
) {

    composable(
        route = "registration_setup/{eventId}",
        arguments = listOf(
            navArgument("eventId") {
                type = NavType.IntType
            }
        ),
    ) { backStackEntry ->

        val eventId =
            backStackEntry.arguments?.getInt("eventId")
                ?: return@composable

        val registration = FakeRegistrationService.registration

        RegistrationSetupScreen(
            eventName = registration.eventName,
            isPublished = registration.isPublished,
            initialSettings = registration.settings,

            onBack = {
                navController.popBackStack()
            },

            onContinue = { _ ->
                navController.navigateToFormBuilder(eventId)
            },


            onTeamQuestions = {
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

        val registration = FakeRegistrationService.registration

        val formViewModel = rememberFormViewModel(navController, backStackEntry)
        val teamFields by formViewModel.teamFields.collectAsState()
        val individualFields by formViewModel.individualFields.collectAsState()

        TeamQuestionsScreen(
            eventName = registration.eventName,
            isPublished = registration.isPublished,
            fields = teamFields,
            onFieldsChange = formViewModel::setTeamFields,
            movableQuestions = individualFields,
            onMoveQuestions = formViewModel::moveToTeam,
            onPublish = { _ -> navController.popBackStack() },
            onBack = { navController.popBackStack() },
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

        val registration = FakeRegistrationService.registration

        RegistrationResponsesScreen(
            eventName = registration.eventName,
            isPublished = registration.isPublished,
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

        val response =
            FakeRegistrationResponses.responses.firstOrNull { it.id == registrationId }
                ?: return@composable

        // Share the list's ViewModel so loaded responses are reused, not refetched.
        val listEntry = remember(backStackEntry) {
            navController.getBackStackEntry("registration_responses/{eventId}")
        }
        val viewModel: RegistrationResponsesViewModel = viewModel(listEntry)

        RegistrationResponseDetailScreen(
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

        val registration = FakeRegistrationService.registration

        val formViewModel = rememberFormViewModel(navController, backStackEntry)
        val fields by formViewModel.individualFields.collectAsState()

        FormBuilderScreen(
            eventId = eventId,
            eventName = registration.eventName,
            isPublished = registration.isPublished,
            fields = fields,
            onFieldsChange = formViewModel::setIndividualFields,

            onPublish = { _ ->
                navController.popBackStack()
            },

            onBack = {
                navController.popBackStack()
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
