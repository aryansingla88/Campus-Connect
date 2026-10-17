package com.example.campusconnect.feature.map.ui.util

import java.text.SimpleDateFormat
import java.util.Locale

private val isoDateTimeRegex =
    Regex(
        """\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}(?:\.\d+)?(?:Z|[+-]\d{2}:?\d{2})?"""
    )


private val isoTimeOfDayRegex =
    Regex(
        """T(\d{2}):(\d{2})"""
    )


fun formatEventDate(
    raw: String
): String {

    if (raw.isBlank()) {
        return "Date unavailable"
    }

    val inputPatterns = listOf(
        "yyyy-MM-dd'T'HH:mm:ss",
        "yyyy-MM-dd",
        "yyyy/MM/dd",
        "dd-MM-yyyy",
        "dd/MM/yyyy",
        "MM-dd-yyyy",
        "MM/dd/yyyy"
    )

    for (pattern in inputPatterns) {

        try {

            val parser =
                SimpleDateFormat(
                    pattern,
                    Locale.getDefault()
                )

            parser.isLenient = false

            val parsed =
                parser.parse(
                    raw.trim()
                ) ?: continue

            val output =
                SimpleDateFormat(
                    "dd MMM yyyy",
                    Locale.getDefault()
                )

            return output.format(parsed)

        } catch (_: Exception) {
            // Try next pattern.
        }
    }

    return raw
}


// ============================================================================
// TIME FORMAT
// ============================================================================

private fun to12Hour(
    hour24: Int,
    minute: Int
): String {

    val period =
        if (hour24 < 12) {
            "AM"
        } else {
            "PM"
        }

    val hour12Raw =
        hour24 % 12

    val hour12 =
        if (hour12Raw == 0) {
            12
        } else {
            hour12Raw
        }

    return String.format(
        Locale.getDefault(),
        "%02d:%02d %s",
        hour12,
        minute,
        period
    )
}


private fun extractIsoTime(
    candidate: String
): String? {

    val match =
        isoTimeOfDayRegex.find(
            candidate
        ) ?: return null

    val hour =
        match.groupValues[1]
            .toIntOrNull()
            ?: return null

    val minute =
        match.groupValues[2]
            .toIntOrNull()
            ?: return null

    return to12Hour(
        hour,
        minute
    )
}


private fun formatSingleTime(
    raw: String
): String? {

    extractIsoTime(raw)?.let {
        return it
    }

    val inputPatterns = listOf(
        "HH:mm",
        "H:mm",
        "hh:mm a",
        "h:mm a"
    )

    for (pattern in inputPatterns) {

        try {

            val parser =
                SimpleDateFormat(
                    pattern,
                    Locale.getDefault()
                )

            parser.isLenient = false

            val parsed =
                parser.parse(
                    raw.trim()
                ) ?: continue

            val output =
                SimpleDateFormat(
                    "hh:mm a",
                    Locale.getDefault()
                )

            return output.format(parsed)

        } catch (_: Exception) {
            // Try next pattern.
        }
    }

    return null
}


fun formatEventTimeRange(
    raw: String,
    endTimeRaw: String?
): String {

    if (raw.isBlank()) {
        return "Time unavailable"
    }

    if (!endTimeRaw.isNullOrBlank()) {

        val start =
            formatSingleTime(raw)

        val end =
            formatSingleTime(
                endTimeRaw
            )

        if (
            start != null &&
            end != null
        ) {
            return "$start – $end"
        }
    }

    val isoMatches =
        isoDateTimeRegex
            .findAll(raw)
            .map {
                it.value
            }
            .toList()

    if (isoMatches.size >= 2) {

        val start =
            extractIsoTime(
                isoMatches[0]
            )

        val end =
            extractIsoTime(
                isoMatches[1]
            )

        if (
            start != null &&
            end != null
        ) {
            return "$start – $end"
        }
    }

    if (isoMatches.isEmpty()) {

        val separators = listOf(
            "–",
            " to ",
            "-"
        )

        for (separator in separators) {

            val split =
                raw.split(separator)
                    .map {
                        it.trim()
                    }

            if (
                split.size == 2 &&
                split.all {
                    it.isNotBlank()
                }
            ) {

                val start =
                    formatSingleTime(
                        split[0]
                    )

                val end =
                    formatSingleTime(
                        split[1]
                    )

                if (
                    start != null &&
                    end != null
                ) {
                    return "$start – $end"
                }
            }
        }
    }

    if (isoMatches.size == 1) {

        extractIsoTime(
            isoMatches[0]
        )?.let {
            return it
        }
    }

    formatSingleTime(raw)?.let {
        return it
    }

    return raw
}
