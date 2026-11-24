package com.example.campusconnect.feature.metadata.hostels

import com.example.campusconnect.core.network.ApiResponse
import com.example.campusconnect.feature.metadata.hostels.local.HostelDao
import com.example.campusconnect.feature.metadata.hostels.local.HostelEntity
import com.example.campusconnect.feature.metadata.hostels.remote.HostelApi
import com.example.campusconnect.feature.metadata.hostels.remote.HostelResponse
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response

class HostelRepositoryTest {
    @Test
    fun fetchesMetadataAndStoresItInTheRoomDao() = runBlocking {
        val dao = FakeHostelDao()
        val cache = FakeHostelCache(lastRefresh = 0L)
        val api = FakeHostelApi(listOf(
            HostelResponse(1, "H1", "Abhimanyu Bhawan", true),
            HostelResponse(6, "DS", "Day Scholar", true)
        ))
        val repository = HostelRepository(api, dao, cache)

        assertTrue(repository.ensureHostelsCached().isSuccess)
        assertEquals(
            listOf(
                Hostel(1, "H1", "Abhimanyu Bhawan", true),
                Hostel(6, "DS", "Day Scholar", true)
            ),
            repository.getAllHostels()
        )
        assertEquals("H1 (Abhimanyu Bhawan)", repository.getAllHostels().first().displayLabel())
        assertEquals(1, api.calls)
        assertTrue(cache.lastRefresh > 0L)
    }

    @Test
    fun freshRoomCacheAvoidsNetworkAndExpiredCacheRefreshes() = runBlocking {
        val dao = FakeHostelDao().apply { rows += HostelEntity(6, "Day Scholar", "DS", true) }
        val now = System.currentTimeMillis()
        val cache = FakeHostelCache(lastRefresh = now)
        val api = FakeHostelApi(listOf(HostelResponse(1, "H1", "Abhimanyu Bhawan", true)))
        val repository = HostelRepository(api, dao, cache)

        repository.ensureHostelsCached()
        assertEquals(0, api.calls)
        cache.lastRefresh = now - HostelRepository.CACHE_DURATION_MS - 1
        repository.ensureHostelsCached()

        assertEquals(1, api.calls)
        assertEquals(listOf(Hostel(1, "H1", "Abhimanyu Bhawan", true)), repository.getAllHostels())
    }

    @Test
    fun refreshesFreshCacheRowsThatPredateTheHostelCodeField() = runBlocking {
        val dao = FakeHostelDao().apply { rows += HostelEntity(6, "Day Scholar", "", true) }
        val cache = FakeHostelCache(lastRefresh = System.currentTimeMillis())
        val api = FakeHostelApi(listOf(HostelResponse(6, "DS", "Day Scholar", true)))
        val repository = HostelRepository(api, dao, cache)

        assertTrue(repository.ensureHostelsCached().isSuccess)

        assertEquals(1, api.calls)
        assertEquals(listOf(Hostel(6, "DS", "Day Scholar", true)), repository.getAllHostels())
    }

    private class FakeHostelApi(private val data: List<HostelResponse>) : HostelApi {
        var calls = 0
        override suspend fun getHostels(): Response<ApiResponse<List<HostelResponse>>> {
            calls++
            return Response.success(ApiResponse(true, data, null))
        }
    }

    private class FakeHostelDao : HostelDao {
        val rows = mutableListOf<HostelEntity>()
        override suspend fun getAllHostels(): List<HostelEntity> = rows.sortedBy { it.name }
        override suspend fun insertHostels(hostels: List<HostelEntity>) { rows.addAll(hostels) }
        override suspend fun clearHostels() { rows.clear() }
        override suspend fun replaceHostels(hostels: List<HostelEntity>) {
            rows.clear()
            rows.addAll(hostels)
        }
    }

    private class FakeHostelCache(var lastRefresh: Long) : HostelCache {
        override fun getLastRefreshTime(): Long = lastRefresh
        override fun updateLastRefreshTime() { lastRefresh = System.currentTimeMillis() }
    }
}
