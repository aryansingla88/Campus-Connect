package com.example.campusconnect.feature.posts.data.repo

import android.content.ContextWrapper
import com.example.campusconnect.core.network.ApiResponse
import com.example.campusconnect.core.visibility.VisibilityApiConfiguration
import com.example.campusconnect.core.visibility.VisibilityScope
import com.example.campusconnect.feature.posts.data.remote.PostsApi
import com.example.campusconnect.feature.posts.models.Post
import kotlinx.coroutines.runBlocking
import okhttp3.RequestBody
import okio.Buffer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import retrofit2.Response
import java.lang.reflect.Proxy

class ApiPostsRepositoryVisibilityTest {

    @Test
    fun forwardsPublicAndEventsVisibilityPartsToPostsApi() = runBlocking {
        val publicCall = invokeCreatePost(VisibilityApiConfiguration(VisibilityScope.PUBLIC))
        assertEquals("PUBLIC", bodyText(publicCall[6]))
        assertNull(publicCall[7])

        val eventsCall = invokeCreatePost(VisibilityApiConfiguration(scope = null, value = null))
        assertNull(eventsCall[6])
        assertNull(eventsCall[7])
    }

    @Test
    fun forwardsCustomVisibilityValuesWithoutChangingTheirRepresentation() = runBlocking {
        val configurations = listOf(
            VisibilityApiConfiguration(VisibilityScope.CONNECTIONS),
            VisibilityApiConfiguration(VisibilityScope.CLUB, "12"),
            VisibilityApiConfiguration(VisibilityScope.HOSTEL, "3"),
            VisibilityApiConfiguration(VisibilityScope.COURSE, "41"),
            VisibilityApiConfiguration(VisibilityScope.ACADEMIC_YEAR, "4"),
            VisibilityApiConfiguration(VisibilityScope.ACADEMIC_YEAR, "ALUMNI")
        )

        configurations.forEach { configuration ->
            val call = invokeCreatePost(configuration)
            assertEquals(configuration.scope?.name, bodyText(call[6]))
            assertEquals(configuration.value, bodyText(call[7]))
        }
    }

    private suspend fun invokeCreatePost(
        visibility: VisibilityApiConfiguration
    ): Array<out Any?> {
        var captured: Array<out Any?>? = null
        val api = Proxy.newProxyInstance(
            PostsApi::class.java.classLoader,
            arrayOf(PostsApi::class.java)
        ) { _, method, args ->
            if (method.name == "createPost") {
                captured = args
                Response.success(ApiResponse<Post>(success = true, data = null, message = null))
            } else {
                throw UnsupportedOperationException("Unexpected PostsApi call: ${method.name}")
            }
        } as PostsApi

        ApiPostsRepository(context = ContextWrapper(null), api = api).createPost(
            title = "Title",
            body = "Body",
            tags = emptyList(),
            image = null,
            visibilityConfiguration = visibility
        )
        return requireNotNull(captured)
    }

    private fun bodyText(value: Any?): String? {
        val requestBody = value as? RequestBody ?: return null
        val buffer = Buffer()
        requestBody.writeTo(buffer)
        return buffer.readUtf8()
    }
}
