package com.example.campusconnect.feature.posts.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.campusconnect.core.network.RetrofitClient
import com.example.campusconnect.feature.posts.data.repo.ApiPostsRepository
import com.example.campusconnect.feature.posts.data.repo.PostsRepository
import com.example.campusconnect.feature.posts.data.remote.PostApiException
import com.example.campusconnect.feature.posts.models.Post
import com.example.campusconnect.feature.posts.models.PostEvent
import com.example.campusconnect.feature.posts.models.PostTag
import com.example.campusconnect.feature.posts.models.PostTagCount
import com.example.campusconnect.feature.posts.models.PostEventCategory
import com.example.campusconnect.feature.posts.models.VoteType
import com.example.campusconnect.core.visibility.VisibilityApiConfiguration
import com.example.campusconnect.core.visibility.VisibilityScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class FeedUiState(
    val posts: List<Post> = emptyList(),
    val customPosts: List<Post> = emptyList(),
    val eventPosts: List<PostEvent> = emptyList(),
    val tags: List<PostTag> = emptyList(),
    val tagCounts: List<PostTagCount>? = null,
    val isLoadingPosts: Boolean = true,
    val postsError: String? = null,
    val isLoadingCustomPosts: Boolean = true,
    val customPostsError: String? = null,
    val isLoadingEvents: Boolean = true,
    val eventsError: String? = null,
    val isLoadingTags: Boolean = true,
    val tagsError: String? = null,
    val isLoadingTagCounts: Boolean = true,
    val tagCountsError: String? = null,
    val generalSearchResults: List<Post> = emptyList(),
    val customSearchResults: List<Post> = emptyList(),
    val eventSearchResults: List<PostEvent> = emptyList(),
    val isSearching: Boolean = false,
    val searchError: String? = null,
    val voteError: String? = null,
    val votingEventPostIds: Set<Int> = emptySet(),
    val isDeletingPost: Boolean = false,
    val deletePostError: String? = null,
    val isDeletingEventPost: Boolean = false,
    val deleteEventPostError: String? = null,
    val currentUserDisplayName: String = "Current user",
    val currentUserAvatarUrl: String? = null
)

class FeedViewModel(
    private val repository: PostsRepository = ApiPostsRepository()
) : ViewModel() {
    private val mutableUiState = MutableStateFlow(FeedUiState())
    val uiState = mutableUiState.asStateFlow()
    private var searchJob: Job? = null
    private var searchGeneration = 0
    private var feedGeneration = 0
    private var feedJob: Job? = null

    fun loadFeed(profileUserId: Int?) {
        feedJob?.cancel()
        val generation = ++feedGeneration
        feedJob = viewModelScope.launch {
            mutableUiState.update {
                it.copy(isLoadingPosts = true, postsError = null, isLoadingEvents = true, eventsError = null,
                    isLoadingCustomPosts = true, customPostsError = null,
                    isLoadingTags = true, tagsError = null, isLoadingTagCounts = true, tagCountsError = null)
            }
            val postsRequest = async { repository.getPosts() }
            val customPostsRequest = async { repository.getCustomPosts() }
            val eventsRequest = async { repository.getEventPosts() }
            val tagsRequest = async { repository.getTags() }
            val countsRequest = async { repository.getGeneralTagCounts() }
            val results = try {
                FeedLoadResults(postsRequest.await(), customPostsRequest.await(), eventsRequest.await(), tagsRequest.await(), countsRequest.await())
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                if (generation == feedGeneration) mutableUiState.update {
                    it.copy(isLoadingPosts = false, isLoadingCustomPosts = false, isLoadingEvents = false, isLoadingTags = false,
                        isLoadingTagCounts = false, postsError = error.message ?: "Failed to load posts.")
                }
                return@launch
            }
            if (generation != feedGeneration) return@launch
            val postsResult = results.posts
            val customPostsResult = results.customPosts
            val eventsResult = results.events
            val tagsResult = results.tags
            val countsResult = results.counts
            mutableUiState.update { old ->
                old.copy(
                    posts = postsResult.getOrElse { old.posts }.filter { profileUserId == null || it.authorId == profileUserId },
                    postsError = if (postsResult.isFailure) postsResult.exceptionOrNull()?.message ?: "Failed to load posts." else null,
                    isLoadingPosts = false,
                    customPosts = customPostsResult.getOrElse { old.customPosts }.filter { profileUserId == null || it.authorId == profileUserId },
                    customPostsError = if (customPostsResult.isFailure) customPostsResult.exceptionOrNull()?.message ?: "Failed to load custom posts." else null,
                    isLoadingCustomPosts = false,
                    eventPosts = eventsResult.getOrElse { old.eventPosts }.filter { profileUserId == null || it.organizerUserId == profileUserId },
                    eventsError = eventsResult.exceptionOrNull()?.message?.takeIf { eventsResult.isFailure } ?: if (eventsResult.isFailure) "Failed to load event posts." else null,
                    isLoadingEvents = false,
                    tags = tagsResult.getOrElse { old.tags },
                    tagsError = tagsResult.exceptionOrNull()?.message?.takeIf { tagsResult.isFailure },
                    isLoadingTags = false,
                    tagCounts = countsResult.getOrElse { old.tagCounts },
                    tagCountsError = countsResult.exceptionOrNull()?.message?.takeIf { countsResult.isFailure },
                    isLoadingTagCounts = false
                )
            }
        }
    }

    fun loadCurrentUserProfile() {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.profileApi.getMyProfile()
                val profile = if (response.isSuccessful) response.body()?.data else null
                mutableUiState.update { it.copy(
                    currentUserDisplayName = profile?.fullName?.takeIf(String::isNotBlank)
                        ?: profile?.username?.takeIf(String::isNotBlank) ?: "Current user",
                    currentUserAvatarUrl = profile?.avatarUrl
                ) }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                // Initials remain available if profile data cannot be loaded.
            }
        }
    }

    fun clearSearch() {
        searchGeneration++
        searchJob?.cancel()
        searchJob = null
        mutableUiState.update { it.copy(generalSearchResults = emptyList(), customSearchResults = emptyList(), eventSearchResults = emptyList(), isSearching = false, searchError = null) }
    }

    fun prepareSearch(query: String) {
        searchGeneration++
        searchJob?.cancel()
        searchJob = null
        mutableUiState.update { it.copy(generalSearchResults = emptyList(), customSearchResults = emptyList(), eventSearchResults = emptyList(),
            isSearching = query.trim().length >= 2, searchError = null) }
    }

    fun search(query: String, tab: Int, profileUserId: Int?) {
        searchGeneration++
        val generation = searchGeneration
        searchJob?.cancel()
        if (query.trim().length < 2) {
            mutableUiState.update { it.copy(generalSearchResults = emptyList(), customSearchResults = emptyList(), eventSearchResults = emptyList(), isSearching = false, searchError = null) }
            return
        }
        mutableUiState.update { it.copy(isSearching = true, searchError = null) }
        searchJob = viewModelScope.launch {
            try {
                if (tab == 1) {
                    val result = repository.getEventPosts(query.trim())
                    if (generation != searchGeneration) return@launch
                    mutableUiState.update { it.copy(eventSearchResults = result.getOrElse { emptyList() }
                        .filter { profileUserId == null || it.organizerUserId == profileUserId },
                        searchError = result.exceptionOrNull()?.message, isSearching = false) }
                } else if (tab == 2) {
                    val result = repository.getCustomPosts(query.trim())
                    if (generation != searchGeneration) return@launch
                    mutableUiState.update { it.copy(customSearchResults = result.getOrElse { emptyList() }
                        .filter { profileUserId == null || it.authorId == profileUserId },
                        searchError = result.exceptionOrNull()?.message, isSearching = false) }
                } else {
                    val result = repository.getPosts(query.trim())
                    if (generation != searchGeneration) return@launch
                    mutableUiState.update { it.copy(generalSearchResults = result.getOrElse { emptyList() }
                        .filter { profileUserId == null || it.authorId == profileUserId },
                        searchError = result.exceptionOrNull()?.message, isSearching = false) }
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                if (generation == searchGeneration) mutableUiState.update {
                    it.copy(searchError = error.message ?: "Search failed.", isSearching = false)
                }
            }
        }
    }

    suspend fun voteGeneralPost(post: Post, vote: VoteType, custom: Boolean = false) {
        mutableUiState.update { it.copy(voteError = null) }
        val result = when {
            post.userVote == vote -> repository.removePostVote(post.id)
            vote == VoteType.UPVOTE -> repository.upvotePost(post.id)
            else -> repository.downvotePost(post.id)
        }
        result.onSuccess {
            val source = if (custom) mutableUiState.value.customPosts else mutableUiState.value.posts
            val updated = source.map { if (it.id == post.id) it.withVote(vote) else it }
            val map = updated.associateBy(Post::id)
            mutableUiState.update { state ->
                if (custom) state.copy(customPosts = updated,
                    customSearchResults = state.customSearchResults.map { map[it.id] ?: if (it.id == post.id) it.withVote(vote) else it })
                else state.copy(posts = updated,
                    generalSearchResults = state.generalSearchResults.map { map[it.id] ?: if (it.id == post.id) it.withVote(vote) else it })
            }
        }.onFailure { error ->
            if (custom && (error as? PostApiException)?.statusCode == 404) {
                mutableUiState.update { state -> state.copy(
                    customPosts = state.customPosts.filterNot { it.id == post.id },
                    customSearchResults = state.customSearchResults.filterNot { it.id == post.id }
                ) }
            }
            mutableUiState.update { it.copy(voteError = error.message ?: "Couldn't update your vote.") }
        }
    }

    private data class FeedLoadResults(
        val posts: Result<List<Post>>,
        val customPosts: Result<List<Post>>,
        val events: Result<List<PostEvent>>,
        val tags: Result<List<PostTag>>,
        val counts: Result<List<PostTagCount>>
    )

    private fun Post.withVote(requested: VoteType): Post {
        val target = if (userVote == requested) null else requested
        return copy(
            upvotes = (upvotes - (if (userVote == VoteType.UPVOTE) 1 else 0) + (if (target == VoteType.UPVOTE) 1 else 0)).coerceAtLeast(0),
            downvotes = (downvotes - (if (userVote == VoteType.DOWNVOTE) 1 else 0) + (if (target == VoteType.DOWNVOTE) 1 else 0)).coerceAtLeast(0),
            userVote = target
        )
    }

    suspend fun voteEventPost(event: PostEvent, requested: VoteType) {
        val postId = event.postId ?: return
        val state = mutableUiState.value
        if (postId in state.votingEventPostIds) return
        val previousVote = event.userVote
        val nextVote = if (previousVote == requested) null else requested
        updateEventVotes(postId, previousVote, nextVote)
        mutableUiState.update { it.copy(voteError = null, votingEventPostIds = it.votingEventPostIds + postId) }
        try {
            when {
                previousVote == requested -> repository.removePostVote(postId)
                requested == VoteType.UPVOTE -> repository.upvotePost(postId)
                else -> repository.downvotePost(postId)
            }.onFailure { error ->
                updateEventVotes(postId, nextVote, previousVote)
                mutableUiState.update { it.copy(voteError = error.message ?: "Couldn't update your vote.") }
            }
        } catch (cancelled: CancellationException) {
            updateEventVotes(postId, nextVote, previousVote)
            throw cancelled
        } catch (error: Exception) {
            updateEventVotes(postId, nextVote, previousVote)
            mutableUiState.update { it.copy(voteError = error.message ?: "Couldn't update your vote.") }
        } finally {
            mutableUiState.update { it.copy(votingEventPostIds = it.votingEventPostIds - postId) }
        }
    }

    private fun updateEventVotes(postId: Int, from: VoteType?, to: VoteType?) {
        fun PostEvent.changed() = if (this.postId != postId) this else copy(
            upvotes = (upvotes - (if (from == VoteType.UPVOTE) 1 else 0) + (if (to == VoteType.UPVOTE) 1 else 0)).coerceAtLeast(0),
            downvotes = (downvotes - (if (from == VoteType.DOWNVOTE) 1 else 0) + (if (to == VoteType.DOWNVOTE) 1 else 0)).coerceAtLeast(0), userVote = to)
        mutableUiState.update { it.copy(eventPosts = it.eventPosts.map { event -> event.changed() }, eventSearchResults = it.eventSearchResults.map { event -> event.changed() }) }
    }

    suspend fun deleteGeneralPost(postId: Int, custom: Boolean = false): Result<Unit> {
        mutableUiState.update { it.copy(isDeletingPost = true, deletePostError = null) }
        return try {
            val result = repository.deletePost(postId)
            result.onSuccess {
                mutableUiState.update { state ->
                    if (custom) state.copy(
                        customPosts = state.customPosts.filterNot { post -> post.id == postId },
                        customSearchResults = state.customSearchResults.filterNot { post -> post.id == postId }
                    ) else state.copy(
                        posts = state.posts.filterNot { post -> post.id == postId },
                        generalSearchResults = state.generalSearchResults.filterNot { post -> post.id == postId }
                    )
                }
                if (!custom) {
                    mutableUiState.update { it.copy(isLoadingTagCounts = true, tagCountsError = null) }
                    val counts = repository.getGeneralTagCounts()
                    mutableUiState.update { state ->
                        state.copy(tagCounts = counts.getOrElse { state.tagCounts }, tagCountsError = counts.exceptionOrNull()?.message, isLoadingTagCounts = false)
                    }
                }
            }.onFailure { error ->
                if (custom && (error as? PostApiException)?.statusCode == 404) {
                    mutableUiState.update { state -> state.copy(
                        customPosts = state.customPosts.filterNot { it.id == postId },
                        customSearchResults = state.customSearchResults.filterNot { it.id == postId }
                    ) }
                }
                mutableUiState.update { it.copy(deletePostError = error.message ?: "Couldn't delete the post.") }
            }
            result
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (error: Exception) {
            Result.failure<Unit>(error).also { mutableUiState.update { it.copy(deletePostError = error.message ?: "Couldn't delete the post.") } }
        } finally {
            mutableUiState.update { it.copy(isDeletingPost = false) }
        }
    }

    suspend fun deleteEventPost(postId: Int): Result<Unit> {
        mutableUiState.update { it.copy(isDeletingEventPost = true, deleteEventPostError = null) }
        return try {
            val result = repository.deletePost(postId)
            result.onSuccess { mutableUiState.update { it.copy(eventPosts = it.eventPosts.filterNot { event -> event.postId == postId }, eventSearchResults = it.eventSearchResults.filterNot { event -> event.postId == postId }) } }
                .onFailure { error -> mutableUiState.update { it.copy(deleteEventPostError = error.message ?: "Couldn’t delete the event post.") } }
            result
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (error: Exception) {
            Result.failure<Unit>(error).also { mutableUiState.update { it.copy(deleteEventPostError = error.message ?: "Couldn’t delete the event post.") } }
        } finally {
            mutableUiState.update { it.copy(isDeletingEventPost = false) }
        }
    }

    fun clearDeleteErrors() { mutableUiState.update { it.copy(deletePostError = null, deleteEventPostError = null) } }

    suspend fun getPosts(query: String? = null): Result<List<Post>> = repository.getPosts(query)
    suspend fun getCustomPosts(query: String? = null): Result<List<Post>> = repository.getCustomPosts(query)
    suspend fun getPost(postId: Int): Result<Post> = repository.getPost(postId)
    suspend fun getTags(): Result<List<PostTag>> = repository.getTags()
    suspend fun getGeneralTagCounts(): Result<List<PostTagCount>> = repository.getGeneralTagCounts()
    suspend fun getMyEvents() = repository.getMyEvents()
    suspend fun getEventCategories(): Result<List<PostEventCategory>> = repository.getEventCategories()
    suspend fun getEventPosts(query: String? = null) = repository.getEventPosts(query)
    suspend fun createPost(title: String, body: String, tags: List<PostTag>, image: Uri?, eventId: Int? = null, eventCategoryId: Int? = null, visibilityConfiguration: VisibilityApiConfiguration = VisibilityApiConfiguration(VisibilityScope.PUBLIC)): Result<Post> = repository.createPost(title, body, tags, image, eventId, eventCategoryId, visibilityConfiguration)
    suspend fun updatePost(postId: Int, body: String, tags: List<PostTag>, image: Uri?, eventCategoryId: Int? = null, removeImage: Boolean = false): Result<Post> = repository.updatePost(postId, body, tags, image, eventCategoryId, removeImage)
    suspend fun upvotePost(postId: Int): Result<Unit> = repository.upvotePost(postId)
    suspend fun downvotePost(postId: Int): Result<Unit> = repository.downvotePost(postId)
    suspend fun removePostVote(postId: Int): Result<Unit> = repository.removePostVote(postId)
    suspend fun deletePost(postId: Int): Result<Unit> = repository.deletePost(postId)
}
