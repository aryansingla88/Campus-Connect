package com.example.campusconnect.feature.metadata.courses

import android.content.Context
import com.example.campusconnect.core.database.AppDatabase
import com.example.campusconnect.core.network.RetrofitClient

object CourseRepositoryProvider {

    @Volatile
    private var INSTANCE: CourseRepository? = null

    fun getRepository(context: Context): CourseRepository {
        return INSTANCE ?: synchronized(this) {
            CourseRepository(
                courseApi = RetrofitClient.courseApi,
                courseDao = AppDatabase
                    .getDatabase(context)
                    .courseDao(),
                courseCacheManager =
                    CourseCacheManager(context.applicationContext)
            ).also { INSTANCE = it }
        }
    }
}