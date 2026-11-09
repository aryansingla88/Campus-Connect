package com.example.campusconnect.feature.posts.models
import com.example.campusconnect.feature.posts.models.VoteType

data class Post(

    val id: Int,

    val username: String,

    val title: String,

    val body: String,

    val tags: List<PostTag>,

    val imageUrl: String? = null,

    val upvotes: Int,

    val downvotes: Int,

    val userVote: VoteType? = null,

    val createdAt: String,

    val commentCount: Int,

    val canDelete: Boolean = false,
    val isEdited: Boolean = false,
    val postType: String = "GENERAL",
    val eventCategoryName: String? = null,
    val eventOrganizerName: String? = null,
    val eventVenue: String? = null,
    val eventLatitude: Double? = null,
    val eventLongitude: Double? = null,
    val eventStartTime: String? = null,
    val eventId: Int? = null,
    val eventCategoryId: Int? = null,
)
