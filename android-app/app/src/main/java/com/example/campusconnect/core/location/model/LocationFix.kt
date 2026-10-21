package com.example.campusconnect.core.location.model

data class LocationFix(
    val latitude: Double,
    val longitude: Double,
    val accuracyMeters: Float,
    val timeMillis: Long,
    val elapsedRealtimeNanos: Long,
    val provider: String?,
    /** True when sourced from a provider's last-known snapshot rather than a fresh callback. */
    val isCached: Boolean = false
)
