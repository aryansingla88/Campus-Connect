package com.example.campusconnect.feature.test

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Modifier


@Composable
fun TestScreen(
    onAuth: () -> Unit,
    onPosts: () -> Unit,
    onEvents: () -> Unit,
    onMap: () -> Unit,
    onProfile: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.padding(16.dp)
    ) {

        Button(onClick = onAuth) { Text("Auth") }
        Button(onClick = onPosts) { Text("Posts") }
        Button(onClick = onEvents) { Text("Events") }
        Button(onClick = onMap) { Text("Map") }
        Button(onClick = onProfile) { Text("Profile") }
    }
}