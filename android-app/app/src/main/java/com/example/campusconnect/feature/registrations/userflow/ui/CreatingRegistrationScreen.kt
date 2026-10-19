package com.example.campusconnect.feature.registrations.userflow.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.campusconnect.feature.registrations.ui.components.CardBg
import com.example.campusconnect.feature.registrations.ui.components.OrangeLight
import com.example.campusconnect.feature.registrations.ui.components.OrangePrimary
import com.example.campusconnect.feature.registrations.ui.components.TextPrimary
import com.example.campusconnect.feature.registrations.ui.components.TextSecondary

/** "Starting your registration…" while the create-registration call is in flight. */
@Composable
fun CreatingRegistrationScreen(
    error: String?,
    onBack: () -> Unit,
    onRetry: () -> Unit,
) {
    Column(Modifier.fillMaxSize().background(CardBg)) {
        IconButton(onClick = onBack, modifier = Modifier.statusBarsPadding().padding(start = 4.dp, top = 4.dp)) {
            Icon(Icons.Outlined.ArrowBack, contentDescription = "Back", tint = TextPrimary)
        }

        Column(
            modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(150.dp)) {
                Box(Modifier.fillMaxSize().background(OrangeLight.copy(alpha = 0.7f), CircleShape))
                if (error == null) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(112.dp),
                        strokeWidth = 6.dp,
                        color = OrangePrimary,
                        trackColor = Color.White.copy(alpha = 0.6f),
                    )
                }
                Icon(
                    if (error == null) Icons.Outlined.Description else Icons.Outlined.ErrorOutline,
                    contentDescription = null,
                    tint = OrangePrimary.copy(alpha = 0.75f),
                    modifier = Modifier.size(52.dp),
                )
            }

            Spacer(Modifier.height(28.dp))
            Text(
                if (error == null) "Starting your registration..." else "Couldn't start your registration",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(10.dp))
            Text(
                error ?: "Setting up your registration. Please wait a moment.",
                fontSize = 14.sp,
                lineHeight = 20.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center,
            )
            if (error != null) {
                Spacer(Modifier.height(20.dp))
                Button(
                    onClick = onRetry,
                    colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary, contentColor = Color.White),
                ) { Text("Try again") }
            }
        }
        Spacer(Modifier.height(64.dp))
    }
}
