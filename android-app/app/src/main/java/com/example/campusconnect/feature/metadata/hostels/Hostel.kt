package com.example.campusconnect.feature.metadata.hostels

data class Hostel(
    val id: Int,
    val hostel: String,
    val name: String,
    val active: Boolean
) {
    fun displayLabel(): String = "$hostel ($name)"
}
