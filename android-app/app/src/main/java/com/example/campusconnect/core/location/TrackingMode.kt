package com.example.campusconnect.core.location

enum class TrackingMode(
    val storageValue: String,
    val intervalMillis: Long,
    val minUpdateIntervalMillis: Long,
    val maxAgeMillis: Long,
    val maxAccuracyMeters: Float,
    val minMovementMeters: Float,
    val heartbeatMillis: Long
) {
    BALANCED(
        storageValue = "balanced",
        intervalMillis = 15_000L,
        minUpdateIntervalMillis = 7_500L,
        maxAgeMillis = 30_000L,
        maxAccuracyMeters = 100f,
        minMovementMeters = 25f,
        heartbeatMillis = 120_000L
    ),
    HIGH_PRECISION(
        storageValue = "high_precision",
        intervalMillis = 1_000L,
        minUpdateIntervalMillis = 1_000L,
        maxAgeMillis = 10_000L,
        maxAccuracyMeters = 25f,
        minMovementMeters = 10f,
        heartbeatMillis = 45_000L
    );

    companion object {
        fun fromStorageValue(value: String?): TrackingMode =
            entries.firstOrNull { it.storageValue == value } ?: BALANCED
    }
}
