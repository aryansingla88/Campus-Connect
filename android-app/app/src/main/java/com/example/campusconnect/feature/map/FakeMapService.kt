package com.example.campusconnect.feature.map

import com.example.campusconnect.feature.map.data.fake.FakeEventMapData
import com.example.campusconnect.feature.map.data.fake.FakePoiMapData
import com.example.campusconnect.feature.map.data.fake.FakeShopMapData
import com.example.campusconnect.feature.map.data.fake.FakeUserMapData
import com.example.campusconnect.feature.map.mapengine.MapMarker

class FakeMapService {

    fun getMarkers(): List<MapMarker> {
        return FakeEventMapData.getEvents() +
                FakePoiMapData.getPois() +
                FakeShopMapData.getShops() +
                FakeUserMapData.getUsers()
    }
}