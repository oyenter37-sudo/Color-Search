package com.example.ui.celebration

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

data class ConfettiParticle(
    val startX: Float,
    val startY: Float,
    val angle: Double,
    val speed: Float,
    val size: Float,
    val color: Color,
    val rotationSpeed: Float
)

@Composable
fun ConfettiCanvas(
    isActive: Boolean,
    onFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (!isActive) return

    val progress = remember { Animatable(0f) }

    val particles = remember {
        val colors = listOf(
            Color(0xFFFF007F),
            Color(0xFF00F5D4),
            Color(0xFFFFEE00),
            Color(0xFF9D4EDD),
            Color(0xFFFF7B00),
            Color(0xFF06D6A0),
            Color(0xFF4361EE)
        )
        List(70) {
            val angle = Random.nextDouble(0.0, 2 * Math.PI)
            val speed = Random.nextFloat() * 450f + 250f
            ConfettiParticle(
                startX = 0.5f,
                startY = 0.5f,
                angle = angle,
                speed = speed,
                size = Random.nextFloat() * 12f + 8f,
                color = colors.random(),
                rotationSpeed = Random.nextFloat() * 720f - 360f
            )
        }
    }

    LaunchedEffect(isActive) {
        progress.snapTo(0f)
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1400, easing = LinearEasing)
        )
        onFinished()
    }

    val t = progress.value
    Canvas(modifier = modifier.fillMaxSize()) {
        val cx = size.width / 2f
        val cy = size.height / 2f

        for (p in particles) {
            val currentDist = p.speed * t
            // Add slight gravity
            val gravity = 400f * t * t
            val px = cx + (cos(p.angle) * currentDist).toFloat()
            val py = cy + (sin(p.angle) * currentDist).toFloat() + gravity

            val alpha = (1f - t).coerceIn(0f, 1f)
            val currentRotation = p.rotationSpeed * t

            rotate(degrees = currentRotation, pivot = Offset(px, py)) {
                drawRect(
                    color = p.color.copy(alpha = alpha),
                    topLeft = Offset(px - p.size / 2f, py - p.size / 2f),
                    size = Size(p.size, p.size * 0.7f)
                )
            }
        }
    }
}
