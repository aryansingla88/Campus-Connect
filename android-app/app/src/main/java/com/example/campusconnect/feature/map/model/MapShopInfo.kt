package com.example.campusconnect.feature.map.model

data class MapShopInfo(
    val id: String,
    val name: String,
    val category: String,             // Replaced 'type' with 'category' to match Backend & POI standards
    val description: String? = null,  // Made nullable for safe JSON parsing
    val openingTime: String? = null,
    val closingTime: String? = null,
    val isOpen: Boolean = true,
    val contactNumber: String? = null // Renamed from 'phone' to match FakeMapRepo
)