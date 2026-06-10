package com.example.campusconnect.core.utils

fun String.toInitials(): String =
    trim()
        .split(Regex("\\s+"))
        .take(2)
        .joinToString("") {
            it.firstOrNull()?.uppercaseChar()?.toString() ?: ""
        }