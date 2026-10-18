package com.example.campusconnect.core.location.android

import com.example.campusconnect.core.location.TrackingMode
import com.google.android.gms.location.Priority
import org.junit.Assert.assertEquals
import org.junit.Test

class FusedLocationRequestConfigTest {
    @Test
    fun balancedUsesBalancedPriorityAndItsConfiguredIntervals() {
        val request = buildLocationRequest(TrackingMode.BALANCED)
        assertEquals(Priority.PRIORITY_BALANCED_POWER_ACCURACY, locationPriorityFor(TrackingMode.BALANCED))
        assertEquals(TrackingMode.BALANCED.intervalMillis, request.intervalMillis)
        assertEquals(TrackingMode.BALANCED.minUpdateIntervalMillis, request.minUpdateIntervalMillis)
    }

    @Test
    fun highPrecisionUsesHighAccuracyPriorityAndItsConfiguredIntervals() {
        val request = buildLocationRequest(TrackingMode.HIGH_PRECISION)
        assertEquals(Priority.PRIORITY_HIGH_ACCURACY, locationPriorityFor(TrackingMode.HIGH_PRECISION))
        assertEquals(TrackingMode.HIGH_PRECISION.intervalMillis, request.intervalMillis)
        assertEquals(TrackingMode.HIGH_PRECISION.minUpdateIntervalMillis, request.minUpdateIntervalMillis)
        assertEquals(false, request.isWaitForAccurateLocation)
    }

    @Test
    fun callbackFixPredatingRequestIsClassifiedAsCached() {
        assertEquals(true, isFixFromBeforeRequest(9_000L, 10_000L))
        assertEquals(false, isFixFromBeforeRequest(10_000L, 10_000L))
    }
}
