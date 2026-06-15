package com.example.campusconnect.feature.map.data.repo

import com.example.campusconnect.feature.map.mapengine.MapMarker
import com.example.campusconnect.feature.map.model.MapEventInfo
import com.example.campusconnect.feature.map.model.MapPoiInfo
import com.example.campusconnect.feature.map.model.MapUserProfile

interface MapRepo {

    suspend fun getMarkers(): Result<List<MapMarker>>

    suspend fun getUserProfile(
        userId: String
    ): Result<MapUserProfile>

    suspend fun getPoiInfo(
        poiId: String
    ): Result<MapPoiInfo>

    suspend fun getEventInfo(
        eventId: String
    ): Result<MapEventInfo>

    suspend fun sendConnectionRequest(
        userId: String
    ): Result<Unit>

    suspend fun registerEvent(
        eventId: String
    ): Result<Unit>

    suspend fun enableEventReminder(
        eventId: String
    ): Result<Unit>
}