package com.example.campusconnect.core.location

import com.example.campusconnect.core.location.model.LocationFix
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class LocationQualityValidatorTest {
    private val nowElapsed = 90_000_000_000L
    private val nowWall = 1_000_000L

    @Test
    fun acceptsFreshAccurateFix() {
        assertNull(
            LocationQualityValidator.rejectionReason(
                fix(30.0, 76.0, accuracy = 8f, elapsed = nowElapsed - 2_000_000_000L),
                TrackingMode.BALANCED,
                nowElapsed,
                nowWall,
                null
            )
        )
    }

    @Test
    fun rejectsInvalidCoordinatesAndLowAccuracy() {
        assertEquals(
            LocationRejection.INVALID_COORDINATES,
            LocationQualityValidator.rejectionReason(
                fix(91.0, 76.0, 5f, nowElapsed), TrackingMode.BALANCED, nowElapsed, nowWall, null
            )
        )
        assertEquals(
            LocationRejection.LOW_ACCURACY,
            LocationQualityValidator.rejectionReason(
                fix(30.0, 76.0, 100f, nowElapsed), TrackingMode.HIGH_PRECISION, nowElapsed, nowWall, null
            )
        )
    }

    @Test
    fun rejectsStaleAndOutOfOrderFixes() {
        assertEquals(
            LocationRejection.STALE,
            LocationQualityValidator.rejectionReason(
                fix(30.0, 76.0, 5f, nowElapsed - 31_000_000_000L),
                TrackingMode.BALANCED,
                nowElapsed,
                nowWall,
                null
            )
        )
        assertEquals(
            LocationRejection.OUT_OF_ORDER,
            LocationQualityValidator.rejectionReason(
                fix(30.0, 76.0, 5f, nowElapsed - 1_000_000_000L),
                TrackingMode.BALANCED,
                nowElapsed,
                nowWall,
                fix(30.0, 76.0, 5f, nowElapsed - 500_000_000L)
            )
        )
    }

    @Test
    fun appliesModeSpecificAccuracyAndAgeLimits() {
        val balancedAccuracy = fix(30.0, 76.0, accuracy = 75f, elapsed = nowElapsed - 1_000_000_000L)
        assertNull(
            LocationQualityValidator.rejectionReason(
                balancedAccuracy, TrackingMode.BALANCED, nowElapsed, nowWall, null
            )
        )
        assertEquals(
            LocationRejection.LOW_ACCURACY,
            LocationQualityValidator.rejectionReason(
                balancedAccuracy, TrackingMode.HIGH_PRECISION, nowElapsed, nowWall, null
            )
        )

        val elevenSecondsOld = fix(30.0, 76.0, accuracy = 5f, elapsed = nowElapsed - 11_000_000_000L)
        assertEquals(
            LocationRejection.STALE,
            LocationQualityValidator.rejectionReason(
                elevenSecondsOld, TrackingMode.HIGH_PRECISION, nowElapsed, nowWall, null
            )
        )
        assertNull(
            LocationQualityValidator.rejectionReason(
                elevenSecondsOld, TrackingMode.BALANCED, nowElapsed, nowWall, null
            )
        )
    }

    @Test
    fun cachedFixIsNeverAcceptedAsFresh() {
        val cached = fix(30.0, 76.0, 5f, nowElapsed - 1_000_000_000L, isCached = true)
        assertEquals(
            LocationRejection.CACHED,
            LocationQualityValidator.rejectionReason(
                cached, TrackingMode.BALANCED, nowElapsed, nowWall, null
            )
        )
    }

    private fun fix(
        latitude: Double,
        longitude: Double,
        accuracy: Float,
        elapsed: Long,
        isCached: Boolean = false
    ) = LocationFix(
        latitude = latitude,
        longitude = longitude,
        accuracyMeters = accuracy,
        timeMillis = nowWall - (nowElapsed - elapsed) / 1_000_000L,
        elapsedRealtimeNanos = elapsed,
        provider = "test",
        isCached = isCached
    )
}
