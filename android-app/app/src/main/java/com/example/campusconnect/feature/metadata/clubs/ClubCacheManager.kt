package com.example.campusconnect.feature.metadata.clubs

import android.content.Context

class ClubCacheManager(context: Context) {

    private val preferences =
        context.getSharedPreferences(
            "club_cache",
            Context.MODE_PRIVATE
        )

    fun getLastRefreshTime(): Long {
        return preferences.getLong(KEY_LAST_REFRESH, 0L)
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
        private const val KEY_LAST_REFRESH = "last_refresh_time"
    }
}
