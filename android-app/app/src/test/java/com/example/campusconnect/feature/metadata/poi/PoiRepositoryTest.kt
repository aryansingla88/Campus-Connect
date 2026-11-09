package com.example.campusconnect.feature.metadata.poi

import com.example.campusconnect.core.network.ApiResponse
import com.example.campusconnect.feature.metadata.poi.local.PoiDao
import com.example.campusconnect.feature.metadata.poi.local.PoiEntity
import com.example.campusconnect.feature.metadata.poi.local.toEntity
import com.example.campusconnect.feature.map.data.remote.response.PoiResponse
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PoiRepositoryTest {
    @Test
    fun successfulEmptyResponseUpdatesRefreshTimeButEmptyCacheIsRetried() = runBlocking {
        var requests = 0
        val cache = FakeCacheState()
        val repository = repository(
            cache = cache,
            fetch = {
                requests++
                ApiResponse(success = true, data = emptyList(), message = null)
            }
        )

        assertTrue(repository.getPois().isSuccess)
        assertTrue(repository.getPois().getOrThrow().isEmpty())
        assertEquals(2, requests)
        assertEquals(NOW + 6 * 60 * 1000L, cache.lastRefresh)
    }

    @Test
    fun cacheRowCountMatchesPersistedRowsWhenApiContainsDuplicateIds() = runBlocking {
        var requests = 0
        val repository = repository(fetch = {
            requests++
            ApiResponse(true, listOf(poi(1), poi(1, "DUPLICATE")), null)
        })

        assertEquals(1, repository.getPois().getOrThrow().size)
        assertEquals(1, repository.getPois().getOrThrow().size)
        assertEquals(1, requests)
    }

    @Test
    fun staleMetadataCacheRefreshes() = runBlocking {
        val cache = FakeCacheState().apply { updateLastRefreshTime(NOW - CACHE_MS - 1) }
        val repository = repository(cache = cache, fetch = {
            ApiResponse(true, listOf(poi(1), poi(2)), null)
        })

        val result = repository.getPois().getOrThrow()

        assertEquals(listOf(1, 2), result.map { it.id })
        assertEquals(NOW + 6 * 60 * 1000L, cache.lastRefresh)
    }

    @Test
    fun failedRefreshReturnsStaleRowsAndDoesNotReplaceThem() = runBlocking {
        val stalePoi = poi(7).toEntity()
        val dao = FakePoiDao(mutableListOf(stalePoi))
        val cache = FakeCacheState().apply { updateLastRefreshTime(NOW - CACHE_MS - 1) }
        val repository = repository(dao, cache, fetch = {
            ApiResponse(success = false, data = null, message = "offline")
        })

        val result = repository.getPois().getOrThrow()

        assertEquals(listOf(7), result.map { it.id })
        assertEquals(listOf(stalePoi), dao.rows)
    }

    @Test
    fun failedTransactionalReplacementKeepsStaleRows() = runBlocking {
        val stalePoi = poi(9).toEntity()
        val dao = object : FakePoiDao(mutableListOf(stalePoi)) {
            override suspend fun replaceAll(pois: List<PoiEntity>) {
                val previous = rows.toList()
                clear()
                rows.addAll(previous)
                throw IllegalStateException("insert failed")
            }
        }
        val cache = FakeCacheState().apply { updateLastRefreshTime(NOW - CACHE_MS - 1) }
        val repository = repository(dao, cache, fetch = {
            ApiResponse(true, listOf(poi(10)), null)
        })

        val result = repository.getPois().getOrThrow()

        assertEquals(listOf(9), result.map { it.id })
        assertEquals(listOf(stalePoi), dao.rows)
    }

    @Test
    fun failedRefreshWithoutRowsReturnsFailure() = runBlocking {
        val repository = repository(fetch = {
            ApiResponse(success = false, data = null, message = "offline")
        })

        val result = repository.getPois()

        assertTrue(result.isFailure)
    }

    @Test
    fun categoryFilteringUsesCachedBaseDataset() = runBlocking {
        var requests = 0
        val repository = repository(fetch = {
            requests++
            ApiResponse(true, listOf(poi(1, "LIBRARY"), poi(2, "HOSTEL")), null)
        })

        val filtered = repository.getPois(" library ").getOrThrow()

        assertEquals(listOf(1), filtered.map { it.id })
        assertEquals(1, requests)
    }

    private fun repository(
        dao: FakePoiDao = FakePoiDao(),
        cache: FakeCacheState = FakeCacheState(),
        now: Long = NOW + 6 * 60 * 1000L,
        fetch: suspend () -> ApiResponse<List<PoiResponse>>
    ) = PoiRepository(fetch, dao, cache, nowMillis = { now })

    private fun poi(id: Int, category: String? = "GENERAL") = PoiResponse(
        id = id,
        name = "POI $id",
        category = category
    )

    private open class FakePoiDao(
        val rows: MutableList<PoiEntity> = mutableListOf()
    ) : PoiDao {
        override suspend fun getAll(): List<PoiEntity> = rows.sortedBy { it.name }

        override suspend fun insertAll(pois: List<PoiEntity>) {
            pois.forEach { poi ->
                rows.removeAll { it.id == poi.id }
                rows.add(poi)
            }
        }

        override suspend fun clear() {
            rows.clear()
        }

        open override suspend fun replaceAll(pois: List<PoiEntity>) {
            val previous = rows.toList()
            try {
                clear()
                insertAll(pois)
            } catch (error: Throwable) {
                rows.clear()
                rows.addAll(previous)
                throw error
            }
        }
    }

    private class FakeCacheState : PoiCacheState {
        var lastRefresh = 0L
        override fun isExpired(now: Long) = now - lastRefresh > CACHE_MS
        override fun updateLastRefreshTime(now: Long) { lastRefresh = now }
    }

    private companion object {
        const val NOW = 10_000_000L
        const val CACHE_MS = 7 * 24 * 60 * 60 * 1000L
    }
}
