package com.example.campusconnect.feature.metadata.hostels.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction

@Dao
interface HostelDao {
    @Query("SELECT * FROM hostels ORDER BY name ASC")
    suspend fun getAllHostels(): List<HostelEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHostels(hostels: List<HostelEntity>)

    @Query("DELETE FROM hostels")
    suspend fun clearHostels()

    @Transaction
    suspend fun replaceHostels(hostels: List<HostelEntity>) {
        clearHostels()
        insertHostels(hostels)
    }
}
