package com.example.campusconnect.feature.events.mapper

import com.example.campusconnect.core.network.ApiConfig
import com.example.campusconnect.feature.events.data.remote.response.EventResponse
import com.example.campusconnect.feature.events.model.Event
import com.example.campusconnect.feature.events.model.EventStatus
import com.example.campusconnect.feature.map.mapengine.MapCalibration
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

private const val MAP_IMAGE_WIDTH = 3000f
private const val MAP_IMAGE_HEIGHT = 3000f

fun EventResponse.toEvent(): Event {

    // ---------------------------------------------------------
    // LOCATION
    // Backend latitude/longitude → frontend map xRatio/yRatio
    // ---------------------------------------------------------

    val mapPoint = MapCalibration.converter.latLngToPoint(
        lat = latitude,
        lng = longitude
    )

    val xRatio = mapPoint.x / MAP_IMAGE_WIDTH
    val yRatio = mapPoint.y / MAP_IMAGE_HEIGHT


    // ---------------------------------------------------------
    // TIME
    // Backend sends ISO-8601 Instant strings
    // Android min SDK = 21, so don't use java.time here
    // ---------------------------------------------------------

    val startDate = parseBackendDate(startTime)

    val date = startDate?.let {
        SimpleDateFormat(
            "dd MMM yyyy",
            Locale.getDefault()
        ).format(it)
    } ?: ""

    val formattedStartTime = startDate?.let {
        SimpleDateFormat(
            "hh:mm a",
            Locale.getDefault()
        ).format(it)
    } ?: startTime

    val formattedEndTime = endTime?.let {
        parseBackendDate(it)?.let { parsed ->
            SimpleDateFormat(
                "hh:mm a",
                Locale.getDefault()
            ).format(parsed)
        } ?: it
    }


    // ---------------------------------------------------------
    // EVENT STATE
    // Backend:
    // UPCOMING, ONGOING, COMPLETED, LIVE, CANCELLED
    //
    // Frontend:
    // LIVE, PAST, UPCOMING
    // ---------------------------------------------------------

    val status = when (eventState.uppercase()) {
        "LIVE",
        "ONGOING" -> EventStatus.LIVE

        "COMPLETED",
        "CANCELLED" -> EventStatus.PAST

        "UPCOMING" -> EventStatus.UPCOMING

        else -> EventStatus.UPCOMING
    }


    // ---------------------------------------------------------
    // REGISTRATION
    // ---------------------------------------------------------

    val registrationTypeUpper = registrationType.uppercase()

    val registrationRequired =
        registrationTypeUpper != "NONE"

    val inAppRegistration =
        registrationTypeUpper == "THROUGH_APP"


    // ---------------------------------------------------------
    // CLUB / HOST
    //
    // If club exists:
    //     clubName will later come from cached club data.
    //
    // If no club:
    //     backend hostName can be used directly.
    // ---------------------------------------------------------

    val frontendClubName =
        if (clubId == null) {
            hostName
        } else {
            ""
        }


    // ---------------------------------------------------------
    // FINAL FRONTEND MODEL
    // ---------------------------------------------------------

    return Event(

        // BASIC
        id = id,
        title = title,
        description = description,

        // LOCATION
        latitude = latitude,
        longitude = longitude,
        xRatio = xRatio,
        yRatio = yRatio,

        // TIME
        date = date,
        startTime = formattedStartTime,
        endTime = formattedEndTime,

        // ORGANIZATION
        createdBy = createdBy,
        clubName = frontendClubName,

        // MEDIA
        isPoster = posterUrl != null,
        posterUrl = posterUrl?.let { url ->
            if (url.startsWith("http")) {
                url
            } else {
                ApiConfig.BASE_URL.removeSuffix("/") +
                        "/" +
                        url.removePrefix("/")
            }
        },

        // CLASSIFICATION
        category = categoryName ?: "",

        // VISIBILITY
        visibilityType = visibilityType,
        visibilityValue = visibilityValue ?: "",

        // REGISTRATION
        registrationRequired = registrationRequired,
        registrationLink = registrationLink ?: "",
        inAppRegistration = inAppRegistration,

        // EXTRA
        venue = venue,

        // UI-only field; backend currently doesn't send this
        enableChat = false,

        // STATUS
        status = status
    )
}


// =============================================================
// BACKEND DATE PARSER
// =============================================================
//
// Handles common Jackson Instant formats such as:
//
// 2026-09-06T10:30:00Z
// 2026-09-06T10:30:00.000Z
// 2026-09-06T10:30:00+05:30
// 2026-09-06T10:30:00.000+05:30
//
// No java.time → works with min SDK 21.
// =============================================================

private fun parseBackendDate(value: String): Date? {

    val formats = listOf(
        "yyyy-MM-dd'T'HH:mm:ss.SSSX",
        "yyyy-MM-dd'T'HH:mm:ssX",
        "yyyy-MM-dd'T'HH:mm:ss.SSSZ",
        "yyyy-MM-dd'T'HH:mm:ssZ"
    )

    for (pattern in formats) {

        try {

            val formatter = SimpleDateFormat(
                pattern,
                Locale.US
            )

            formatter.timeZone = TimeZone.getDefault()

            val parsed = formatter.parse(value)

            if (parsed != null) {
                return parsed
            }

        } catch (_: Exception) {
            // Try next supported format
        }
    }

    return null
}