package com.example.campusconnect.feature.map

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class MapViewModel : ViewModel() {

    private val fakeMapService = FakeMapService()

    private val _uiState = MutableStateFlow(MapUiState())
    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()

    fun loadMarkers() {
        _uiState.value = _uiState.value.copy(
            markers = fakeMapService.getMarkers(),
            isLoading = false,
            errorMessage = null
        )
    }
}