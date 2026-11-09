package com.example.campusconnect.feature.registrations.ui.screens

import com.example.campusconnect.feature.registrations.ui.components.*

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val LoaderBackground = Color(0xFFFFFBF7)
private val LoaderOrange = Color(0xFFFF6D00)

/** Branded transition shown while the event's registration draft is initialized. */
@Composable
fun CreatingRegistrationScreen(
    error: String?,
    onBack: () -> Unit,
    onRetry: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize().background(LoaderBackground),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(Modifier.fillMaxSize()) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.align(Alignment.TopStart).padding(start = 8.dp, top = 12.dp),
            ) {
                Icon(Icons.Outlined.ArrowBack, contentDescription = "Back", tint = Color(0xFF333333))
            }

            Column(
                modifier = Modifier.align(Alignment.Center).padding(horizontal = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.size(112.dp)) {
                    Box(Modifier.fillMaxSize().background(Color(0xFFFFF0E3), CircleShape))
                    if (error == null) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(78.dp),
                            strokeWidth = 5.dp,
                            color = LoaderOrange,
                            trackColor = Color(0xFFFFD8B8),
                        )
                    }
                    Icon(
                        imageVector = if (error == null) Icons.Outlined.Description else Icons.Outlined.ErrorOutline,
                        contentDescription = null,
                        tint = LoaderOrange,
                        modifier = Modifier.size(38.dp),
                    )
                }

                Spacer(Modifier.size(24.dp))
                Text(
                    text = if (error == null) "Starting your registration..." else "Couldn't start your registration",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF242424),
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.size(8.dp))
                Text(
                    text = error ?: "Setting up your registration. Please wait a moment.",
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    color = Color(0xFF777777),
                    textAlign = TextAlign.Center,
                )
                if (error != null) {
                    Spacer(Modifier.size(20.dp))
                    Button(
                        onClick = onRetry,
                        colors = ButtonDefaults.buttonColors(containerColor = LoaderOrange),
                    ) {
                        Icon(Icons.Outlined.Refresh, contentDescription = null)
                        Spacer(Modifier.size(8.dp))
                        Text("Try again")
                    }
                }
            }
        }
    }
}
