package com.example.campusconnect.core.location

import com.example.campusconnect.core.location.model.LocationFix
import kotlinx.coroutines.flow.Flow

interface LocationSource {
    fun locationUpdates(mode: TrackingMode): Flow<LocationFix>
}

class PreciseLocationPermissionRequiredException :
    SecurityException("High Precision requires precise location permission")
