package com.example.campusconnect.feature.registrations

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.campusconnect.feature.registrations.data.FakeFormDetails
import com.example.campusconnect.feature.registrations.data.FakeRegistrationService
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

            onQuickFields = {
                onQuickFields(eventId)
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

        TeamQuestionsScreen(
            eventName = registration.eventName,
            isPublished = registration.isPublished,
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

        FormBuilderScreen(
            eventId = eventId,
            eventName = registration.eventName,
            isPublished = registration.isPublished,

            onPublish = { _ ->
                navController.popBackStack()
            },

            onBack = {
                navController.popBackStack()
            },

            initialFields = FakeFormDetails.fields,
        )
    }
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
