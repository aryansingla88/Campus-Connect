package com.example.campusconnect.feature.map.model

data class MapShopInfo(
    val id: String,
    val name: String,
    val type: String,
    val description: String,
    val openingTime: String?,
    val closingTime: String?,
    val isOpen: Boolean,
    val phone: String?
)