package com.example.campusconnect.feature.map.components

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween

object MapMotion {

    const val Fast = 180
    const val Medium = 280
    const val Slow = 360

    val SmoothEasing = CubicBezierEasing(
        0.20f,
        0.00f,
        0.00f,
        1.00f
    )

    fun <T> tweenFast(): TweenSpec<T> {
        return tween(
            durationMillis = Fast,
            easing = SmoothEasing
        )
    }

    fun <T> tweenMedium(): TweenSpec<T> {
        return tween(
            durationMillis = Medium,
            easing = SmoothEasing
        )
    }

    fun <T> tweenSlow(): TweenSpec<T> {
        return tween(
            durationMillis = Slow,
            easing = SmoothEasing
        )
    }

    fun <T> springSoft(): SpringSpec<T> {
        return spring(
            dampingRatio = 0.86f,
            stiffness = 520f
        )
    }
}