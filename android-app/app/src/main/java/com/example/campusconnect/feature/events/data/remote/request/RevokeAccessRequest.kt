package com.example.campusconnect.feature.events.data.remote.request

import com.google.gson.annotations.SerializedName

/** Body for revoking a user's access. Matches addedUsers.remove(user) in EventAccessDialog. */
data class RevokeAccessRequest(
    @SerializedName("user_id")
    val userId  : Int
)