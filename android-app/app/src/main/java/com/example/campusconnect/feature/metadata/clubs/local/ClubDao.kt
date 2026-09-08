package com.example.campusconnect.feature.metadata.clubs.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ClubDao {

    @Query("SELECT * FROM clubs ORDER BY name ASC")
    suspend fun getAllClubs(): List<ClubEntity>

    @Query("SELECT name FROM clubs WHERE clubId = :clubId")
    suspend fun getClubNameById(clubId: Int): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClubs(clubs: List<ClubEntity>)

    @Query("DELETE FROM clubs")
    suspend fun clearClubs()
}