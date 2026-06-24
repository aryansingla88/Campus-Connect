package com.example.campusconnect.feature.map.data.fake

import com.example.campusconnect.feature.map.mapengine.MapMarker

class FakeMapService {

    fun getMarkers(): List<MapMarker> {
        return FakeEventMapData.getEvents() +
                FakePoiMapData.getPois() +
                FakeShopMapData.getShops() +
                FakeUserMapData.getUsers()
    }
}