package com.example.campusconnect.feature.map.mapengine

class CoordinateConverter(
    private val mapWidth: Float,
    private val mapHeight: Float,
    private val minLat: Double,
    private val maxLat: Double,
    private val minLng: Double,
    private val maxLng: Double
) {

    private fun isValid(): Boolean {
        return mapWidth > 0f &&
                mapHeight > 0f &&
                maxLat != minLat &&
                maxLng != minLng
    }

    fun latLngToPoint(lat: Double, lng: Double): Pair<Float, Float> {
        if (!isValid()) return Pair(0f, 0f)

        val xRatio = (lng - minLng) / (maxLng - minLng)
        val yRatio = (lat - minLat) / (maxLat - minLat)

        val x = (xRatio * mapWidth).toFloat()
        val y = (mapHeight - (yRatio * mapHeight)).toFloat()

        return Pair(x, y)
    }

    fun pointToLatLng(x: Float, y: Float): Pair<Double, Double> {
        if (!isValid()) return Pair(0.0, 0.0)

        val xRatio = x / mapWidth
        val yRatio = 1.0 - (y / mapHeight)

        val lng = minLng + (xRatio * (maxLng - minLng))
        val lat = minLat + (yRatio * (maxLat - minLat))

        return Pair(lat, lng)
    }
}