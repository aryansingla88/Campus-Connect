package com.example.campusconnect.core.util

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

fun formatBackendDateTime(dateTime: String): String {

    return try {

        val parsedDateTime =
            LocalDateTime.parse(dateTime)

        val formatter =
            DateTimeFormatter.ofPattern(
                "d MMMM | h:mm a",
                Locale.ENGLISH
            )

        parsedDateTime
            .format(formatter)
            .lowercase(Locale.ENGLISH)

    } catch (e: Exception) {

        dateTime
    }
}