package com.example.campusconnect.feature.profile.data.remote.response

import com.google.gson.annotations.SerializedName

// Confirmed connection — user_friends.status = accepted
data class ConnectionResponse(
    @SerializedName("user_id")          val userId        : Int,
    @SerializedName("username")         val username      : String,
    @SerializedName("full_name")        val fullName      : String,
    @SerializedName("avatar_url")       val avatarUrl     : String?,
    @SerializedName("avatar_color")     val avatarColor   : String,   // server-assigned hex
    @SerializedName("course_name")      val courseName    : String,
    @SerializedName("admission_year")   val admissionYear : Int,
)

// Pending request — user_friends.status = pending
data class ConnectionRequestResponse(
    @SerializedName("user_id")          val userId        : Int,
    @SerializedName("username")         val username      : String,
    @SerializedName("full_name")        val fullName      : String,
    @SerializedName("avatar_url")       val avatarUrl     : String?,
    @SerializedName("avatar_color")     val avatarColor   : String,
    @SerializedName("direction")        val direction     : String,   // "incoming" | "outgoing"
    @SerializedName("created_at")       val createdAt     : String,
)