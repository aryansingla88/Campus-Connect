package com.example.campusconnect.core.components

import com.example.campusconnect.core.network.ApiConfig
import org.junit.Assert.assertEquals
import org.junit.Test

class ApiUrlResolverTest {
    @Test
    fun blankPathsResolveToNull() {
        assertEquals(null, ApiUrlResolver.resolve(null))
        assertEquals(null, ApiUrlResolver.resolve(""))
        assertEquals(null, ApiUrlResolver.resolve("   "))
    }

    @Test
    fun absoluteUrlsArePreserved() {
        val url = "https://cdn.example.test/avatars/user.png?size=small"
        assertEquals(url, ApiUrlResolver.resolve(url))
    }

    @Test
    fun relativePathsUseTheConfiguredApiBaseUrl() {
        assertEquals(
            ApiConfig.BASE_URL.trimEnd('/') + "/uploads/avatar.png",
            ApiUrlResolver.resolve("/uploads/avatar.png"),
        )
    }
}
