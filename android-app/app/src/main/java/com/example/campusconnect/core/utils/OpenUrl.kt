package com.example.campusconnect.core.utils

import android.content.Context
import android.content.Intent
import android.net.Uri

fun openUrl(
    context: Context,
    value: String
) {
    val url = if (
        value.startsWith("http://") ||
        value.startsWith("https://")
    ) {
        value
    } else {
        "https://$value"
    }

    val intent = Intent(
        Intent.ACTION_VIEW,
        Uri.parse(url)
    )

    context.startActivity(intent)
}