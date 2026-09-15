package com.example.campusconnect.core.utils.Image

import android.content.Context
import android.net.Uri
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

object ImageConverter {

    fun uriToMultipart(
        context: Context,
        uri: Uri,
        partName: String,
        maxDimension: Int = 2048,
        quality: Int = 85
    ): MultipartBody.Part? {

        val preparedImage = ImageUtils.prepareForUpload(
            context = context,
            uri = uri,
            maxDimension = maxDimension,
            quality = quality
        ) ?: return null

        val requestBody = preparedImage.bytes.toRequestBody(
            preparedImage.mimeType.toMediaTypeOrNull()
        )

        return MultipartBody.Part.createFormData(
            name = partName,
            filename = preparedImage.fileName,
            body = requestBody
        )
    }
}