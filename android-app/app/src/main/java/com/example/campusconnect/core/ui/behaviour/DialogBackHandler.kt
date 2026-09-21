package com.example.campusconnect.core.ui.behaviour

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable

@Composable
fun DialogBackHandler(
    visible: Boolean,
    onDismiss: () -> Unit
) {
    BackHandler(
        enabled = visible,
        onBack = onDismiss
    )
}