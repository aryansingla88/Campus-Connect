package com.example.campusconnect.feature.events.data.remote.request

import com.google.gson.annotations.SerializedName

/** Body for granting a user access to an event. Matches addedUsers.add(user) in EventAccessDialog. */
data class GrantAccessRequest(
    @SerializedName("user_id")
    val userId  : Int
)