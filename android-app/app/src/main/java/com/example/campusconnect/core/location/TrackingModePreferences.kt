package com.example.campusconnect.core.location

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Device-local tracking preference. A tracking mode is not account data. */
object TrackingModePreferences {
    private const val PREF_NAME = "location_tracking_preferences"
    private const val KEY_TRACKING_MODE = "tracking_mode"

    private lateinit var preferences: SharedPreferences
    private val _mode = MutableStateFlow(TrackingMode.BALANCED)
    val mode: StateFlow<TrackingMode> = _mode.asStateFlow()

    @Synchronized
    fun initialize(context: Context) {
        preferences = context.applicationContext.getSharedPreferences(
            PREF_NAME,
            Context.MODE_PRIVATE
        )
        _mode.value = TrackingMode.fromStorageValue(
            preferences.getString(KEY_TRACKING_MODE, TrackingMode.BALANCED.storageValue)
        )
    }

    @Synchronized
    fun setMode(mode: TrackingMode) {
        check(::preferences.isInitialized) { "TrackingModePreferences is not initialized" }
        preferences.edit().putString(KEY_TRACKING_MODE, mode.storageValue).apply()
        _mode.value = mode
    }
}
