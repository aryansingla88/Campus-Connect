package com.example.campusconnect.feature.metadata.eventcategories

import android.content.Context
import com.example.campusconnect.core.database.AppDatabase
import com.example.campusconnect.core.network.RetrofitClient

object EventCategoryRepositoryProvider {

    @Volatile
    private var INSTANCE: EventCategoryRepository? = null

    fun getRepository(context: Context): EventCategoryRepository {
        return INSTANCE ?: synchronized(this) {
            INSTANCE ?: EventCategoryRepository(
                RetrofitClient.eventCategoryApi,
                AppDatabase
                    .getDatabase(context)
                    .eventCategoryDao(),
                EventCategoryCacheManager(
                    context.applicationContext
                )
            ).also {
                INSTANCE = it
            }
        }
    }
}