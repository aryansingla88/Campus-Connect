package com.example.campusconnect.feature.metadata.clubs

import com.example.campusconnect.feature.metadata.clubs.local.ClubDao
import com.example.campusconnect.feature.metadata.clubs.local.toClub
import com.example.campusconnect.feature.metadata.clubs.local.toEntity
import com.example.campusconnect.feature.metadata.clubs.remote.ClubApi

class ClubRepository(
    private val clubApi: ClubApi,
    private val clubDao: ClubDao,
    private val clubCacheManager: ClubCacheManager
) {

    suspend fun getAllClubs(): List<Club> {
        ensureClubsCached()

        return clubDao
            .getAllClubs()
            .map { it.toClub() }
    }

    suspend fun getClubNameById(clubId: Int): String? {
        ensureClubsCached()

        return clubDao.getClubNameById(clubId)
    }

    suspend fun refreshClubs(): Result<Unit> {
        return try {
            val response = clubApi.getClubs()

            if (!response.isSuccessful) {
                return Result.failure(
                    Exception("Failed to fetch clubs: ${response.code()}")
                )
            }

            val clubs = response.body()?.data
                ?: return Result.failure(
                    Exception("Empty clubs response")
                )

            clubDao.clearClubs()

            clubDao.insertClubs(
                clubs
                    .map { it.toClub() }
                    .map { it.toEntity() }
            )

            clubCacheManager.updateLastRefreshTime()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun ensureClubsCached(): Result<Unit> {
        val cachedClubs = clubDao.getAllClubs()

        val lastRefreshTime =
            clubCacheManager.getLastRefreshTime()

        val cacheIsEmpty = cachedClubs.isEmpty()

        val cacheExpired =
            System.currentTimeMillis() - lastRefreshTime >
                    CACHE_DURATION_MS

        return if (cacheIsEmpty || cacheExpired) {
            refreshClubs()
        } else {
            Result.success(Unit)
        }
    }

    companion object {
        private const val CACHE_DURATION_MS =
            24 * 60 * 60 * 1000L
    }
}
