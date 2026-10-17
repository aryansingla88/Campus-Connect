package com.example.campusconnect.feature.map.ui.components.controls

import android.graphics.Bitmap
import android.graphics.Canvas as AndroidCanvas
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import com.example.campusconnect.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.math.hypot
import kotlin.math.roundToInt
import kotlin.random.Random

/** Shared transition controller so any screen can run work while the app is cloud-covered. */
class CloudTransitionState {
    val progress = Animatable(0f)
    private val transitionMutex = Mutex()
    private var pendingTransitions = 0

    var isRunning by mutableStateOf(false)
        private set

    suspend fun runEntry(action: suspend () -> Unit) {
        pendingTransitions += 1
        isRunning = true

        try {
            transitionMutex.withLock {
                // Cover the screen immediately instead of animating in
                // from an uncovered state.
                progress.snapTo(1f)

                action()

                delay(200)

                // Reveal the map only after it is ready.
                progress.animateTo(
                    0f,
                    tween(1400, easing = FastOutSlowInEasing)
                )
            }
        } finally {
            pendingTransitions -= 1
            isRunning = pendingTransitions > 0
        }
    }
}

val LocalCloudTransitionState = staticCompositionLocalOf<CloudTransitionState> {
    error("CloudTransitionState was not provided by AppNavHost")
}

@Composable
fun rememberCloudTransitionState(): CloudTransitionState = remember { CloudTransitionState() }

private const val CLOUD_COUNT = 100
private const val CLOUD_VECTOR_WIDTH = 200
private const val CLOUD_VECTOR_HEIGHT = 140
// Portrait map surfaces need more vertical samples than horizontal ones. The
// final row is partial, keeping the cloud count at exactly 100.
private const val CLOUD_GRID_COLUMNS = 7

private data class Cloud(
    val spriteIndex: Int,
    val cx: Float,
    val cy: Float,
    val widthFrac: Float,
    val delay: Float,
    val flip: Boolean,
    val rotation: Float,
    val alpha: Float,
    val speed: Float
)

/** Places 100 puffs from the seamless vector cloud set across the map. */
private fun buildClouds(): List<Cloud> {
    val random = Random(42)
    return (0 until CLOUD_COUNT).map { index ->
        val column = index % CLOUD_GRID_COLUMNS
        val row = index / CLOUD_GRID_COLUMNS
        val layer = when (index) {
            in 0..49 -> 0 // distant layer
            in 50..79 -> 1 // middle layer
            else -> 2 // foreground puffs
        }
        val cellWidth = 1.2f / CLOUD_GRID_COLUMNS
        val cellHeight = 1.2f / (CLOUD_COUNT.toFloat() / CLOUD_GRID_COLUMNS)
        val centerX = -0.1f + (column + 0.5f) * cellWidth +
                (random.nextFloat() - 0.5f) * cellWidth * 0.30f
        val centerY = -0.1f + (row + 0.5f) * cellHeight +
                (random.nextFloat() - 0.5f) * cellHeight * 0.30f

        val widthRange = when (layer) {
            0 -> 0.68f..0.86f
            1 -> 0.58f..0.76f
            else -> 0.50f..0.68f
        }
        val delayRange = when (layer) {
            0 -> 0f..0.15f
            1 -> 0.10f..0.25f
            else -> 0.20f..0.35f
        }

        Cloud(
            spriteIndex = random.nextInt(CLOUD_VECTOR_RESOURCES.size),
            cx = centerX,
            cy = centerY,
            widthFrac = random.nextFloat() * (widthRange.endInclusive - widthRange.start) +
                    widthRange.start,
            delay = random.nextFloat() * (delayRange.endInclusive - delayRange.start) +
                    delayRange.start,
            flip = random.nextBoolean(),
            rotation = (random.nextFloat() - 0.5f) * 6f,
            alpha = when (layer) {
                0 -> 0.74f
                1 -> 0.81f
                else -> 0.88f
            },
            speed = when (layer) {
                0 -> 0.82f
                1 -> 1f
                else -> 1.14f
            }
        )
    }.sortedBy { cloud ->
        when {
            cloud.speed < 0.9f -> 0
            cloud.speed < 1.1f -> 1
            else -> 2
        }
    }
}

/** App-wide overlay. Place after the navigation host so clouds cover every destination. */
@Composable
fun CloudTransitionOverlay(state: CloudTransitionState) {
    if (!state.isRunning) return
    BackHandler(enabled = true) {}

    val clouds = remember { buildClouds() }
    val context = LocalContext.current
    val cloudSprites = remember(context) {
        CLOUD_VECTOR_RESOURCES.map { resourceId ->
            val bitmap = Bitmap.createBitmap(
                CLOUD_VECTOR_WIDTH * 3,
                CLOUD_VECTOR_HEIGHT * 3,
                Bitmap.Config.ARGB_8888
            )
            val drawable = requireNotNull(context.getDrawable(resourceId))
            drawable.setBounds(0, 0, bitmap.width, bitmap.height)
            drawable.draw(AndroidCanvas(bitmap))
            bitmap.asImageBitmap()
        }
    }
    val interactionSource = remember { MutableInteractionSource() }

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = {}
            )
    ) {
        drawClouds(cloudSprites, clouds, state.progress.value)
    }
}

private val CLOUD_VECTOR_RESOURCES = listOf(
    R.drawable.map_cloud_vector_1,
    R.drawable.map_cloud_vector_2,
    R.drawable.map_cloud_vector_3,
    R.drawable.map_cloud_vector_4,
    R.drawable.map_cloud_vector_5
)

private fun DrawScope.drawClouds(
    cloudSprites: List<ImageBitmap>,
    clouds: List<Cloud>,
    progress: Float
) {
    val screenWidth = size.width
    val screenHeight = size.height
    val diagonal = hypot(screenWidth.toDouble(), screenHeight.toDouble()).toFloat()
    clouds.forEach { cloud ->
        val localProgress = ((progress - cloud.delay) / (1f - cloud.delay))
            .coerceIn(0f, 1f)
        val remaining = 1f - localProgress
        val dx = cloud.cx - 0.5f
        val dy = cloud.cy - 0.5f
        val distance = hypot(dx.toDouble(), dy.toDouble()).toFloat()
        val directionX = if (distance < 0.05f) 0f else dx / distance
        val directionY = if (distance < 0.05f) -1f else dy / distance

        val destinationWidth = (cloud.widthFrac * screenWidth).roundToInt().coerceAtLeast(1)
        val destinationHeight = (destinationWidth * CLOUD_VECTOR_HEIGHT.toFloat() / CLOUD_VECTOR_WIDTH)
            .roundToInt().coerceAtLeast(1)
        val left = cloud.cx * screenWidth - destinationWidth / 2f +
                directionX * diagonal * remaining * cloud.speed
        val top = cloud.cy * screenHeight - destinationHeight / 2f +
                directionY * diagonal * remaining * cloud.speed
        val pivot = Offset(destinationWidth / 2f, destinationHeight / 2f)

        withTransform({
            translate(left, top)
            rotate(cloud.rotation * remaining, pivot)
            if (cloud.flip) scale(-1f, 1f, pivot)
        }) {
            drawImage(
                image = cloudSprites[cloud.spriteIndex],
                dstOffset = IntOffset.Zero,
                dstSize = IntSize(destinationWidth, destinationHeight),
                alpha = cloud.alpha
            )
        }
    }
}
