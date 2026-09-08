package com.example.campusconnect.feature.metadata.clubs

import android.content.Context
import com.example.campusconnect.core.database.AppDatabase
import com.example.campusconnect.core.network.RetrofitClient

object ClubRepositoryProvider {

    @Volatile
    private var INSTANCE: ClubRepository? = null

    fun getRepository(context: Context): ClubRepository {
        return INSTANCE ?: synchronized(this) {
            INSTANCE ?: ClubRepository(
                clubApi = RetrofitClient.clubApi,
                clubDao = AppDatabase
                    .getDatabase(context)
                    .clubDao(),
                clubCacheManager =
                    ClubCacheManager(context.applicationContext)
            ).also {
                INSTANCE = it
            }
        }
    }
}
