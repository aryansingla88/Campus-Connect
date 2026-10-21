package com.example.campusconnect.core.location

import com.example.campusconnect.core.location.model.LocationFix

enum class LocationRejection {
    INVALID_COORDINATES,
    INVALID_ACCURACY,
    STALE,
    FUTURE,
    CACHED,
    OUT_OF_ORDER,
    LOW_ACCURACY
}

object LocationQualityValidator {
    fun rejectionReason(
        fix: LocationFix,
        mode: TrackingMode,
        nowElapsedRealtimeNanos: Long,
        nowTimeMillis: Long,
        previous: LocationFix?
    ): LocationRejection? {
        if (!fix.latitude.isFinite() || !fix.longitude.isFinite() ||
            fix.latitude !in -90.0..90.0 || fix.longitude !in -180.0..180.0
        ) return LocationRejection.INVALID_COORDINATES
        if (!fix.accuracyMeters.isFinite() || fix.accuracyMeters <= 0f) {
            return LocationRejection.INVALID_ACCURACY
        }
        if (fix.timeMillis <= 0L || fix.elapsedRealtimeNanos <= 0L) return LocationRejection.INVALID_ACCURACY
        if (fix.elapsedRealtimeNanos > nowElapsedRealtimeNanos || fix.timeMillis > nowTimeMillis + 120_000L) {
            return LocationRejection.FUTURE
        }
        if (nowElapsedRealtimeNanos - fix.elapsedRealtimeNanos > mode.maxAgeMillis * 1_000_000L ||
            nowTimeMillis - fix.timeMillis > mode.maxAgeMillis
        ) return LocationRejection.STALE
        // A recent cached position can be shown provisionally, but must never become a fresh fix.
        if (fix.isCached) return LocationRejection.CACHED
        if (previous != null && fix.elapsedRealtimeNanos <= previous.elapsedRealtimeNanos) {
            return LocationRejection.OUT_OF_ORDER
        }
        if (fix.accuracyMeters > mode.maxAccuracyMeters) return LocationRejection.LOW_ACCURACY
        return null
    }

}
