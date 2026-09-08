package com.example.campusconnect.core.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.campusconnect.feature.metadata.courses.local.CourseDao
import com.example.campusconnect.feature.metadata.courses.local.CourseEntity
import com.example.campusconnect.feature.metadata.clubs.local.ClubDao
import com.example.campusconnect.feature.metadata.clubs.local.ClubEntity
import com.example.campusconnect.feature.metadata.eventcategories.local.EventCategoryDao
import com.example.campusconnect.feature.metadata.eventcategories.local.EventCategoryEntity

@Database(
    entities = [
        CourseEntity::class,
        ClubEntity::class,
        EventCategoryEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun courseDao(): CourseDao

    abstract fun clubDao(): ClubDao

    abstract fun eventCategoryDao(): EventCategoryDao

    companion object {

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "campus_connect_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}