package com.example.campusconnect.feature.registrations.userflow.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Send
import androidx.compose.runtime.Composable

/** Shared busy-state wrapper; back blocking and duplicate-submit protection stay in the flow/ViewModel. */
@Composable
fun SubmittingRegistrationScreen(
    title: String = "Submitting your registration...",
    message: String = "Please wait while we finalize your registration.",
    isLoading: Boolean = true,
    showSendIcon: Boolean = true,
) {
    RegistrationLoadingPresentation(
        title = title,
        message = message,
        icon = if (showSendIcon) Icons.Outlined.Send else Icons.Outlined.Description,
        isLoading = isLoading,
    )
}
