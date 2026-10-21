package com.example.campusconnect.feature.map.data.repo

import com.example.campusconnect.core.location.TrackingMode
import com.example.campusconnect.core.location.model.LocationFix
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

object PresenceUploadPolicy {
    fun shouldUpload(
        candidate: LocationFix,
        lastUploaded: LocationFix?,
        lastUploadElapsedRealtime: Long?,
        nowElapsedRealtime: Long,
        mode: TrackingMode
    ): Boolean {
        if (lastUploaded == null || lastUploadElapsedRealtime == null) return true
        val movedMeters = distanceMeters(
            lastUploaded.latitude,
            lastUploaded.longitude,
            candidate.latitude,
            candidate.longitude
        )
        val elapsedMillis = nowElapsedRealtime - lastUploadElapsedRealtime
        return movedMeters >= mode.minMovementMeters || elapsedMillis >= mode.heartbeatMillis
    }

    private fun distanceMeters(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val earthRadiusMeters = 6_371_000.0
        val deltaLat = Math.toRadians(lat2 - lat1)
        val deltaLon = Math.toRadians(lon2 - lon1)
        val a = sin(deltaLat / 2) * sin(deltaLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(deltaLon / 2) * sin(deltaLon / 2)
        return earthRadiusMeters * 2 * atan2(sqrt(a), sqrt(1 - a))
    }
}
