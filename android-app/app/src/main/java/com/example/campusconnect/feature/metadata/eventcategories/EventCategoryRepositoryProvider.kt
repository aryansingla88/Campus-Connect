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
                eventCategoryApi = RetrofitClient.eventCategoryApi,
                eventCategoryDao = AppDatabase
                    .getDatabase(context)
                    .eventCategoryDao(),
                eventCategoryCacheManager =
                    EventCategoryCacheManager(
                        context.applicationContext
                    )
            ).also {
                INSTANCE = it
            }
        }
    }
}
