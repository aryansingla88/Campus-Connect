package com.example.campusconnect.feature.profile

import androidx.navigation.NavController
import com.example.campusconnect.core.session.SessionManager

/** Navigate to the appropriate profile for an ordinary user-profile click. */
fun NavController.navigateToUserProfile(userId: Int) {
    val targetRoute = if (
        SessionManager.getAuthenticatedUserId() == userId
    ) {
        ProfileRoutes.MY_PROFILE
    } else {
        ProfileRoutes.viewProfile(userId)
    }

    val currentEntry = currentBackStackEntry
    val currentRoute = currentEntry?.destination?.route
    val alreadyAtTarget = when {
        targetRoute == ProfileRoutes.MY_PROFILE ->
            currentRoute == "profile/me?edit={edit}"

        currentRoute == ProfileRoutes.VIEW_PROFILE ->
            currentEntry.arguments?.getInt("userId") == userId

        else -> false
    }

    if (alreadyAtTarget) return

    navigate(targetRoute) {
        launchSingleTop = true
    }
}
