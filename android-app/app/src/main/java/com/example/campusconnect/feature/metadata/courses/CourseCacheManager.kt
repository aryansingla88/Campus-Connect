package com.example.campusconnect.feature.metadata.courses

import android.content.Context

class CourseCacheManager(context: Context) {

    private val preferences =
        context.getSharedPreferences(
            "course_cache",
            Context.MODE_PRIVATE
        )

    fun getLastRefreshTime(): Long {
        return preferences.getLong(
            KEY_LAST_REFRESH,
            0L
        )
    }

    fun updateLastRefreshTime() {
        preferences.edit()
            .putLong(
                KEY_LAST_REFRESH,
                System.currentTimeMillis()
            )
            .apply()
    }

    companion object {
        private const val KEY_LAST_REFRESH =
            "last_refresh_time"
    }
}