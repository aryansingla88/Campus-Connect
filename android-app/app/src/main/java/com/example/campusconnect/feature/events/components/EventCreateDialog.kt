package com.example.campusconnect.feature.events.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import com.example.campusconnect.feature.events.EventUiState

@Composable
fun EventCreateDialog(
    state: EventUiState,
    onTitleChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onDateChange: (String) -> Unit,
    onVenueChange: (String) -> Unit,
    onStartTimeChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onCreate: () -> Unit
) {

    // 🔹 BACKGROUND OVERLAY
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0x99000000)), // dark overlay
        contentAlignment = Alignment.Center
    ) {

        // 🔹 CARD
        Column(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .background(
                    color = Color.White,
                    shape = RoundedCornerShape(20.dp)
                )
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {

            // 🔹 HEADER
            Text(
                text = "Create Event",
                fontSize = 20.sp
            )

            // 🔹 TITLE
            OutlinedTextField(
                value = state.title,
                onValueChange = onTitleChange,
                label = { Text("Title *") },
                modifier = Modifier.fillMaxWidth()
            )

            // 🔹 DESCRIPTION
            OutlinedTextField(
                value = state.description,
                onValueChange = onDescriptionChange,
                label = { Text("Description *") },
                modifier = Modifier.fillMaxWidth()
            )

            // 🔹 DATE + VENUE (same row)
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {

                OutlinedTextField(
                    value = state.date,
                    onValueChange = onDateChange,
                    label = { Text("Date *") },
                    modifier = Modifier.weight(1f)
                )

                OutlinedTextField(
                    value = state.venue,
                    onValueChange = onVenueChange,
                    label = { Text("Venue *") },
                    modifier = Modifier.weight(1f)
                )
            }

            // 🔹 START TIME
            OutlinedTextField(
                value = state.startTime,
                onValueChange = onStartTimeChange,
                label = { Text("Start Time *") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 🔹 BUTTONS (equal size)
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cancel")
                }

                Button(
                    onClick = onCreate,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Create")
                }
            }
        }
    }
}
