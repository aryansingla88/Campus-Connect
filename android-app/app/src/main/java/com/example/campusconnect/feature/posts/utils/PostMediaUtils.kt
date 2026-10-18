package com.example.campusconnect.feature.posts.utils

import com.example.campusconnect.core.network.ApiConfig

fun resolvePostImageUrl(imageUrl: String?): String? {
    if (imageUrl.isNullOrBlank()) return null
    if (imageUrl.startsWith("http://") || imageUrl.startsWith("https://")) return imageUrl
    return ApiConfig.BASE_URL.trimEnd('/') + "/" + imageUrl.trimStart('/')
}
