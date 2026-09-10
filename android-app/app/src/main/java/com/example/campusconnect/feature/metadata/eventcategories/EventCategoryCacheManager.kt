package com.example.campusconnect.feature.metadata.eventcategories

import android.content.Context

class EventCategoryCacheManager(context: Context) {

    private val preferences =
        context.getSharedPreferences(
            "event_category_cache",
            Context.MODE_PRIVATE
        )

    fun getLastRefreshTime(): Long =
        preferences.getLong(KEY_LAST_REFRESH, 0L)

    fun updateLastRefreshTime() {
        preferences.edit()
            .putLong(KEY_LAST_REFRESH, System.currentTimeMillis())
            .apply()
    }

    companion object {
        private const val KEY_LAST_REFRESH = "last_refresh_time"
    }
}
