package com.example.campusconnect.core.location

import android.os.SystemClock
import com.example.campusconnect.core.location.android.LocationServicesDisabledException
import com.example.campusconnect.core.location.PreciseLocationPermissionRequiredException
import com.example.campusconnect.core.location.model.LocationFix
import com.example.campusconnect.core.location.model.LocationStatus
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.isActive
import kotlin.random.Random

class LocationTrackingCoordinator(
    private val source: LocationSource,
    private val nowElapsedRealtimeNanos: () -> Long = SystemClock::elapsedRealtimeNanos,
    private val nowTimeMillis: () -> Long = System::currentTimeMillis,
    private val retryDelayMillis: (attempt: Int) -> Long = { attempt ->
        val base = (1_000L shl attempt.coerceAtMost(5)).coerceAtMost(30_000L)
        (base * Random.nextDouble(0.8, 1.2)).toLong().coerceAtLeast(1L)
    },
    private val onFixEvaluated: (mode: TrackingMode, fix: LocationFix, rejection: LocationRejection?) -> Unit = { _, _, _ -> }
) {
    private val _status = MutableStateFlow<LocationStatus>(LocationStatus.Unavailable)
    val status: StateFlow<LocationStatus> = _status.asStateFlow()

    private var trackingJob: Job? = null
    private var lastAcceptedFix: LocationFix? = null
    private var lastAcceptedGeneration: Long? = null
    private var currentMode: TrackingMode? = null
    @Volatile
    private var generation = 0L

    @Synchronized
    fun start(scope: CoroutineScope, mode: TrackingMode) {
        if (trackingJob?.isActive == true && currentMode == mode) return
        val previousJob = trackingJob
        previousJob?.cancel()
        val requestedGeneration = ++generation
        currentMode = mode
        _status.value = LocationStatus.Acquiring
        trackingJob = scope.launch {
            previousJob?.cancelAndJoin()
            if (!currentCoroutineContext().isActive || generation != requestedGeneration) return@launch

            var retryAttempt = 0
            while (currentCoroutineContext().isActive) {
                setStatusIfCurrent(requestedGeneration, LocationStatus.Acquiring)
                try {
                    source.locationUpdates(mode).collect { fix ->
                        if (!isCurrent(requestedGeneration)) return@collect
                        val previousFix = synchronized(this@LocationTrackingCoordinator) {
                            lastAcceptedFix
                        }
                        val nowElapsedRealtimeNanos = nowElapsedRealtimeNanos()
                        val qualityRejection = LocationQualityValidator.rejectionReason(
                            fix = fix,
                            mode = mode,
                            nowElapsedRealtimeNanos = nowElapsedRealtimeNanos,
                            nowTimeMillis = nowTimeMillis(),
                            previous = previousFix
                        )
                        val rejection = qualityRejection
                        onFixEvaluated(mode, fix, rejection)
                        when (rejection) {
                            null -> {
                                retryAttempt = 0
                                acceptFixIfCurrent(requestedGeneration, fix)
                            }
                            LocationRejection.CACHED,
                            LocationRejection.STALE,
                            LocationRejection.OUT_OF_ORDER,
                            LocationRejection.FUTURE -> {
                                // A late cached/out-of-order callback must not downgrade a still-fresh
                                // accepted fix to a stale UI state or interrupt presence uploads.
                                if (!keepAcceptedFixAvailable(requestedGeneration, mode)) {
                                    setStatusIfCurrent(requestedGeneration, LocationStatus.Stale(lastAcceptedFix ?: fix))
                                }
                            }
                            LocationRejection.LOW_ACCURACY -> {
                                if (hasFreshAcceptableFix(mode)) {
                                    setStatusIfCurrent(requestedGeneration, LocationStatus.InsufficientAccuracy(lastAcceptedFix))
                                } else {
                                    // Do not freeze indefinitely if no sufficiently accurate fix
                                    // arrives. Show the actual fresh fix with an explicit quality warning.
                                    setStatusIfCurrent(requestedGeneration, LocationStatus.Approximate(fix))
                                }
                            }
                            LocationRejection.INVALID_COORDINATES, LocationRejection.INVALID_ACCURACY ->
                                setStatusIfCurrent(requestedGeneration, LocationStatus.Error("Received an invalid location fix"))
                        }
                    }
                    if (currentCoroutineContext().isActive) {
                        setStatusIfCurrent(requestedGeneration, LocationStatus.Stale(lastAcceptedFix))
                        kotlinx.coroutines.delay(retryDelayMillis(retryAttempt++))
                    }
                } catch (error: CancellationException) {
                    throw error
                } catch (_: PreciseLocationPermissionRequiredException) {
                    setStatusIfCurrent(requestedGeneration, LocationStatus.PreciseLocationPermissionRequired)
                    break
                } catch (_: SecurityException) {
                    setStatusIfCurrent(requestedGeneration, LocationStatus.PermissionDenied)
                    break
                } catch (_: LocationServicesDisabledException) {
                    setStatusIfCurrent(requestedGeneration, LocationStatus.ServicesDisabled)
                    kotlinx.coroutines.delay(retryDelayMillis(retryAttempt++))
                } catch (error: Throwable) {
                    setStatusIfCurrent(
                        requestedGeneration,
                        LocationStatus.Error(error.message ?: "Unable to obtain location")
                    )
                    kotlinx.coroutines.delay(retryDelayMillis(retryAttempt++))
                }
            }
        }
    }

    @Synchronized
    fun stop() {
        generation += 1
        trackingJob?.cancel()
        currentMode = null
        _status.value = lastAcceptedFix?.let(LocationStatus::Stale) ?: LocationStatus.Unavailable
    }

    fun reportPermissionDenied() {
        _status.value = LocationStatus.PermissionDenied
    }

    private fun isCurrent(requestedGeneration: Long): Boolean = synchronized(this) {
        generation == requestedGeneration
    }

    private fun setStatusIfCurrent(requestedGeneration: Long, status: LocationStatus) {
        synchronized(this) {
            if (generation == requestedGeneration) _status.value = status
        }
    }

    private fun keepAcceptedFixAvailable(requestedGeneration: Long, mode: TrackingMode): Boolean = synchronized(this) {
        if (generation != requestedGeneration) return@synchronized false
        // A fix from the prior mode/provider is provisional only until this subscription
        // accepts a new live fix; it must not look current immediately after a switch.
        if (lastAcceptedGeneration != requestedGeneration) return@synchronized false
        val accepted = lastAcceptedFix ?: return@synchronized false
        val rejection = LocationQualityValidator.rejectionReason(
            fix = accepted,
            mode = mode,
            nowElapsedRealtimeNanos = nowElapsedRealtimeNanos(),
            nowTimeMillis = nowTimeMillis(),
            previous = null
        )
        if (rejection == null) {
            _status.value = LocationStatus.Available(accepted)
            true
        } else {
            false
        }
    }

    private fun hasFreshAcceptableFix(mode: TrackingMode): Boolean = synchronized(this) {
        val accepted = lastAcceptedFix ?: return@synchronized false
        LocationQualityValidator.rejectionReason(
            fix = accepted,
            mode = mode,
            nowElapsedRealtimeNanos = nowElapsedRealtimeNanos(),
            nowTimeMillis = nowTimeMillis(),
            previous = null
        ) == null
    }

    private fun acceptFixIfCurrent(requestedGeneration: Long, fix: LocationFix) {
        synchronized(this) {
            if (generation == requestedGeneration) {
                lastAcceptedFix = fix
                lastAcceptedGeneration = requestedGeneration
                _status.value = LocationStatus.Available(fix)
            }
        }
    }

}
