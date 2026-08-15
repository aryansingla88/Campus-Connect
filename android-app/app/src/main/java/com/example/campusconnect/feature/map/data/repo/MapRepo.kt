package com.example.campusconnect.feature.map.data.repo

import com.example.campusconnect.feature.map.mapengine.MapMarker
import com.example.campusconnect.feature.map.mapengine.MarkerType
import com.example.campusconnect.feature.map.model.MapEventInfo
import com.example.campusconnect.feature.map.model.MapPoiInfo
import com.example.campusconnect.feature.map.model.MapShopInfo
import com.example.campusconnect.feature.map.model.MapUserProfile

interface MapRepo {

    // Filter-based Marker Fetching (Search parameter removed)
    suspend fun getMarkers(
        type: MarkerType? = null
    ): Result<List<MapMarker>>

    // User Preview Card & Social Actions
    suspend fun getUserProfile(
        userId: String
    ): Result<MapUserProfile>

    suspend fun sendConnectionRequest(
        userId: String
    ): Result<Unit>

    // POI Card Details
    suspend fun getPoiInfo(
        poiId: String,
        fallbackName: String = ""
    ): Result<MapPoiInfo>

    // Event Card Details & Interactions
    suspend fun getEventInfo(
        eventId: String
    ): Result<MapEventInfo>

    suspend fun registerEvent(
        eventId: String
    ): Result<Unit>

    suspend fun enableEventReminder(
        eventId: String
    ): Result<Unit>

    suspend fun disableEventReminder(
        eventId: String
    ): Result<Unit>

    // Shop Card Details
    suspend fun getShopInfo(
        shopId: String
    ): Result<MapShopInfo>
}