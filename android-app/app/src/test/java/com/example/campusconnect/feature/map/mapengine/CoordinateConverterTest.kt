package com.example.campusconnect.feature.map.mapengine

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs

class CoordinateConverterTest {

    private val converter = MapCalibration.converter

    @Test
    fun testCalibrationPoint1ForwardConversion() {
        val lat = 29.943455
        val lng = 76.818978
        val expectedX = 922.11914f
        val expectedY = 2222.046f

        val point = converter.latLngToPoint(lat, lng)

        assertEquals(expectedX, point.x, 1.0f)
        assertEquals(expectedY, point.y, 1.0f)
    }

    @Test
    fun testCalibrationPoint2ForwardConversion() {
        val lat = 29.945381
        val lng = 76.813989
        val expectedX = 1285.951f
        val expectedY = 955.2599f

        val point = converter.latLngToPoint(lat, lng)

        assertEquals(expectedX, point.x, 1.0f)
        assertEquals(expectedY, point.y, 1.0f)
    }

    @Test
    fun testReversibility() {
        val originalLat = 29.946000
        val originalLng = 76.815000

        val point = converter.latLngToPoint(originalLat, originalLng)
        val (recoveredLat, recoveredLng) = converter.pointToLatLng(point.x, point.y)

        assertTrue("Latitude should match within epsilon", abs(originalLat - recoveredLat) < 0.0001)
        assertTrue("Longitude should match within epsilon", abs(originalLng - recoveredLng) < 0.0001)
    }
}
