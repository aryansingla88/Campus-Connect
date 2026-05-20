package com.example.campusconnect.feature.profile.viewmodel

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import com.example.campusconnect.model.StatPanel

// Placeholder – API DATA
class ViewProfileViewModel : ViewModel() {

    var activePanel by mutableStateOf(StatPanel.NONE)
        private set

    fun togglePanel(panel: StatPanel) {
        activePanel = if (activePanel == panel) StatPanel.NONE else panel
    }
}
