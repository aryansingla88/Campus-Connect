package com.example.campusconnect.feature.metadata.eventcategories.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface EventCategoryDao {

    @Query("SELECT * FROM event_categories ORDER BY name ASC")
    suspend fun getAllEventCategories(): List<EventCategoryEntity>

    @Query("SELECT * FROM event_categories WHERE id = :id")
    suspend fun getEventCategoryById(id: Int): EventCategoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEventCategories(
        categories: List<EventCategoryEntity>
    )

    @Query("DELETE FROM event_categories")
    suspend fun clearEventCategories()
}
