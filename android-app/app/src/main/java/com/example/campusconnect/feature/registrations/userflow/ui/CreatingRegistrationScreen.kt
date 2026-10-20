package com.example.campusconnect.feature.registrations.userflow.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.campusconnect.feature.registrations.ui.components.CardBg
import com.example.campusconnect.feature.registrations.ui.components.OrangePrimary
import com.example.campusconnect.feature.registrations.ui.components.TextPrimary

/** Creation loading/error presentation; request state and retry remain owned by the ViewModel. */
@Composable
fun CreatingRegistrationScreen(
    error: String?,
    onBack: () -> Unit,
    onRetry: () -> Unit,
) {
    Box(Modifier.fillMaxSize().background(CardBg)) {
        IconButton(
            onClick = onBack,
            modifier = Modifier.align(Alignment.TopStart).statusBarsPadding().padding(start = 4.dp, top = 4.dp),
        ) {
            Icon(Icons.Outlined.ArrowBack, contentDescription = "Back", tint = TextPrimary)
        }

        RegistrationLoadingPresentation(
            title = if (error == null) "Starting your registration..." else "Couldn't start your registration",
            message = error ?: "Setting up your registration. Please wait a moment.",
            icon = if (error == null) Icons.Outlined.Description else Icons.Outlined.ErrorOutline,
            isLoading = error == null,
            modifier = Modifier.padding(top = 56.dp, bottom = 64.dp),
        ) {
            if (error != null) {
                Spacer(Modifier.height(20.dp))
                Button(
                    onClick = onRetry,
                    colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary, contentColor = Color.White),
                ) {
                    Text("Try again", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
