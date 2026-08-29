package com.example.campusconnect.feature.map.data.repo

import com.example.campusconnect.feature.map.mapengine.MapMarker
import com.example.campusconnect.feature.map.mapengine.MarkerType
import com.example.campusconnect.feature.map.model.MapEventInfo
import com.example.campusconnect.feature.map.model.MapPoiInfo
import com.example.campusconnect.feature.map.model.MapShopInfo
import com.example.campusconnect.feature.map.model.MapUserProfile

interface MapRepo {

    suspend fun getMarkers(
        type: MarkerType? = null
    ): Result<List<MapMarker>>

    // User Profile & Connections (Int IDs)
    suspend fun getUserProfile(
        userId: Int
    ): Result<MapUserProfile>

    suspend fun sendConnectionRequest(
        userId: Int
    ): Result<Unit>

    // POI Card Details (Int ID)
    suspend fun getPoiInfo(
        poiId: Int,
        fallbackName: String = ""
    ): Result<MapPoiInfo>

    // Event Card Details & Interactions (Int IDs)
    suspend fun getEventInfo(
        eventId: Int
    ): Result<MapEventInfo>

    suspend fun registerEvent(
        eventId: Int
    ): Result<Unit>

    suspend fun enableEventReminder(
        eventId: Int
    ): Result<Unit>

    suspend fun disableEventReminder(
        eventId: Int
    ): Result<Unit>

    // Shop Card Details (Int ID)
    suspend fun getShopInfo(
        shopId: Int
    ): Result<MapShopInfo>
}