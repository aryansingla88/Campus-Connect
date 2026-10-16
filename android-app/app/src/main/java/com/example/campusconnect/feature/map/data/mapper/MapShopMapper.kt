package com.example.campusconnect.feature.map.data.mapper

import com.example.campusconnect.feature.map.data.remote.response.ShopResponse
import com.example.campusconnect.feature.map.model.MapShopInfo

/** Converts shop transport data into the shop detail model. */
object MapShopMapper {
    fun toInfo(response: ShopResponse): MapShopInfo = MapShopInfo(
        id = response.id,
        name = response.name,
        category = response.category ?: response.type ?: "SHOP",
        description = response.description,
        openingTime = response.openingTime,
        closingTime = response.closingTime,
        isOpen = response.isOpen ?: true,
        contactNumber = response.phone
    )
}
