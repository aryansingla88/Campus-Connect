package com.example.campusconnect.feature.events

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

import androidx.compose.material3.Icon
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.offset

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*

import com.example.campusconnect.feature.events.components.ToolIcon

import androidx.compose.runtime.*
import com.example.campusconnect.feature.events.components.ModeToggle

import com.example.campusconnect.feature.events.components.EventCreateDialog


@Composable
fun EventScreen() {

    var showDialog by remember { mutableStateOf(true) } // TEMP for testing
    val fakeService = remember { FakeEventService() }
    val events = fakeService.getEvents()

    var selectedMode by remember { mutableStateOf<String?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {

        // 🔹 BACKGROUND
        Box(modifier = Modifier.fillMaxSize())

        // 🔹 TOP CENTER → SELF / SHARED (keep simple for now)

        Row(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            ModeToggle(
                text = "Self",
                icon = Icons.Default.Person,
                selected = selectedMode == "self",
                onClick = {
                    selectedMode = if (selectedMode == "self") null else "self"
                }
            )

            ModeToggle(
                text = "Shared",
                icon = Icons.Default.Group,
                selected = selectedMode == "shared",
                onClick = {
                    selectedMode = if (selectedMode == "shared") null else "shared"
                }
            )
        }

        // 🔹 TOP RIGHT → SETTINGS + VIEW
        Column(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 80.dp, end = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ToolIcon(Icons.Default.Settings)
            ToolIcon(Icons.Default.Visibility)
        }

        // 🔹 RIGHT SIDE → TOOL STACK ABOVE PLUS
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 90.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ToolIcon(Icons.Default.Notifications)
            ToolIcon(Icons.Default.GroupAdd)   // manage
            ToolIcon(Icons.Default.Delete)
        }

        // 🔹 BOTTOM RIGHT → PLUS (MAIN ACTION)
        ToolIcon(
            icon = Icons.Default.AddLocation,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        )

        // 🔹 LEFT MID → REGISTER + PARTICIPANTS
        Column(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 16.dp, bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ToolIcon(Icons.Default.PersonAdd)
            ToolIcon(Icons.Default.Group)
        }

        // 🔹 BOTTOM LEFT → CHAT
        ToolIcon(
            icon = Icons.Default.Chat,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp)
        )

        events.forEach { event ->

            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = null,
                tint = Color(0xFFFF6F00), // your orange
                modifier = Modifier
                    .offset(
                        x = (event.xRatio * 300).dp,
                        y = (event.yRatio * 600).dp
                    )
                    .size(36.dp)
            )
        }

        if (showDialog) {
            EventCreateDialog(
                onDismiss = { showDialog = false },
                onCreate = {
                    println("Event Created")
                    showDialog = false
                }
            )
        }
    }
}