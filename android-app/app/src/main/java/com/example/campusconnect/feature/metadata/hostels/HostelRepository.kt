package com.example.campusconnect.feature.metadata.hostels

import com.example.campusconnect.feature.metadata.hostels.local.HostelDao
import com.example.campusconnect.feature.metadata.hostels.local.toEntity
import com.example.campusconnect.feature.metadata.hostels.local.toHostel
import com.example.campusconnect.feature.metadata.hostels.remote.HostelApi

class HostelRepository(
    private val hostelApi: HostelApi,
    private val hostelDao: HostelDao,
    private val hostelCache: HostelCache
) {
    suspend fun getAllHostels(): List<Hostel> = hostelDao.getAllHostels().map { it.toHostel() }

    suspend fun ensureHostelsCached(forceRefresh: Boolean = false): Result<Unit> {
        val cachedHostels = hostelDao.getAllHostels()
        val lastRefresh = hostelCache.getLastRefreshTime()
        val cacheIsFresh = lastRefresh > 0L
                && System.currentTimeMillis() - lastRefresh <= CACHE_DURATION_MS
        val cacheHasHostelCodes = cachedHostels.isNotEmpty() && cachedHostels.all { it.hostel.isNotBlank() }
        if (!forceRefresh && cacheIsFresh && cacheHasHostelCodes) return Result.success(Unit)

        return try {
            val response = hostelApi.getHostels()
            if (!response.isSuccessful) {
                Result.failure(Exception("Failed to fetch hostels: ${response.code()}"))
            } else {
                val hostels = response.body()?.data
                    ?: return Result.failure(Exception("Hostel metadata is empty"))
                hostelDao.replaceHostels(hostels.map { it.toHostel().toEntity() })
                hostelCache.updateLastRefreshTime()
                Result.success(Unit)
            }
        } catch (error: Exception) {
            Result.failure(error)
        }
    }

    companion object {
        const val CACHE_DURATION_MS = 7 * 24 * 60 * 60 * 1000L
    }
}
