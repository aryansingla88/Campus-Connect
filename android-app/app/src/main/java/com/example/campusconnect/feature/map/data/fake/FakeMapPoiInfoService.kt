package com.example.campusconnect.feature.map.data.fake

import com.example.campusconnect.feature.map.model.MapPoiInfo

object FakeMapPoiInfoService {

    fun getPoiInfo(
        poiId: String,
        fallbackName: String
    ): MapPoiInfo {
        return when (poiId) {

            "poi_main_gate",
            "main_gate",
            "poi_1" -> MapPoiInfo(
                id = poiId,
                name = "Main Gate",
                type = "poi",
                description = "The Main Gate of the National Institute of Technology, Kurukshetra is located along Kirmich Road."
            )

            "poi_library",
            "library",
            "poi_2" -> MapPoiInfo(
                id = poiId,
                name = "Library",
                type = "poi",
                description = "The Library of the National Institute of Technology, Kurukshetra serves as an important academic."
            )

            else -> MapPoiInfo(
                id = poiId,
                name = fallbackName,
                type = "poi",
                description = "$fallbackName is an important point of interest on campus. More details will be loaded later."
            )
        }
    }
}