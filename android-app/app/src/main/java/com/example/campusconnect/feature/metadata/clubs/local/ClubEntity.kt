package com.example.campusconnect.feature.metadata.clubs.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "clubs")
data class ClubEntity(
    @PrimaryKey
    val clubId: Int,
    val name: String
)
