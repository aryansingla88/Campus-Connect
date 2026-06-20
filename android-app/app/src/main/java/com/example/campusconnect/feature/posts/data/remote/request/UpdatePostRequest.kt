package com.example.campusconnect.feature.posts.data.remote.request

data class UpdatePostRequest(

    val title: String,

    val body: String,

    val tags: List<String>
)