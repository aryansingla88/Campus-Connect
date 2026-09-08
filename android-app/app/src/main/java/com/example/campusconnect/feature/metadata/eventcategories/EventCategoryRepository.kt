package com.example.campusconnect.feature.metadata.eventcategories

import com.example.campusconnect.feature.metadata.eventcategories.local.EventCategoryDao
import com.example.campusconnect.feature.metadata.eventcategories.local.toEntity
import com.example.campusconnect.feature.metadata.eventcategories.local.toEventCategory
import com.example.campusconnect.feature.metadata.eventcategories.remote.EventCategoryApi

class EventCategoryRepository(
    private val eventCategoryApi: EventCategoryApi,
    private val eventCategoryDao: EventCategoryDao,
    private val eventCategoryCacheManager: EventCategoryCacheManager
) {

    suspend fun getAllEventCategories(): List<EventCategory> {
        ensureEventCategoriesCached()

        return eventCategoryDao
            .getAllEventCategories()
            .map { it.toEventCategory() }
    }

    suspend fun getEventCategoryById(id: Int): EventCategory? {
        ensureEventCategoriesCached()

        return eventCategoryDao
            .getEventCategoryById(id)
            ?.toEventCategory()
    }

    suspend fun refreshEventCategories(): Result<Unit> {
        return try {
            val response = eventCategoryApi.getEventCategories()

            if (!response.isSuccessful) {
                return Result.failure(
                    Exception("Failed to fetch event categories")
                )
            }

            val categories = response.body()?.data
                ?: return Result.failure(
                    Exception("Empty event categories response")
                )

            eventCategoryDao.clearEventCategories()

            eventCategoryDao.insertEventCategories(
                categories
                    .map { it.toEventCategory() }
                    .map { it.toEntity() }
            )

            eventCategoryCacheManager.updateLastRefreshTime()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun ensureEventCategoriesCached(): Result<Unit> {
        val cachedCategories =
            eventCategoryDao.getAllEventCategories()

        val lastRefreshTime =
            eventCategoryCacheManager.getLastRefreshTime()

        val cacheIsEmpty = cachedCategories.isEmpty()

        val cacheExpired =
            System.currentTimeMillis() - lastRefreshTime >
                CACHE_DURATION_MS

        return if (cacheIsEmpty || cacheExpired) {
            refreshEventCategories()
        } else {
            Result.success(Unit)
        }
    }

    companion object {
        private const val CACHE_DURATION_MS = 7 * 24 * 60 * 60 * 1000L
    }
}
