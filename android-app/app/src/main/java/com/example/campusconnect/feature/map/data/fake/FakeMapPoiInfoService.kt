package com.example.campusconnect.feature.map.data.fake

import com.example.campusconnect.feature.map.model.MapPoiInfo

object FakeMapPoiInfoService {

    fun getPoiInfo(
        poiId: Int,
        fallbackName: String = "Campus POI"
    ): MapPoiInfo {
        return when (poiId) {

            1 -> MapPoiInfo(
                id = poiId,
                name = "Main Gate",
                category = "ENTRY_GATE",
                description = "The Main Gate of the National Institute of Technology, Kurukshetra is located along Kirmich Road.",
                iconType = "GATE",
                visibility = "PUBLIC",
                priority = 10,
                sizeString = "LARGE"
            )

            2 -> MapPoiInfo(
                id = poiId,
                name = "Library",
                category = "ACADEMIC",
                description = "The Library of the National Institute of Technology, Kurukshetra serves as an important academic.",
                iconType = "BOOK",
                visibility = "PUBLIC",
                priority = 5,
                sizeString = "MEDIUM"
            )

            else -> MapPoiInfo(
                id = poiId,
                name = fallbackName,
                category = "GENERAL",
                description = "$fallbackName is an important point of interest on campus. More details will be loaded later.",
                iconType = "DEFAULT",
                visibility = "PUBLIC",
                priority = 0,
                sizeString = "MEDIUM"
            )
        }
    }
}