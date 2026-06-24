package com.example.campusconnect.feature.events.registrations

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.campusconnect.feature.events.registrations.data.FakeFormDetails
import com.example.campusconnect.feature.events.registrations.ui.FormBuilderScreen

fun NavGraphBuilder.RegisterationNav(navController: NavController) {
    composable(
        route     = "form_builder/{eventId}",
        arguments = listOf(navArgument("eventId") { type = NavType.IntType }),
    ) { backStackEntry ->
        val eventId = backStackEntry.arguments?.getInt("eventId") ?: return@composable

        FormBuilderScreen(
            eventId = eventId,
            onPublish = { _, _, _ -> navController.popBackStack() },
            onBack = { navController.popBackStack() },
            initialTitle = FakeFormDetails.title,
            initialDescription = FakeFormDetails.description,
            initialFields = FakeFormDetails.fields,
        )
    }
}

fun NavController.navigateToFormBuilder(eventId: Int) {
    navigate("form_builder/$eventId")
}