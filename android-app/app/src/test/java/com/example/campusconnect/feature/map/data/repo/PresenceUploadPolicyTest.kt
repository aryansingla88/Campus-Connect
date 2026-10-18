package com.example.campusconnect.feature.map.data.repo

import com.example.campusconnect.core.location.TrackingMode
import com.example.campusconnect.core.location.model.LocationFix
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PresenceUploadPolicyTest {
    private val last = fix(latitude = 30.0)

    @Test
    fun movementThresholdTriggersUpload() {
        assertTrue(
            PresenceUploadPolicy.shouldUpload(
                candidate = fix(latitude = 30.0003),
                lastUploaded = last,
                lastUploadElapsedRealtime = 5_000L,
                nowElapsedRealtime = 10_000L,
                mode = TrackingMode.BALANCED
            )
        )
    }

    @Test
    fun smallMovementWaitsUntilHeartbeat() {
        assertFalse(
            PresenceUploadPolicy.shouldUpload(
                candidate = fix(latitude = 30.00001),
                lastUploaded = last,
                lastUploadElapsedRealtime = 5_000L,
                nowElapsedRealtime = 20_000L,
                mode = TrackingMode.BALANCED
            )
        )
        assertTrue(
            PresenceUploadPolicy.shouldUpload(
                candidate = fix(latitude = 30.00001),
                lastUploaded = last,
                lastUploadElapsedRealtime = 5_000L,
                nowElapsedRealtime = 125_000L,
                mode = TrackingMode.BALANCED
            )
        )
    }

    private fun fix(latitude: Double) = LocationFix(
        latitude = latitude,
        longitude = 76.0,
        accuracyMeters = 5f,
        timeMillis = 1L,
        elapsedRealtimeNanos = 1L,
        provider = "test"
    )
}
