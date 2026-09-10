package com.example.campusconnect.feature.metadata.eventcategories.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "event_categories")
data class EventCategoryEntity(
    @PrimaryKey
    val id: Int,
    val name: String
)
