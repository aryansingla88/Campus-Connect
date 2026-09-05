package com.example.campusconnect.feature.map.data.repo

import com.example.campusconnect.feature.map.mapengine.model.MapMarker
import com.example.campusconnect.feature.map.mapengine.model.MarkerType
import com.example.campusconnect.feature.map.model.MapEventInfo
import com.example.campusconnect.feature.map.model.MapPoiInfo
import com.example.campusconnect.feature.map.model.MapShopInfo
import com.example.campusconnect.feature.map.model.MapUserProfile

interface MapRepo {

    suspend fun getMarkers(
        type: MarkerType? = null
    ): Result<List<MapMarker>>

    // Backend user ID
    suspend fun getUserProfile(
        userId: Int
    ): Result<MapUserProfile>

    // Backend POI ID
    suspend fun getPoiInfo(
        poiId: Int,
        fallbackName: String = ""
    ): Result<MapPoiInfo>

    // Backend event ID
    suspend fun getEventInfo(
        eventId: Int
    ): Result<MapEventInfo>

    // Temporary shop ID
    suspend fun getShopInfo(
        shopId: Int
    ): Result<MapShopInfo>

    // Backend user ID
    suspend fun sendConnectionRequest(
        userId: Int
    ): Result<Unit>

    // Backend event ID
    suspend fun registerEvent(
        eventId: Int
    ): Result<Unit>

    // Backend event ID
    suspend fun enableEventReminder(
        eventId: Int
    ): Result<Unit>

    // Backend event ID
    suspend fun disableEventReminder(
        eventId: Int
    ): Result<Unit>


}