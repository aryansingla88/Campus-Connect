package com.example.campusconnect.feature.profile.viewmodel

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import com.example.campusconnect.model.StatPanel

// Placeholder – wire up real data layer when the API is ready.
class ViewProfileViewModel : ViewModel() {

    var activePanel by mutableStateOf(StatPanel.NONE)
        private set

    fun togglePanel(panel: StatPanel) {
        activePanel = if (activePanel == panel) StatPanel.NONE else panel
    }
}
