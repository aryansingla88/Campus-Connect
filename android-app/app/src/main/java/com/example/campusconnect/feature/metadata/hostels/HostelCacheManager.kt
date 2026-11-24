package com.example.campusconnect.feature.metadata.hostels

import android.content.Context

interface HostelCache {
    fun getLastRefreshTime(): Long
    fun updateLastRefreshTime()
}

class HostelCacheManager(context: Context) : HostelCache {
    private val preferences = context.getSharedPreferences("hostel_cache", Context.MODE_PRIVATE)

    override fun getLastRefreshTime(): Long = preferences.getLong(KEY_LAST_REFRESH, 0L)

    override fun updateLastRefreshTime() {
        preferences.edit().putLong(KEY_LAST_REFRESH, System.currentTimeMillis()).apply()
    }

    private companion object {
        const val KEY_LAST_REFRESH = "last_refresh_time"
    }
}
