package com.example.campusconnect.feature.posts.data.repo

import android.content.ContextWrapper
import com.example.campusconnect.core.network.ApiResponse
import com.example.campusconnect.core.visibility.VisibilityApiConfiguration
import com.example.campusconnect.core.visibility.VisibilityScope
import com.example.campusconnect.feature.posts.data.remote.PostsApi
import com.example.campusconnect.feature.posts.data.remote.PostApiErrorParser
import com.example.campusconnect.feature.posts.data.remote.PostApiException
import com.example.campusconnect.feature.posts.models.Post
import kotlinx.coroutines.runBlocking
import okhttp3.RequestBody
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.ResponseBody.Companion.toResponseBody
import okhttp3.MediaType.Companion.toMediaType
import okio.Buffer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response
import java.lang.reflect.Proxy

class ApiPostsRepositoryVisibilityTest {

    @Test
    fun customFeedUsesDedicatedEndpointAndForwardsSearchQuery() = runBlocking {
        var requestedQuery: String? = null
        val api = Proxy.newProxyInstance(
            PostsApi::class.java.classLoader,
            arrayOf(PostsApi::class.java)
        ) { _, method, args ->
            if (method.name == "getCustomPosts") {
                requestedQuery = args?.firstOrNull() as? String
                Response.success(ApiResponse<List<Post>>(success = true, data = emptyList(), message = null))
            } else {
                throw UnsupportedOperationException("Unexpected PostsApi call: ${method.name}")
            }
        } as PostsApi

        val result = ApiPostsRepository(context = ContextWrapper(null), api = api)
            .getCustomPosts("custom search")

        assertTrue(result.isSuccess)
        assertEquals("custom search", requestedQuery)
    }

    @Test
    fun inaccessiblePostResponsePreserves404ForTheUi() {
        val rawResponse = okhttp3.Response.Builder()
            .request(Request.Builder().url("https://campus.example/posts/42").build())
            .protocol(Protocol.HTTP_1_1)
            .code(404)
            .message("Not Found")
            .build()
        val response = Response.error<ApiResponse<Post>>(
            "{}".toResponseBody("application/json".toMediaType()),
            rawResponse
        )

        val error = PostApiErrorParser.exception(response, "Failed to fetch post")

        assertEquals(404, error.statusCode)
        assertTrue(error.message.contains("no longer available"))
    }

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
