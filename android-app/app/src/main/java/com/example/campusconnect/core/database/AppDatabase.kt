package com.example.campusconnect.core.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.campusconnect.feature.map.data.local.PoiDao
import com.example.campusconnect.feature.map.data.local.PoiEntity
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
        EventCategoryEntity::class,
        PoiEntity::class
    ],
    version = 6,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun courseDao(): CourseDao

    abstract fun clubDao(): ClubDao

    abstract fun eventCategoryDao(): EventCategoryDao

    abstract fun poiDao(): PoiDao

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
                    .addMigrations(MIGRATION_4_6, MIGRATION_5_6)
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
        }

        internal val MIGRATION_4_6 = object : Migration(4, 6) {
            override fun migrate(db: SupportSQLiteDatabase) = createBasePoiTable(db)
        }

        /** Version 5 on the map reference branch persisted shopId; retain only base POI columns. */
        internal val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """CREATE TABLE `map_pois_new` (
                        `id` INTEGER NOT NULL,
                        `name` TEXT NOT NULL,
                        `category` TEXT,
                        `description` TEXT,
                        `latitude` REAL,
                        `longitude` REAL,
                        `iconType` TEXT,
                        `visibility` TEXT,
                        `priority` INTEGER,
                        PRIMARY KEY(`id`)
                    )""".trimIndent()
                )
                db.execSQL(
                    """INSERT INTO `map_pois_new` (`id`, `name`, `category`, `description`,
                        `latitude`, `longitude`, `iconType`, `visibility`, `priority`)
                        SELECT `id`, `name`, `category`, `description`, `latitude`, `longitude`,
                        `iconType`, `visibility`, `priority` FROM `map_pois`
                        WHERE `shopId` IS NULL""".trimIndent()
                )
                db.execSQL("DROP TABLE `map_pois`")
                db.execSQL("ALTER TABLE `map_pois_new` RENAME TO `map_pois`")
            }
        }

        private fun createBasePoiTable(db: SupportSQLiteDatabase) {
            db.execSQL(
                """CREATE TABLE IF NOT EXISTS `map_pois` (
                    `id` INTEGER NOT NULL,
                    `name` TEXT NOT NULL,
                    `category` TEXT,
                    `description` TEXT,
                    `latitude` REAL,
                    `longitude` REAL,
                    `iconType` TEXT,
                    `visibility` TEXT,
                    `priority` INTEGER,
                    PRIMARY KEY(`id`)
                )""".trimIndent()
            )
        }
    }
}
