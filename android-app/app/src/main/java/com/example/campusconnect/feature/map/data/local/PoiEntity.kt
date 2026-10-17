package com.example.campusconnect.feature.map.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Base map POI metadata. Shop and Lost & Found records are stored separately. */
@Entity(tableName = "map_pois")
data class PoiEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val category: String?,
    val description: String?,
    val latitude: Double?,
    val longitude: Double?,
    val iconType: String?,
    val visibility: String?,
    val priority: Int?
)
