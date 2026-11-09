package com.example.campusconnect.feature.metadata.poi

import android.content.Context

class PoiCacheManager(context: Context) : PoiCacheState {
    private val preferences = context.getSharedPreferences("poi_cache", Context.MODE_PRIVATE)

    fun getLastRefreshTime(): Long = preferences.getLong(KEY_LAST_REFRESH, 0L)

    override fun isExpired(now: Long): Boolean = now - getLastRefreshTime() > CACHE_DURATION_MS

    override fun updateLastRefreshTime(now: Long) {
        preferences.edit().putLong(KEY_LAST_REFRESH, now).apply()
    }

    private companion object {
        const val KEY_LAST_REFRESH = "last_refresh_time"
        const val CACHE_DURATION_MS = 7 * 24 * 60 * 60 * 1000L
    }
}
