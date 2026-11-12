package com.example.campusconnect.feature.posts.utils

import com.example.campusconnect.core.util.formatBackendDateTime
import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

fun formatPostTimestamp(createdAt: String, now: LocalDateTime = LocalDateTime.now()): String {
    val created = parsePostTimestamp(createdAt) ?: return formatBackendDateTime(createdAt)
    val age = Duration.between(created, now)
    val seconds = age.seconds

    return when {
        seconds < 60 -> "Just now"
        seconds < 60 * 60 -> "${age.toMinutes()} min ago"
        seconds < 24 * 60 * 60 -> "${age.toHours()} hr ago"
        seconds < 7 * 24 * 60 * 60 -> "${age.toDays()} days ago"
        seconds < 30 * 24 * 60 * 60 -> "${age.toDays() / 7} weeks ago"
        else -> {
            val pattern = if (created.year != now.year) "d MMM yyyy" else "d MMM"
            created.format(DateTimeFormatter.ofPattern(pattern, Locale.ENGLISH))
        }
    }
}

private fun parsePostTimestamp(value: String): LocalDateTime? =
    runCatching { LocalDateTime.parse(value) }.getOrNull()
        ?: runCatching {
            OffsetDateTime.parse(value)
                .atZoneSameInstant(ZoneId.systemDefault())
                .toLocalDateTime()
        }.getOrNull()
        ?: runCatching {
            Instant.parse(value)
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime()
        }.getOrNull()
