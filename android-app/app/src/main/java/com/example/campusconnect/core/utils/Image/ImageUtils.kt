package com.example.campusconnect.core.utils.Image

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.OpenableColumns
import java.io.ByteArrayOutputStream

data class PreparedImage(
    val bytes: ByteArray,
    val mimeType: String,
    val fileName: String
)

object ImageUtils {

    private const val DEFAULT_FILE_NAME = "image.jpg"
    private const val MAX_DIMENSION = 2048
    private const val JPEG_QUALITY = 85

    fun prepareForUpload(
        context: Context,
        uri: Uri,
        maxDimension: Int = MAX_DIMENSION,
        quality: Int = JPEG_QUALITY
    ): PreparedImage? {

        val resolver = context.contentResolver

        val bounds = resolver.openInputStream(uri)?.use { input ->
            BitmapFactory.Options().apply {
                inJustDecodeBounds = true
                BitmapFactory.decodeStream(input, null, this)
            }
        } ?: return null

        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) {
            return null
        }

        val sampleSize = calculateSampleSize(
            width = bounds.outWidth,
            height = bounds.outHeight,
            maxDimension = maxDimension
        )

        val bitmap = resolver.openInputStream(uri)?.use { input ->
            BitmapFactory.Options().apply {
                inSampleSize = sampleSize
                inPreferredConfig = Bitmap.Config.ARGB_8888
            }.let { options ->
                BitmapFactory.decodeStream(input, null, options)
            }
        } ?: return null

        val resizedBitmap = resizeIfNeeded(
            bitmap = bitmap,
            maxDimension = maxDimension
        )

        val outputStream = ByteArrayOutputStream()

        val compressed = resizedBitmap.compress(
            Bitmap.CompressFormat.JPEG,
            quality.coerceIn(0, 100),
            outputStream
        )

        if (resizedBitmap !== bitmap) {
            resizedBitmap.recycle()
        }

        bitmap.recycle()

        if (!compressed) {
            return null
        }

        return PreparedImage(
            bytes = outputStream.toByteArray(),
            mimeType = "image/jpeg",
            fileName = buildJpegFileName(
                getFileName(context, uri)
            )
        )
    }

    fun getMimeType(
        context: Context,
        uri: Uri
    ): String {
        return context.contentResolver.getType(uri)
            ?: "image/jpeg"
    }

    fun getFileName(
        context: Context,
        uri: Uri
    ): String? {

        context.contentResolver.query(
            uri,
            arrayOf(OpenableColumns.DISPLAY_NAME),
            null,
            null,
            null
        )?.use { cursor ->

            val nameIndex =
                cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)

            if (nameIndex >= 0 && cursor.moveToFirst()) {
                return cursor.getString(nameIndex)
            }
        }

        return null
    }

    private fun buildJpegFileName(
        originalName: String?
    ): String {

        if (originalName.isNullOrBlank()) {
            return DEFAULT_FILE_NAME
        }

        val baseName = originalName.substringBeforeLast(
            '.',
            originalName
        )

        return "$baseName.jpg"
    }

    private fun calculateSampleSize(
        width: Int,
        height: Int,
        maxDimension: Int
    ): Int {

        var sampleSize = 1

        while (
            width / (sampleSize * 2) >= maxDimension &&
            height / (sampleSize * 2) >= maxDimension
        ) {
            sampleSize *= 2
        }

        return sampleSize
    }

    private fun resizeIfNeeded(
        bitmap: Bitmap,
        maxDimension: Int
    ): Bitmap {

        val width = bitmap.width
        val height = bitmap.height
        val largestDimension = maxOf(width, height)

        if (largestDimension <= maxDimension) {
            return bitmap
        }

        val scale =
            maxDimension.toFloat() / largestDimension

        val newWidth =
            (width * scale).toInt().coerceAtLeast(1)

        val newHeight =
            (height * scale).toInt().coerceAtLeast(1)

        return Bitmap.createScaledBitmap(
            bitmap,
            newWidth,
            newHeight,
            true
        )
    }
}