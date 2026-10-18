package com.example.campusconnect.core.location

import com.example.campusconnect.core.location.android.LocationServicesDisabledException
import com.example.campusconnect.core.location.model.LocationFix
import com.example.campusconnect.core.location.model.LocationStatus
import com.example.campusconnect.core.location.model.displayLocation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.channels.awaitClose
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.concurrent.atomic.AtomicInteger

class LocationTrackingCoordinatorTest {
    private val nowElapsed = 100_000_000_000L
    private val nowWall = 2_000_000L

    @Test
    fun modeChangeWaitsForOldSubscriptionCleanup() = runBlocking {
        val active = AtomicInteger()
        val maxActive = AtomicInteger()
        val starts = Channel<TrackingMode>(Channel.UNLIMITED)
        val stops = Channel<TrackingMode>(Channel.UNLIMITED)
        val source = locationSource { mode ->
            flow {
                val currentActive = active.incrementAndGet()
                maxActive.updateAndGet { maxOf(it, currentActive) }
                starts.send(mode)
                try {
                    suspendCancellableCoroutine<Unit> { }
                } finally {
                    withContext(NonCancellable) { delay(25) }
                    active.decrementAndGet()
                    stops.send(mode)
                }
            }
        }
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        val coordinator = coordinator(source)
        try {
            coordinator.start(scope, TrackingMode.BALANCED)
            assertEquals(TrackingMode.BALANCED, withTimeout(2_000) { starts.receive() })
            coordinator.start(scope, TrackingMode.HIGH_PRECISION)
            assertEquals(TrackingMode.BALANCED, withTimeout(2_000) { stops.receive() })
            assertEquals(TrackingMode.HIGH_PRECISION, withTimeout(2_000) { starts.receive() })
            assertEquals(1, maxActive.get())

            coordinator.start(scope, TrackingMode.BALANCED)
            assertEquals(TrackingMode.HIGH_PRECISION, withTimeout(2_000) { stops.receive() })
            assertEquals(TrackingMode.BALANCED, withTimeout(2_000) { starts.receive() })
            assertEquals(1, maxActive.get())
        } finally {
            coordinator.stop()
            scope.cancel()
        }
    }

    @Test
    fun cachedFixIsDisplayedAsStaleAndFreshFixBecomesAvailable() = runBlocking {
        val cached = fix(elapsed = nowElapsed - 1_000_000_000L, cached = true)
        val fresh = fix(elapsed = nowElapsed - 500_000_000L)
        val updates = Channel<LocationFix>(Channel.UNLIMITED)
        val source = locationSource {
            flow { for (fix in updates) emit(fix) }
        }
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        val coordinator = coordinator(source)
        try {
            coordinator.start(scope, TrackingMode.HIGH_PRECISION)
            updates.send(cached)
            awaitStatus(coordinator) { it is LocationStatus.Stale }
            val provisional = coordinator.status.value as LocationStatus.Stale
            assertEquals(cached, provisional.lastFix)
            assertTrue(coordinator.status.value.displayLocation(null).isStale)

            updates.send(fresh)
            awaitStatus(coordinator) { it is LocationStatus.Available }
            val available = coordinator.status.value as LocationStatus.Available
            assertEquals(fresh, available.fix)
            assertFalse(available.fix.isCached)
        } finally {
            coordinator.stop()
            scope.cancel()
            updates.close()
        }
    }

    @Test
    fun highPrecisionStartupWithoutCachedFixStaysInAcquiringState() = runBlocking {
        val source = locationSource {
            flow { suspendCancellableCoroutine<Unit> { } }
        }
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        val coordinator = coordinator(source)
        try {
            coordinator.start(scope, TrackingMode.HIGH_PRECISION)
            awaitStatus(coordinator) { it is LocationStatus.Acquiring }

            assertEquals(null, coordinator.status.value.displayLocation(null).fix)
        } finally {
            coordinator.stop()
            scope.cancel()
        }
    }

    @Test
    fun switchingProviderKeepsPreviousPositionStaleUntilNewModeAcceptsAFix() = runBlocking {
        val balancedUpdates = Channel<LocationFix>(Channel.UNLIMITED)
        val highPrecisionStarted = Channel<Unit>(Channel.RENDEZVOUS)
        val source = locationSource { mode ->
            when (mode) {
                TrackingMode.BALANCED -> flow { for (fix in balancedUpdates) emit(fix) }
                TrackingMode.HIGH_PRECISION -> flow {
                    highPrecisionStarted.send(Unit)
                    suspendCancellableCoroutine<Unit> { }
                }
            }
        }
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        val coordinator = coordinator(source)
        try {
            coordinator.start(scope, TrackingMode.BALANCED)
            val balancedFix = fix(elapsed = nowElapsed - 1_000_000_000L)
            balancedUpdates.send(balancedFix)
            awaitStatus(coordinator) { it is LocationStatus.Available }

            coordinator.start(scope, TrackingMode.HIGH_PRECISION)
            withTimeout(2_000) { highPrecisionStarted.receive() }

            assertTrue(coordinator.status.value is LocationStatus.Acquiring)
            val retained = coordinator.status.value.displayLocation(balancedFix)
            assertEquals(balancedFix, retained.fix)
            assertTrue(retained.isStale)
        } finally {
            coordinator.stop()
            scope.cancel()
            balancedUpdates.close()
        }
    }

    @Test
    fun callbackFromCancelledProviderCannotOverwriteNewModeStatus() = runBlocking {
        val oldProviderCallback = Channel<(LocationFix) -> Unit>(Channel.CONFLATED)
        val source = locationSource { mode ->
            if (mode == TrackingMode.BALANCED) {
                callbackFlow {
                    oldProviderCallback.send { fix -> trySend(fix) }
                    awaitClose { }
                }
            } else {
                flow { suspendCancellableCoroutine<Unit> { } }
            }
        }
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        val coordinator = coordinator(source)
        try {
            coordinator.start(scope, TrackingMode.BALANCED)
            val callback = withTimeout(2_000) { oldProviderCallback.receive() }
            val first = fix(elapsed = nowElapsed - 1_000_000_000L)
            callback(first)
            awaitStatus(coordinator) { it is LocationStatus.Available }

            coordinator.start(scope, TrackingMode.HIGH_PRECISION)
            awaitStatus(coordinator) { it is LocationStatus.Acquiring }
            callback(first.copy(
                latitude = 31.0,
                elapsedRealtimeNanos = nowElapsed - 500_000_000L,
                timeMillis = first.timeMillis + 500L
            ))
            delay(10)

            assertTrue(coordinator.status.value is LocationStatus.Acquiring)
            assertEquals(first, coordinator.status.value.displayLocation(first).fix)
            assertTrue(coordinator.status.value.displayLocation(first).isStale)
        } finally {
            coordinator.stop()
            scope.cancel()
        }
    }

    @Test
    fun precisePermissionRequirementStopsTrackingWithExplicitStatus() = runBlocking {
        val source = locationSource {
            flow { throw PreciseLocationPermissionRequiredException() }
        }
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        val coordinator = coordinator(source)
        try {
            coordinator.start(scope, TrackingMode.HIGH_PRECISION)
            awaitStatus(coordinator) { it is LocationStatus.PreciseLocationPermissionRequired }
            assertTrue(coordinator.status.value.displayLocation(null).isStale)
        } finally {
            coordinator.stop()
            scope.cancel()
        }
    }

    @Test
    fun freshPoorGpsFixIsShownAsApproximateWhenNoAcceptableFixExists() = runBlocking {
        val poorFix = fix(elapsed = nowElapsed - 500_000_000L).copy(accuracyMeters = 60f)
        val updates = Channel<LocationFix>(Channel.UNLIMITED)
        val source = locationSource { flow { for (fix in updates) emit(fix) } }
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        val coordinator = coordinator(source)
        try {
            coordinator.start(scope, TrackingMode.HIGH_PRECISION)
            updates.send(poorFix)
            awaitStatus(coordinator) { it is LocationStatus.Approximate }

            val approximate = coordinator.status.value as LocationStatus.Approximate
            assertEquals(poorFix, approximate.fix)
            assertEquals(poorFix, approximate.displayLocation(null).fix)
            assertFalse(approximate.displayLocation(null).isStale)
        } finally {
            coordinator.stop()
            scope.cancel()
            updates.close()
        }
    }

    @Test
    fun poorGpsFixKeepsGoodRecentFixUntilItAgesOutThenShowsFreshApproximateFix() = runBlocking {
        var nowElapsed = this@LocationTrackingCoordinatorTest.nowElapsed
        var nowWall = this@LocationTrackingCoordinatorTest.nowWall
        val updates = Channel<LocationFix>(Channel.UNLIMITED)
        val source = locationSource { flow { for (fix in updates) emit(fix) } }
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        val coordinator = LocationTrackingCoordinator(
            source = source,
            nowElapsedRealtimeNanos = { nowElapsed },
            nowTimeMillis = { nowWall },
            retryDelayMillis = { 0L }
        )
        try {
            coordinator.start(scope, TrackingMode.HIGH_PRECISION)
            val accepted = fix(elapsed = nowElapsed - 500_000_000L)
            updates.send(accepted)
            awaitStatus(coordinator) { it is LocationStatus.Available }

            val poorWhileRecent = fix(elapsed = nowElapsed - 250_000_000L).copy(accuracyMeters = 60f)
            updates.send(poorWhileRecent)
            awaitStatus(coordinator) { it is LocationStatus.InsufficientAccuracy }
            assertEquals(accepted, (coordinator.status.value as LocationStatus.InsufficientAccuracy).lastFix)

            nowElapsed += 11_000_000_000L
            nowWall += 11_000L
            val poorAfterExpiry = fix(elapsed = nowElapsed - 500_000_000L).copy(
                accuracyMeters = 60f,
                timeMillis = nowWall - 500L
            )
            updates.send(poorAfterExpiry)
            awaitStatus(coordinator) { it is LocationStatus.Approximate }
            assertEquals(poorAfterExpiry, (coordinator.status.value as LocationStatus.Approximate).fix)
        } finally {
            coordinator.stop()
            scope.cancel()
            updates.close()
        }
    }

    @Test
    fun successiveFreshUpdatesKeepAdvancingTheTrackedLocation() = runBlocking {
        val first = fix(elapsed = nowElapsed - 2_000_000_000L)
        val second = first.copy(
            latitude = first.latitude + 0.0001,
            elapsedRealtimeNanos = nowElapsed - 1_000_000_000L,
            timeMillis = first.timeMillis + 1_000L
        )
        val updates = Channel<LocationFix>(Channel.UNLIMITED)
        val source = locationSource { flow { for (fix in updates) emit(fix) } }
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        val coordinator = coordinator(source)
        try {
            coordinator.start(scope, TrackingMode.BALANCED)
            updates.send(first)
            awaitStatus(coordinator) { it is LocationStatus.Available && it.fix == first }
            updates.send(second)
            awaitStatus(coordinator) { it is LocationStatus.Available && it.fix == second }
            assertEquals(second, (coordinator.status.value as LocationStatus.Available).fix)
        } finally {
            coordinator.stop()
            scope.cancel()
            updates.close()
        }
    }

    @Test
    fun highPrecisionAcceptsFreshNearbyFixWithinLimitAndContinuesUpdating() = runBlocking {
        val updates = Channel<LocationFix>(Channel.UNLIMITED)
        val evaluations = Channel<LocationRejection?>(Channel.UNLIMITED)
        val source = locationSource { flow { for (fix in updates) emit(fix) } }
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        val coordinator = coordinator(source) { _, _, rejection -> evaluations.trySend(rejection) }
        try {
            coordinator.start(scope, TrackingMode.HIGH_PRECISION)
            val accurate = fix(elapsed = nowElapsed - 2_000_000_000L)
            updates.send(accurate)
            assertEquals(null, withTimeout(2_000) { evaluations.receive() })
            awaitStatus(coordinator) { it is LocationStatus.Available && it.fix == accurate }

            // A fresh nearby fix within the configured limit must not be withheld for being worse.
            val inferiorNearby = accurate.copy(
                longitude = accurate.longitude + 0.000045,
                accuracyMeters = 25f,
                elapsedRealtimeNanos = nowElapsed - 1_000_000_000L,
                timeMillis = accurate.timeMillis + 1_000L
            )
            updates.send(inferiorNearby)
            assertEquals(null, withTimeout(2_000) { evaluations.receive() })
            awaitStatus(coordinator) { it is LocationStatus.Available && it.fix == inferiorNearby }

            // A larger displacement remains eligible even when the reported accuracy is worse.
            val moved = inferiorNearby.copy(
                longitude = accurate.longitude + 0.0002,
                elapsedRealtimeNanos = nowElapsed - 500_000_000L,
                timeMillis = inferiorNearby.timeMillis + 500L
            )
            updates.send(moved)
            assertEquals(null, withTimeout(2_000) { evaluations.receive() })
            awaitStatus(coordinator) { it is LocationStatus.Available && it.fix == moved }
        } finally {
            coordinator.stop()
            scope.cancel()
            updates.close()
        }
    }

    @Test
    fun balancedModeStillAcceptsNewerNearbyFixRegardlessOfRelativeAccuracy() = runBlocking {
        val updates = Channel<LocationFix>(Channel.UNLIMITED)
        val source = locationSource { flow { for (fix in updates) emit(fix) } }
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        val coordinator = coordinator(source)
        try {
            coordinator.start(scope, TrackingMode.BALANCED)
            val accurate = fix(elapsed = nowElapsed - 2_000_000_000L)
            updates.send(accurate)
            awaitStatus(coordinator) { it is LocationStatus.Available && it.fix == accurate }

            val newerButLessAccurate = accurate.copy(
                longitude = accurate.longitude + 0.000045,
                accuracyMeters = 25f,
                elapsedRealtimeNanos = nowElapsed - 1_000_000_000L,
                timeMillis = accurate.timeMillis + 1_000L
            )
            updates.send(newerButLessAccurate)
            awaitStatus(coordinator) {
                it is LocationStatus.Available && it.fix == newerButLessAccurate
            }
            assertEquals(newerButLessAccurate, (coordinator.status.value as LocationStatus.Available).fix)
        } finally {
            coordinator.stop()
            scope.cancel()
            updates.close()
        }
    }

    @Test
    fun firstHighPrecisionFixAfterModeSwitchIsAccepted() = runBlocking {
        val balancedUpdates = Channel<LocationFix>(Channel.UNLIMITED)
        val highUpdates = Channel<LocationFix>(Channel.UNLIMITED)
        val source = locationSource { mode ->
            when (mode) {
                TrackingMode.BALANCED -> flow { for (fix in balancedUpdates) emit(fix) }
                TrackingMode.HIGH_PRECISION -> flow { for (fix in highUpdates) emit(fix) }
            }
        }
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        val coordinator = coordinator(source)
        try {
            coordinator.start(scope, TrackingMode.BALANCED)
            val balancedFix = fix(elapsed = nowElapsed - 2_000_000_000L)
            balancedUpdates.send(balancedFix)
            awaitStatus(coordinator) { it is LocationStatus.Available && it.fix == balancedFix }

            coordinator.start(scope, TrackingMode.HIGH_PRECISION)
            val firstHighFix = balancedFix.copy(
                longitude = balancedFix.longitude + 0.000045,
                accuracyMeters = 25f,
                elapsedRealtimeNanos = nowElapsed - 1_000_000_000L,
                timeMillis = balancedFix.timeMillis + 1_000L
            )
            highUpdates.send(firstHighFix)
            awaitStatus(coordinator) { it is LocationStatus.Available && it.fix == firstHighFix }
        } finally {
            coordinator.stop()
            scope.cancel()
            balancedUpdates.close()
            highUpdates.close()
        }
    }

    @Test
    fun outOfOrderFixCannotReplaceNewerFix() = runBlocking {
        val newer = fix(elapsed = nowElapsed - 500_000_000L)
        val older = fix(elapsed = nowElapsed - 1_000_000_000L)
        val evaluations = Channel<LocationRejection?>(Channel.UNLIMITED)
        val source = locationSource {
            flow {
                emit(newer)
                emit(older)
                suspendCancellableCoroutine<Unit> { }
            }
        }
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        val coordinator = coordinator(source) { _, _, rejection -> evaluations.trySend(rejection) }
        try {
            coordinator.start(scope, TrackingMode.BALANCED)
            assertEquals(null, withTimeout(2_000) { evaluations.receive() })
            assertEquals(LocationRejection.OUT_OF_ORDER, withTimeout(2_000) { evaluations.receive() })
            val available = coordinator.status.value as LocationStatus.Available
            assertEquals(newer, available.fix)
            assertEquals(newer, coordinator.status.value.displayLocation(newer).fix)
            assertFalse(coordinator.status.value.displayLocation(newer).isStale)
        } finally {
            coordinator.stop()
            scope.cancel()
        }
    }

    @Test
    fun providerFailureRetriesAndRecovers() = runBlocking {
        val attempts = AtomicInteger()
        val recoveredFix = fix(elapsed = nowElapsed - 500_000_000L)
        val source = locationSource {
            flow {
                if (attempts.incrementAndGet() == 1) throw LocationServicesDisabledException()
                emit(recoveredFix)
                suspendCancellableCoroutine<Unit> { }
            }
        }
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        val coordinator = coordinator(source)
        try {
            coordinator.start(scope, TrackingMode.BALANCED)
            awaitStatus(coordinator) { it is LocationStatus.Available }
            assertTrue(attempts.get() >= 2)
            assertEquals(recoveredFix, (coordinator.status.value as LocationStatus.Available).fix)
        } finally {
            coordinator.stop()
            scope.cancel()
        }
    }

    @Test
    fun statusMappingRetainsPositionButMarksNonAvailableFixStale() {
        val previous = fix(elapsed = nowElapsed - 2_000_000_000L)
        val acquiring = LocationStatus.Acquiring.displayLocation(previous)
        assertEquals(previous, acquiring.fix)
        assertTrue(acquiring.isStale)

        val accepted = LocationStatus.Available(fix(elapsed = nowElapsed - 500_000_000L))
            .displayLocation(previous)
        assertNotNull(accepted.fix)
        assertFalse(accepted.isStale)
    }

    private fun coordinator(
        source: LocationSource,
        onFixEvaluated: (TrackingMode, LocationFix, LocationRejection?) -> Unit = { _, _, _ -> }
    ) = LocationTrackingCoordinator(
        source = source,
        nowElapsedRealtimeNanos = { nowElapsed },
        nowTimeMillis = { nowWall },
        retryDelayMillis = { 0L },
        onFixEvaluated = onFixEvaluated
    )

    private fun locationSource(updates: (TrackingMode) -> Flow<LocationFix>): LocationSource =
        object : LocationSource {
            override fun locationUpdates(mode: TrackingMode): Flow<LocationFix> = updates(mode)
        }

    private suspend fun awaitStatus(
        coordinator: LocationTrackingCoordinator,
        predicate: (LocationStatus) -> Boolean
    ) {
        withTimeout(2_000) {
            while (!predicate(coordinator.status.value)) delay(1)
        }
    }

    private fun fix(elapsed: Long, cached: Boolean = false) = LocationFix(
        latitude = 30.0,
        longitude = 76.0,
        accuracyMeters = 5f,
        timeMillis = nowWall - (nowElapsed - elapsed) / 1_000_000L,
        elapsedRealtimeNanos = elapsed,
        provider = "test",
        isCached = cached
    )
}
