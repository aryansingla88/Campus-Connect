package com.example.campusconnect.feature.metadata.hostels.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo

@Entity(tableName = "hostels")
data class HostelEntity(
    @PrimaryKey val id: Int,
    val name: String,
    @ColumnInfo(defaultValue = "''") val hostel: String,
    @ColumnInfo(defaultValue = "1") val active: Boolean
)
