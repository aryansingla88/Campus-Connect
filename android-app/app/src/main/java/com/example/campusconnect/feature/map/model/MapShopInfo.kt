package com.example.campusconnect.feature.map.model

data class MapShopInfo(
    val id: Int,                         // Strict Int ID
    val name: String,
    val category: String,
    val description: String? = null,
    val openingTime: String? = null,
    val closingTime: String? = null,
    val isOpen: Boolean = true,
    val contactNumber: String? = null
)