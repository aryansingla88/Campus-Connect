package com.example.campusconnect.feature.map.data

import android.content.Context

interface PoiCacheState {
    fun hasSuccessfulCache(): Boolean
    fun isCacheConsistent(rowCount: Int): Boolean
    fun isExpired(now: Long): Boolean
    fun canAttemptRefresh(now: Long): Boolean
    fun markRefreshAttempt(now: Long)
    fun markRefreshed(now: Long, rowCount: Int)
}

class PoiCacheManager(context: Context) : PoiCacheState {
    private val preferences = context.getSharedPreferences("map_poi_cache", Context.MODE_PRIVATE)

    override fun hasSuccessfulCache(): Boolean = preferences.getBoolean(KEY_HAS_CACHE, false)

    override fun isCacheConsistent(rowCount: Int): Boolean =
        hasSuccessfulCache() && preferences.getInt(KEY_ROW_COUNT, -1) == rowCount

    override fun isExpired(now: Long): Boolean =
        now - preferences.getLong(KEY_LAST_REFRESH, 0L) > CACHE_DURATION_MS

    override fun canAttemptRefresh(now: Long): Boolean =
        now - preferences.getLong(KEY_LAST_ATTEMPT, 0L) >= REFRESH_RETRY_DELAY_MS

    override fun markRefreshAttempt(now: Long) {
        preferences.edit().putLong(KEY_LAST_ATTEMPT, now).apply()
    }

    override fun markRefreshed(now: Long, rowCount: Int) {
        // Commit the freshness marker only after the DAO transaction has completed.
        preferences.edit()
            .putLong(KEY_LAST_REFRESH, now)
            .putInt(KEY_ROW_COUNT, rowCount)
            .putBoolean(KEY_HAS_CACHE, true)
            .commit()
    }

    private companion object {
        const val KEY_LAST_REFRESH = "last_refresh_time"
        const val KEY_LAST_ATTEMPT = "last_refresh_attempt"
        const val KEY_HAS_CACHE = "has_successful_cache"
        const val KEY_ROW_COUNT = "cached_row_count"
        const val CACHE_DURATION_MS = 7 * 24 * 60 * 60 * 1000L
        const val REFRESH_RETRY_DELAY_MS = 5 * 60 * 1000L
    }
}
