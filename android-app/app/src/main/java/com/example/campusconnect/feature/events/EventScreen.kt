package com.example.campusconnect.feature.events

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.campusconnect.event.components.MapMode
import com.example.campusconnect.event.components.MapView
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun EventScreen() {

    val viewModel: EventViewModel = viewModel()
    val state = viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier.padding(16.dp)
    ) {

        Text("Create Event", style = MaterialTheme.typography.titleLarge)

        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = state.value.title,
            onValueChange = { viewModel.updateTitle(it) },
            label = { Text("Title") }
        )

        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = state.value.description,
            onValueChange = { viewModel.updateDescription(it) },
            label = { Text("Description") }
        )

        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = state.value.time,
            onValueChange = { viewModel.updateTime(it) },
            label = { Text("Time") }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            viewModel.createEvent(createdBy = 1)
        }) {
            Text("Create Event")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("Select Location")

        MapView(
            mode = MapMode.SELECT,
            markers = emptyList(),
            onMapClick = { latLng ->
                viewModel.setLocation(latLng.latitude, latLng.longitude)
            }
        )

        state.value.selectedLocation?.let {
            Text("Selected: ${it.first}, ${it.second}")
        }

        // Show error
        state.value.error?.let {
            Text(text = it, color = Color.Red)
        }

        state.value.success.let { success ->
            if (success) {
                Text("Event Created Successfully!", color = Color.Green)
            }
        }
    }
}

@Composable
fun MapView(
    mode: Any,
    markers: List<Any>,
    onMapClick: (Any) -> Unit
) {}