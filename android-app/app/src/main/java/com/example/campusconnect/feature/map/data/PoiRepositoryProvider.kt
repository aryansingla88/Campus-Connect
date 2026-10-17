package com.example.campusconnect.feature.map.data

import android.content.Context
import com.example.campusconnect.core.database.AppDatabase
import com.example.campusconnect.core.network.RetrofitClient

internal object PoiRepositoryProvider {
    @Volatile private var instance: PoiRepository? = null

    fun get(context: Context): PoiRepository = instance ?: synchronized(this) {
        instance ?: PoiRepository(
            fetchPois = RetrofitClient.mapApi::getPois,
            dao = AppDatabase.getDatabase(context).poiDao(),
            cacheManager = PoiCacheManager(context)
        ).also { instance = it }
    }
}
