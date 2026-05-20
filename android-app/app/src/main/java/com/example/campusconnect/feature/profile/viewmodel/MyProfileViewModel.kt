package com.example.campusconnect.feature.profile.viewmodel

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import com.example.campusconnect.model.StatPanel

class MyProfileViewModel : ViewModel() {

    // ── Stat panel (Connections / Honor / Clubs / Interests / NONE) ───────────
    var activePanel by mutableStateOf(StatPanel.NONE)
        private set

    fun togglePanel(panel: StatPanel) {
        activePanel = if (activePanel == panel) StatPanel.NONE else panel
    }

    // ── Manage panel (swaps in below StatsRow via bottom-bar CTA) ─────────────
    var activeManagePanel by mutableStateOf<StatPanel?>(null)
        private set

    fun openManagePanel(panel: StatPanel) {
        activeManagePanel = panel
    }

    fun closeManagePanel() {
        activeManagePanel = null
    }
}