package com.example.campusconnect.core.location.model

sealed class LocationStatus {
    data object Unavailable : LocationStatus()
    data object PermissionDenied : LocationStatus()
    data object PreciseLocationPermissionRequired : LocationStatus()
    data object ServicesDisabled : LocationStatus()
    data object Acquiring : LocationStatus()
    data class Available(val fix: LocationFix) : LocationStatus()
    data class Approximate(val fix: LocationFix) : LocationStatus()
    data class Stale(val lastFix: LocationFix?) : LocationStatus()
    data class InsufficientAccuracy(val lastFix: LocationFix?) : LocationStatus()
    data class Error(val message: String) : LocationStatus()
}

data class DisplayLocation(
    val fix: LocationFix?,
    val isStale: Boolean
)

/** Returns the position to display without treating a retained fix as a current fix. */
fun LocationStatus.displayLocation(lastAcceptedFix: LocationFix?): DisplayLocation = when (this) {
    is LocationStatus.Available -> DisplayLocation(fix, isStale = false)
    is LocationStatus.Approximate -> DisplayLocation(fix, isStale = false)
    is LocationStatus.Stale -> DisplayLocation(lastFix ?: lastAcceptedFix, isStale = true)
    is LocationStatus.InsufficientAccuracy -> DisplayLocation(lastFix ?: lastAcceptedFix, isStale = true)
    LocationStatus.Unavailable,
    LocationStatus.PermissionDenied,
    LocationStatus.PreciseLocationPermissionRequired,
    LocationStatus.ServicesDisabled,
    LocationStatus.Acquiring,
    is LocationStatus.Error -> DisplayLocation(lastAcceptedFix, isStale = true)
}
