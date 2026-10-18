package com.example.campusconnect.core.location.android

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Looper
import android.os.SystemClock
import android.util.Log
import androidx.core.content.ContextCompat
import com.example.campusconnect.core.location.LocationSource
import com.example.campusconnect.core.location.PreciseLocationPermissionRequiredException
import com.example.campusconnect.core.location.TrackingMode
import com.example.campusconnect.core.location.model.LocationFix
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.Task
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.coroutines.withContext
import kotlinx.coroutines.NonCancellable
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class LocationServicesDisabledException(message: String = "Device location services are disabled") :
    IllegalStateException(message)

class FusedLocationSource(context: Context) : LocationSource {
    private val appContext = context.applicationContext
    private val client = LocationServices.getFusedLocationProviderClient(appContext)

    override fun locationUpdates(mode: TrackingMode): Flow<LocationFix> = callbackFlow {
        val sourceStartedAtNanos = SystemClock.elapsedRealtimeNanos()
        val hasFinePermission = hasFineLocationPermission()
        val hasCoarsePermission = hasCoarseLocationPermission()
        Log.d(TAG, "FLP source selected mode=$mode fine=$hasFinePermission coarse=$hasCoarsePermission")
        if (mode == TrackingMode.HIGH_PRECISION && !hasFinePermission) {
            close(PreciseLocationPermissionRequiredException())
            return@callbackFlow
        }
        if (!hasFinePermission && !hasCoarsePermission) {
            close(SecurityException("Location permission has not been granted"))
            return@callbackFlow
        }
        if (!hasFinePermission) {
            Log.w(TAG, "Only approximate location permission is granted for mode=$mode; expected accuracy may not be met")
        }
        if (!isLocationEnabled()) {
            Log.w(TAG, "FLP unavailable mode=$mode reason=location_services_disabled")
            close(LocationServicesDisabledException())
            return@callbackFlow
        }

        val priority = locationPriorityFor(mode)
        val request = buildLocationRequest(mode)
        Log.d(TAG, "location provider=fused mode=$mode priority=$priority intervalMs=${mode.intervalMillis}")
        var updateRequestStartedAtNanos = 0L
        val emissionLock = Any()
        var freshFixEmitted = false

        // A delayed lastLocation result must never overwrite a fresh callback in the flow buffer.
        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.locations.forEach { location ->
                    val fix = location.toLocationFix(
                        isCached = isFixFromBeforeRequest(location.elapsedRealtimeNanos, updateRequestStartedAtNanos)
                    )
                    val sendResult = synchronized(emissionLock) {
                        if (!fix.isCached) freshFixEmitted = true
                        trySend(fix)
                    }
                    Log.d(
                        TAG,
                        "FLP callback mode=$mode cached=${fix.isCached} ageMs=${fix.ageMillis()} accuracyM=${fix.accuracyMeters} trackingMs=${elapsedSince(sourceStartedAtNanos)} queued=${sendResult.isSuccess}"
                    )
                    if (sendResult.isFailure) Log.w(TAG, "FLP callback could not reach coordinator mode=$mode")
                }
            }
        }

        val cancellationTokenSource = CancellationTokenSource()
        var registrationTask: Task<Void>? = null
        var registrationSucceeded = false
        var stopReason = "source_completed"
        var firstFixWatchdog: kotlinx.coroutines.Job? = null
        try {
            client.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    val fix = location.toLocationFix(isCached = true)
                    synchronized(emissionLock) {
                        if (!freshFixEmitted) {
                            Log.d(TAG, "FLP cached snapshot ageMs=${fix.ageMillis()} accuracyM=${fix.accuracyMeters}")
                            trySend(fix)
                        }
                    }
                } else {
                    Log.d(TAG, "FLP last-known lookup returned no fix mode=$mode")
                }
            }.addOnFailureListener { error ->
                Log.w(TAG, "FLP last-known lookup failed mode=$mode error=${error.javaClass.simpleName}")
            }

            updateRequestStartedAtNanos = android.os.SystemClock.elapsedRealtimeNanos()
            Log.d(TAG, "FLP registration requested mode=$mode priority=$priority intervalMs=${mode.intervalMillis}")
            registrationTask = client.requestLocationUpdates(request, callback, Looper.getMainLooper())
            val registrationCompleted = withTimeoutOrNull(REQUEST_REGISTRATION_TIMEOUT_MILLIS) {
                registrationTask.awaitResult()
                true
            } ?: false
            if (!registrationCompleted) {
                throw LocationRegistrationTimeoutException()
            }
            registrationSucceeded = true
            Log.d(TAG, "FLP registration succeeded mode=$mode priority=$priority intervalMs=${mode.intervalMillis} minIntervalMs=${mode.minUpdateIntervalMillis}")
            firstFixWatchdog = launch {
                delay(FIRST_FRESH_FIX_WARNING_MILLIS)
                if (synchronized(emissionLock) { !freshFixEmitted }) {
                    Log.w(TAG, "FLP registered but no fresh callback after ${FIRST_FRESH_FIX_WARNING_MILLIS}ms mode=$mode")
                }
            }

            // Request an immediate fresh fix after continuous updates are registered.
            val currentRequest = CurrentLocationRequest.Builder()
                .setPriority(priority)
                .setMaxUpdateAgeMillis(0L)
                .build()
            val currentFixRequestStartedAtNanos = SystemClock.elapsedRealtimeNanos()
            client.getCurrentLocation(currentRequest, cancellationTokenSource.token)
                .addOnSuccessListener { location ->
                    if (location != null) {
                        val fix = location.toLocationFix(
                            isCached = isFixFromBeforeRequest(
                                location.elapsedRealtimeNanos,
                                currentFixRequestStartedAtNanos
                            )
                        )
                        synchronized(emissionLock) {
                            if (!fix.isCached) freshFixEmitted = true
                            val sendResult = trySend(fix)
                            Log.d(TAG, "FLP current fix mode=$mode cached=${fix.isCached} ageMs=${fix.ageMillis()} accuracyM=${fix.accuracyMeters} queued=${sendResult.isSuccess}")
                            if (sendResult.isFailure) Log.w(TAG, "FLP current fix could not reach coordinator mode=$mode")
                        }
                    } else {
                        Log.w(TAG, "FLP current-fix request returned no fix mode=$mode")
                    }
                }
                .addOnFailureListener { error ->
                    Log.w(TAG, "FLP current-fix request failed mode=$mode: ${error.javaClass.simpleName}")
                }
            awaitClose {
                stopReason = "collector_cancelled_or_closed"
                cancellationTokenSource.cancel()
            }
        } catch (error: SecurityException) {
            stopReason = "security_exception"
            Log.w(TAG, "FLP request failed mode=$mode error=${error.javaClass.simpleName}")
            close(error)
        } catch (error: CancellationException) {
            stopReason = "cancelled_or_mode_switched"
            throw error
        } catch (error: Throwable) {
            stopReason = "registration_or_provider_error"
            Log.e(TAG, "FLP request failed mode=$mode error=${error.javaClass.simpleName}")
            close(error)
        } finally {
            firstFixWatchdog?.cancel()
            cancellationTokenSource.cancel()
            withContext(NonCancellable) {
                val task = registrationTask
                if (registrationSucceeded && task != null) {
                    removeListener(callback, mode, stopReason)
                } else if (task != null) {
                    // Registration may finish after cancellation/timeout. Remove the callback
                    // then, without keeping the next mode blocked on the unresolved task.
                    task.addOnSuccessListener {
                        requestListenerRemoval(callback, mode, "late_registration_after_$stopReason")
                    }.addOnFailureListener { error ->
                        Log.w(TAG, "FLP registration ended without listener mode=$mode error=${error.javaClass.simpleName}")
                    }
                    Log.w(TAG, "FLP registration unresolved at stop; late cleanup scheduled mode=$mode reason=$stopReason")
                }
            }
        }
    }.buffer(capacity = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)

    private fun hasFineLocationPermission(): Boolean =
        ContextCompat.checkSelfPermission(appContext, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED

    private fun hasCoarseLocationPermission(): Boolean =
        ContextCompat.checkSelfPermission(appContext, Manifest.permission.ACCESS_COARSE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED

    @Suppress("DEPRECATION")
    private fun isLocationEnabled(): Boolean {
        val manager = appContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            manager.isLocationEnabled
        } else {
            manager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                    manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        }
    }

    private fun Location.toLocationFix(isCached: Boolean = false) = LocationFix(
        latitude = latitude,
        longitude = longitude,
        accuracyMeters = accuracy,
        timeMillis = time,
        elapsedRealtimeNanos = elapsedRealtimeNanos,
        provider = provider,
        isCached = isCached
    )

    private fun LocationFix.ageMillis(): Long =
        ((android.os.SystemClock.elapsedRealtimeNanos() - elapsedRealtimeNanos).coerceAtLeast(0L) / 1_000_000L)

    private suspend fun removeListener(
        callback: LocationCallback,
        mode: TrackingMode,
        reason: String
    ) {
        val removalTask = requestListenerRemoval(callback, mode, reason) ?: return
        val removalCompleted = withTimeoutOrNull(REQUEST_REMOVAL_WAIT_MILLIS) {
            removalTask.awaitResult()
            true
        } ?: false
        if (!removalCompleted) {
            Log.w(TAG, "FLP unregistration still pending; continuing cleanup mode=$mode reason=$reason")
        }
    }

    private fun requestListenerRemoval(
        callback: LocationCallback,
        mode: TrackingMode,
        reason: String
    ): Task<Void>? {
        return runCatching { client.removeLocationUpdates(callback) }
            .onFailure { error ->
                Log.w(TAG, "FLP unregistration request failed mode=$mode error=${error.javaClass.simpleName}")
            }
            .getOrNull()
            ?.also { removalTask ->
                removalTask.addOnSuccessListener {
                    Log.d(TAG, "FLP listener removed mode=$mode reason=$reason")
                }.addOnFailureListener { error ->
                    Log.w(TAG, "FLP unregistration failed mode=$mode error=${error.javaClass.simpleName}")
                }
            }
    }

    private companion object {
        const val TAG = "CampusLocation"
        const val FIRST_FRESH_FIX_WARNING_MILLIS = 30_000L
        const val REQUEST_REGISTRATION_TIMEOUT_MILLIS = 10_000L
        const val REQUEST_REMOVAL_WAIT_MILLIS = 2_000L
    }
}

class LocationRegistrationTimeoutException :
    IllegalStateException("Fused location request registration timed out")

private suspend fun <T> Task<T>.awaitResult(): T = suspendCancellableCoroutine { continuation ->
    addOnSuccessListener { value ->
        if (continuation.isActive) continuation.resume(value)
    }
    addOnFailureListener { error ->
        if (continuation.isActive) continuation.resumeWithException(error)
    }
    addOnCanceledListener {
        if (continuation.isActive) continuation.cancel(CancellationException("Google Play services task was canceled"))
    }
}

internal fun locationPriorityFor(mode: TrackingMode): Int = when (mode) {
    TrackingMode.BALANCED -> com.google.android.gms.location.Priority.PRIORITY_BALANCED_POWER_ACCURACY
    TrackingMode.HIGH_PRECISION -> com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY
}

internal fun buildLocationRequest(mode: TrackingMode): LocationRequest =
    LocationRequest.Builder(locationPriorityFor(mode), mode.intervalMillis)
        .setMinUpdateIntervalMillis(mode.minUpdateIntervalMillis)
        .setMinUpdateDistanceMeters(0f)
        // This is an interactive map marker; do not batch updates beyond the requested interval.
        .setMaxUpdateDelayMillis(mode.intervalMillis)
        // Let initial lower-confidence fixes reach the validator/status UI while FLP improves them.
        .setWaitForAccurateLocation(false)
        .build()

internal fun isFixFromBeforeRequest(fixElapsedRealtimeNanos: Long, requestStartedAtNanos: Long): Boolean =
    fixElapsedRealtimeNanos < requestStartedAtNanos

private fun elapsedSince(startedAtNanos: Long): Long =
    ((SystemClock.elapsedRealtimeNanos() - startedAtNanos).coerceAtLeast(0L) / 1_000_000L)
