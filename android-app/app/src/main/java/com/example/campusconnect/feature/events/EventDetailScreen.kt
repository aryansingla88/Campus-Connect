package com.example.campusconnect.feature.events

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

import com.example.campusconnect.model.Event

@Composable
fun EventDetailScreen(event: Event) {

    Column(modifier = Modifier.padding(16.dp)) {

        Text(event.title, style = MaterialTheme.typography.titleLarge)

        Spacer(modifier = Modifier.height(8.dp))

        Text(event.description ?: "")

        Spacer(modifier = Modifier.height(8.dp))

        Text("Time: ${event.eventTime}")

        Spacer(modifier = Modifier.height(8.dp))

        Text("Location: ${event.latitude}, ${event.longitude}")
    }
}