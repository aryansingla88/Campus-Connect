package com.example.campusconnect.feature.map.ui

import com.example.campusconnect.feature.map.model.EventMarkerData
import com.example.campusconnect.feature.map.model.LocalUserMarkerData
import com.example.campusconnect.feature.map.model.PoiMarkerData
import com.example.campusconnect.feature.map.model.UserMarkerData
import org.junit.Assert.assertEquals
import org.junit.Test

class AuthenticatedUserMarkerFilterTest {
    @Test
    fun excludesOnlyAuthenticatedUserAndPreservesOtherMarkers() {
        val ownMarker = UserMarkerData(sourceId = 41, latitude = 30.0, longitude = 76.0, username = "same")
        val otherUser = UserMarkerData(sourceId = 42, latitude = 30.1, longitude = 76.1, username = "same")
        val poi = PoiMarkerData(
            sourceId = 7, latitude = 30.2, longitude = 76.2, name = "Library",
            category = null, iconType = null, visibility = null, priority = 1
        )
        val event = EventMarkerData(sourceId = 8, latitude = 30.3, longitude = 76.3, title = "Talk", priority = 1)
        val localDevice = LocalUserMarkerData(
            latitude = 30.4, longitude = 76.4, isStale = false, mapX = 10f, mapY = 20f
        )

        assertEquals(
            listOf(otherUser, poi, event, localDevice),
            filterRemoteUserMarkers(
                listOf(ownMarker, otherUser, poi, event, localDevice),
                authenticatedUserId = 41,
                authenticatedUsername = "same"
            )
        )
        assertEquals("USER_42", otherUser.id)
        assertEquals("same", otherUser.label)
    }

    @Test
    fun usesUsernameOnlyWhenNoReliableIdIsAvailable() {
        val users = listOf(
            UserMarkerData(sourceId = 41, latitude = 30.0, longitude = 76.0, username = "You"),
            UserMarkerData(sourceId = 42, latitude = 30.1, longitude = 76.1, username = "Other")
        )

        assertEquals(
            listOf(users.last()),
            filterRemoteUserMarkers(users, authenticatedUserId = null, authenticatedUsername = "you")
        )
    }

    @Test
    fun suppressesRemoteProfilesWhenIdentityCannotBeResolved() {
        val users = listOf(
            UserMarkerData(sourceId = 41, latitude = 30.0, longitude = 76.0, username = "You"),
            UserMarkerData(sourceId = 42, latitude = 30.1, longitude = 76.1, username = "Other")
        )

        assertEquals(emptyList<UserMarkerData>(), filterRemoteUserMarkers(users, null, null))
    }
}
