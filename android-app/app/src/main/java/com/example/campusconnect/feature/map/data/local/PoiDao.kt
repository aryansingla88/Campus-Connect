package com.example.campusconnect.feature.map.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction

@Dao
interface PoiDao {
    @Query("SELECT * FROM map_pois ORDER BY name ASC")
    suspend fun getAll(): List<PoiEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(pois: List<PoiEntity>)

    @Query("DELETE FROM map_pois")
    suspend fun clear()

    @Transaction
    suspend fun replaceAll(pois: List<PoiEntity>) {
        clear()
        if (pois.isNotEmpty()) insertAll(pois)
    }
}
