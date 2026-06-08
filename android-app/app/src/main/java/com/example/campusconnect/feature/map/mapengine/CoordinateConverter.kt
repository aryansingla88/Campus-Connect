package com.example.campusconnect.feature.map.mapengine

import androidx.compose.ui.geometry.Offset
import kotlin.math.abs

class CoordinateConverter(
    lat1: Double,
    lng1: Double,
    x1: Float,
    y1: Float,

    lat2: Double,
    lng2: Double,
    x2: Float,
    y2: Float,

    lat3: Double,
    lng3: Double,
    x3: Float,
    y3: Float
) {

    private val a: Double
    private val b: Double
    private val c: Double

    private val d: Double
    private val e: Double
    private val f: Double

    init {
        val matrix = arrayOf(
            doubleArrayOf(lng1, lat1, 1.0),
            doubleArrayOf(lng2, lat2, 1.0),
            doubleArrayOf(lng3, lat3, 1.0)
        )

        val xValues = doubleArrayOf(x1.toDouble(), x2.toDouble(), x3.toDouble())
        val yValues = doubleArrayOf(y1.toDouble(), y2.toDouble(), y3.toDouble())

        val inverse = invert3x3(matrix)

        val resultX = multiply(inverse, xValues)
        val resultY = multiply(inverse, yValues)

        a = resultX[0]
        b = resultX[1]
        c = resultX[2]

        d = resultY[0]
        e = resultY[1]
        f = resultY[2]
    }

    fun latLngToPoint(lat: Double, lng: Double): Offset {
        val x = (a * lng + b * lat + c).toFloat()
        val y = (d * lng + e * lat + f).toFloat()
        return Offset(x, y)
    }

    fun pointToLatLng(x: Float, y: Float): Pair<Double, Double> {
        val det = a * e - b * d

        if (abs(det) < 1e-6) {
            return Pair(0.0, 0.0)
        }

        val lng = (e * (x - c) - b * (y - f)) / det
        val lat = (-d * (x - c) + a * (y - f)) / det

        return Pair(lat, lng)
    }

    private fun invert3x3(m: Array<DoubleArray>): Array<DoubleArray> {
        val det =
            m[0][0] * (m[1][1] * m[2][2] - m[1][2] * m[2][1]) -
                    m[0][1] * (m[1][0] * m[2][2] - m[1][2] * m[2][0]) +
                    m[0][2] * (m[1][0] * m[2][1] - m[1][1] * m[2][0])

        require(abs(det) > 1e-6) {
            "Invalid calibration points"
        }

        return arrayOf(
            doubleArrayOf(
                (m[1][1] * m[2][2] - m[1][2] * m[2][1]) / det,
                (m[0][2] * m[2][1] - m[0][1] * m[2][2]) / det,
                (m[0][1] * m[1][2] - m[0][2] * m[1][1]) / det
            ),
            doubleArrayOf(
                (m[1][2] * m[2][0] - m[1][0] * m[2][2]) / det,
                (m[0][0] * m[2][2] - m[0][2] * m[2][0]) / det,
                (m[0][2] * m[1][0] - m[0][0] * m[1][2]) / det
            ),
            doubleArrayOf(
                (m[1][0] * m[2][1] - m[1][1] * m[2][0]) / det,
                (m[0][1] * m[2][0] - m[0][0] * m[2][1]) / det,
                (m[0][0] * m[1][1] - m[0][1] * m[1][0]) / det
            )
        )
    }

    private fun multiply(matrix: Array<DoubleArray>, vector: DoubleArray): DoubleArray {
        return DoubleArray(3) { i ->
            matrix[i][0] * vector[0] +
                    matrix[i][1] * vector[1] +
                    matrix[i][2] * vector[2]
        }
    }
}