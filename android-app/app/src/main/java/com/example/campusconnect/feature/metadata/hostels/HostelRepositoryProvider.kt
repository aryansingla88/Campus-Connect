package com.example.campusconnect.feature.metadata.hostels

import android.content.Context
import com.example.campusconnect.core.database.AppDatabase
import com.example.campusconnect.core.network.RetrofitClient

object HostelRepositoryProvider {
    @Volatile private var instance: HostelRepository? = null

    fun getRepository(context: Context): HostelRepository = instance ?: synchronized(this) {
        instance ?: HostelRepository(
            hostelApi = RetrofitClient.hostelApi,
            hostelDao = AppDatabase.getDatabase(context.applicationContext).hostelDao(),
            hostelCache = HostelCacheManager(context.applicationContext)
        ).also { instance = it }
    }
}
