package com.example.campusconnect.core.database

import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PoiDatabaseMigrationTest {
    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun migratesVersion4WithoutDroppingExistingMetadata() {
        val dbName = "poi-migration-v4-test.db"
        context.deleteDatabase(dbName)
        val oldHelper = openHelper(dbName, version = 4)
        oldHelper.writableDatabase.execSQL(
            "INSERT INTO courses (courseId, degree, programName, courseCode, degreeLevel, durationYears) " +
                "VALUES (11, 'BTech', 'Computer Science', 'CSE', 'UG', 4)"
        )
        oldHelper.writableDatabase.execSQL("INSERT INTO clubs (clubId, name) VALUES (12, 'Robotics')")
        oldHelper.writableDatabase.execSQL("INSERT INTO event_categories (id, name) VALUES (13, 'Workshop')")
        oldHelper.close()

        val migratedHelper = openHelper(dbName, version = 6)
        val migrated = migratedHelper.writableDatabase
        try {
            assertEquals(listOf(11), migrated.query("SELECT courseId FROM courses").use { cursor ->
                buildList { while (cursor.moveToNext()) add(cursor.getInt(0)) }
            })
            assertEquals(1, migrated.query("SELECT clubId FROM clubs WHERE clubId = 12").use { it.count })
            assertEquals(1, migrated.query("SELECT id FROM event_categories WHERE id = 13").use { it.count })
            assertEquals(expectedPoiColumns, columns(migrated))
            assertEquals(0, migrated.query("SELECT * FROM map_pois").use { it.count })
        } finally {
            migratedHelper.close()
            context.deleteDatabase(dbName)
        }
    }

    @Test
    fun migratesVersion5KeepingBasePoisAndDroppingShopLinkedRows() {
        val dbName = "poi-migration-v5-test.db"
        context.deleteDatabase(dbName)
        val oldHelper = openHelper(dbName, version = 5)
        val oldDb = oldHelper.writableDatabase
        oldDb.execSQL(
            "INSERT INTO courses (courseId, degree, programName, courseCode, degreeLevel, durationYears) " +
                "VALUES (22, 'BSc', 'Physics', NULL, 'UG', 3)"
        )
        oldDb.execSQL("INSERT INTO clubs (clubId, name) VALUES (23, 'Astronomy')")
        oldDb.execSQL("INSERT INTO event_categories (id, name) VALUES (24, 'Talk')")
        oldDb.execSQL(
            "INSERT INTO map_pois (id, name, category, shopId) VALUES (1, 'Library', 'LIBRARY', NULL)"
        )
        oldDb.execSQL(
            "INSERT INTO map_pois (id, name, category, shopId) VALUES (2, 'Cafe', 'FOOD', 5)"
        )
        oldHelper.close()

        val migratedHelper = openHelper(dbName, version = 6)
        val migrated = migratedHelper.writableDatabase
        try {
            assertEquals(listOf(1), migrated.query("SELECT id FROM map_pois").use { cursor ->
                buildList { while (cursor.moveToNext()) add(cursor.getInt(0)) }
            })
            assertEquals(expectedPoiColumns, columns(migrated))
            assertFalse(columns(migrated).contains("shopId"))
            assertEquals(listOf(22), migrated.query("SELECT courseId FROM courses").use { cursor ->
                buildList { while (cursor.moveToNext()) add(cursor.getInt(0)) }
            })
            assertEquals(1, migrated.query("SELECT clubId FROM clubs WHERE clubId = 23").use { it.count })
            assertEquals(1, migrated.query("SELECT id FROM event_categories WHERE id = 24").use { it.count })
        } finally {
            migratedHelper.close()
            context.deleteDatabase(dbName)
        }
    }

    private fun openHelper(name: String, version: Int): SupportSQLiteOpenHelper =
        FrameworkSQLiteOpenHelperFactory().create(
            SupportSQLiteOpenHelper.Configuration.builder(context)
                .name(name)
                .callback(object : SupportSQLiteOpenHelper.Callback(version) {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        createExistingTables(db)
                        if (version == 5) createVersion5PoiTable(db)
                    }

                    override fun onUpgrade(
                        db: SupportSQLiteDatabase,
                        oldVersion: Int,
                        newVersion: Int
                    ) {
                        when (oldVersion) {
                            4 -> AppDatabase.MIGRATION_4_6.migrate(db)
                            5 -> AppDatabase.MIGRATION_5_6.migrate(db)
                            else -> error("Unexpected migration path: $oldVersion -> $newVersion")
                        }
                    }
                })
                .build()
        )

    private fun createExistingTables(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE courses (courseId INTEGER NOT NULL, degree TEXT NOT NULL, " +
                "programName TEXT NOT NULL, courseCode TEXT, degreeLevel TEXT NOT NULL, " +
                "durationYears INTEGER NOT NULL, PRIMARY KEY(courseId))"
        )
        db.execSQL("CREATE TABLE clubs (clubId INTEGER NOT NULL, name TEXT NOT NULL, PRIMARY KEY(clubId))")
        db.execSQL("CREATE TABLE event_categories (id INTEGER NOT NULL, name TEXT NOT NULL, PRIMARY KEY(id))")
    }

    private fun createVersion5PoiTable(db: SupportSQLiteDatabase) {
        db.execSQL(
            """CREATE TABLE map_pois (
                id INTEGER NOT NULL,
                name TEXT NOT NULL,
                category TEXT,
                description TEXT,
                latitude REAL,
                longitude REAL,
                iconType TEXT,
                visibility TEXT,
                priority INTEGER,
                shopId INTEGER,
                PRIMARY KEY(id)
            )""".trimIndent()
        )
    }

    private fun columns(db: SupportSQLiteDatabase): List<String> =
        db.query("PRAGMA table_info(map_pois)").use { cursor ->
            buildList { while (cursor.moveToNext()) add(cursor.getString(1)) }
        }

    private companion object {
        val expectedPoiColumns = listOf(
            "id", "name", "category", "description", "latitude", "longitude",
            "iconType", "visibility", "priority"
        )
    }
}
