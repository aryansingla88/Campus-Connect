package com.example.campusconnect.feature.posts.utils

import android.content.Context
import android.net.Uri
import java.io.File

fun Uri.toTemporaryImageFile(context: Context): File? {
    return runCatching {
        val extension = context.contentResolver.getType(this)
            ?.substringAfterLast('/', "jpg")
            ?.let { ".$it" }
            ?: ".jpg"

        val file = File.createTempFile("post_image_", extension, context.cacheDir)
        context.contentResolver.openInputStream(this)?.use { input ->
            file.outputStream().use { output -> input.copyTo(output) }
        } ?: return null

        file
    }.getOrNull()
}
