package com.example.campusconnect.feature.posts.utils

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private fun formatEventTime(value: String, pattern: String): String = try {
    DateTimeFormatter.ofPattern(pattern, Locale.getDefault())
        .withZone(ZoneId.systemDefault())
        .format(Instant.parse(value))
} catch (_: Exception) { value }

fun formatEventDate(value: String): String = formatEventTime(value, "MMM d, yyyy")

fun formatEventDateWithoutYear(value: String): String = formatEventTime(value, "MMM d")

fun formatEventClockTime(value: String): String = formatEventTime(value, "h:mm a")

fun formatEventDateTime(value: String): String =
    "${formatEventDate(value)} • ${formatEventClockTime(value)}"
