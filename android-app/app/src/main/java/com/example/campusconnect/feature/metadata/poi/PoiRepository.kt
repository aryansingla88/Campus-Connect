package com.example.campusconnect.feature.metadata.poi

import com.example.campusconnect.feature.metadata.poi.local.PoiDao
import com.example.campusconnect.feature.metadata.poi.local.toEntity
import com.example.campusconnect.feature.metadata.poi.local.toResponse
import com.example.campusconnect.core.network.ApiResponse
import com.example.campusconnect.feature.map.data.remote.response.PoiResponse
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

interface PoiCacheState {
    fun isExpired(now: Long): Boolean
    fun updateLastRefreshTime(now: Long)
}

/** Single Room-backed cache for ordinary POI metadata; category filtering is local. */
class PoiRepository(
    private val fetchPois: suspend () -> ApiResponse<List<PoiResponse>>,
    private val dao: PoiDao,
    private val cacheManager: PoiCacheState,
    private val nowMillis: () -> Long = System::currentTimeMillis
) {
    private val refreshMutex = Mutex()

    suspend fun getPois(category: String? = null): Result<List<PoiResponse>> {
        val refreshResult = ensureFresh()
        val cached = dao.getAll()
        if (refreshResult.isFailure && cached.isEmpty()) {
            return Result.failure(refreshResult.exceptionOrNull()!!)
        }
        val responses = cached.map { it.toResponse() }
        val normalized = category?.normalizeCategory()?.takeIf(String::isNotEmpty)
        return Result.success(
            if (normalized == null) responses else responses.filter {
                it.category?.normalizeCategory() == normalized
            }
        )
    }

    suspend fun getCategories(): Result<List<String>> = getPois().map { pois ->
        pois.mapNotNull { it.category?.trim()?.takeIf(String::isNotEmpty) }
            .distinctBy { it.normalizeCategory() }
            .sorted()
    }

    private suspend fun ensureFresh(): Result<Unit> = refreshMutex.withLock {
        val cached = dao.getAll()
        val now = nowMillis()
        if (cached.isNotEmpty() && !cacheManager.isExpired(now)) {
            return@withLock Result.success(Unit)
        }
        try {
            val response = fetchPois()
            check(response.success) { response.message ?: "Unable to refresh POIs" }
            val pois = requireNotNull(response.data) { "POI response contained no data" }
            dao.replaceAll(pois.map { it.toEntity() })
            cacheManager.updateLastRefreshTime(nowMillis())
            Result.success(Unit)
        } catch (error: Exception) {
            if (cached.isNotEmpty()) Result.success(Unit) else Result.failure(error)
        }
    }

    private fun String.normalizeCategory(): String = trim().replace(' ', '_').uppercase()
}
