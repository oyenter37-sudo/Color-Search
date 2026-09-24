package com.example.ui.roulette

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ColorCatalog
import com.example.model.HuntColor
import com.example.ui.theme.CyberBorder
import com.example.ui.theme.CyberObsidian
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonPink
import com.example.ui.theme.NeonViolet
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.SoundManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun ColorRouletteWheel(
    soundManager: SoundManager,
    onColorSelected: (HuntColor) -> Unit,
    modifier: Modifier = Modifier,
    initialAutoSpin: Boolean = true
) {
    val colors = remember { ColorCatalog.ALL_COLORS }
    val sliceCount = colors.size
    val sliceDegrees = 360f / sliceCount

    val rotationAngle = remember { Animatable(0f) }
    var isSpinning by remember { mutableStateOf(false) }
    var winningColor by remember { mutableStateOf<HuntColor?>(null) }
    val scope = rememberCoroutineScope()

    // Needle bounce effect
    var needleAngle by remember { mutableFloatStateOf(0f) }
    var lastTickSlice by remember { mutableIntStateOf(0) }

    fun startSpin() {
        if (isSpinning) return
        isSpinning = true
        winningColor = null

        scope.launch {
            val fullRotations = Random.nextInt(5, 8) * 360f
            val randomSliceIndex = Random.nextInt(sliceCount)
            val chosenTarget = colors[randomSliceIndex]

            // Top pointer is at -90 degrees (270 degrees).
            // When wheel has rotation R, slice i is centered at: (i * sliceDegrees + sliceDegrees / 2 + R) % 360.
            // We want that slice center to be at 270 degrees.
            // So: R = 270 - (randomSliceIndex * sliceDegrees + sliceDegrees / 2) + fullRotations
            val targetWheelAngle = (270f - (randomSliceIndex * sliceDegrees + sliceDegrees / 2f)) + fullRotations

            var lastAngle = rotationAngle.value
            rotationAngle.animateTo(
                targetValue = targetWheelAngle,
                animationSpec = tween(
                    durationMillis = 4200,
                    easing = CubicBezierEasing(0.15f, 0.90f, 0.25f, 1.0f)
                )
            ) {
                // Monitor rotation for tick sounds & needle deflection
                val currentNormAngle = (value % 360f + 360f) % 360f
                val currentSlice = (currentNormAngle / sliceDegrees).toInt()
                if (currentSlice != lastTickSlice) {
                    lastTickSlice = currentSlice
                    soundManager.playTick()
                    soundManager.vibrateTick()
                    needleAngle = -14f
                } else {
                    needleAngle *= 0.85f
                }
                lastAngle = value
            }

            needleAngle = 0f
            winningColor = chosenTarget
            soundManager.playVictoryChord()
            soundManager.vibrateVictory()

            // Highlight pause, then deliver chosen color
            delay(1200)
            isSpinning = false
            onColorSelected(chosenTarget)
        }
    }

    LaunchedEffect(Unit) {
        if (initialAutoSpin) {
            delay(600)
            startSpin()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        NeonViolet.copy(alpha = 0.25f),
                        CyberObsidian.copy(alpha = 0.95f),
                        CyberObsidian
                    )
                )
            )
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Safe spacing below top navigation overlay
        Spacer(modifier = Modifier.height(48.dp))

        // Top Header
        Text(
            text = "РУЛЕТКА ОТТЕНКОВ",
            fontSize = 24.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 2.sp,
            color = NeonCyan
        )
        Text(
            text = if (isSpinning) "Выбираем цель охоты..." else "Крутите колесо и найдите цвет!",
            fontSize = 14.sp,
            color = TextSecondary,
            modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
        )

        // Roulette Canvas & Indicator
        Box(
            modifier = Modifier
                .size(310.dp)
                .testTag("roulette_wheel_container"),
            contentAlignment = Alignment.Center
        ) {
            // Glowing Backdrop Ring
            Canvas(modifier = Modifier.size(310.dp)) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            NeonPink.copy(alpha = 0.2f),
                            Color.Transparent
                        )
                    ),
                    radius = size.minDimension / 2f
                )
            }

            // Wheel Canvas
            Canvas(
                modifier = Modifier
                    .size(280.dp)
                    .clip(CircleShape)
            ) {
                val center = Offset(size.width / 2f, size.height / 2f)
                val radius = size.minDimension / 2f

                rotate(degrees = rotationAngle.value, pivot = center) {
                    for (i in colors.indices) {
                        val startAngle = i * sliceDegrees
                        val color = colors[i].color

                        // Draw Slice Wedge
                        drawArc(
                            color = color,
                            startAngle = startAngle,
                            sweepAngle = sliceDegrees,
                            useCenter = true,
                            topLeft = Offset.Zero,
                            size = Size(radius * 2f, radius * 2f)
                        )

                        // Draw Slice Border
                        drawArc(
                            color = Color(0x40FFFFFF),
                            startAngle = startAngle,
                            sweepAngle = sliceDegrees,
                            useCenter = true,
                            topLeft = Offset.Zero,
                            size = Size(radius * 2f, radius * 2f),
                            style = Stroke(width = 1.5f)
                        )

                        // Outer segment marker dot
                        val midAngleRad = (startAngle + sliceDegrees / 2f) * (PI / 180f)
                        val dotX = center.x + (radius * 0.82f) * cos(midAngleRad).toFloat()
                        val dotY = center.y + (radius * 0.82f) * sin(midAngleRad).toFloat()

                        drawCircle(
                            color = Color.White.copy(alpha = 0.85f),
                            radius = 3.5f,
                            center = Offset(dotX, dotY)
                        )
                    }
                }

                // Outer decorative ring
                drawCircle(
                    color = Color.White,
                    radius = radius - 1f,
                    style = Stroke(width = 3.5f)
                )
            }

            // Center Wheel Hub / Ruby Core
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .shadow(12.dp, CircleShape)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            listOf(CyberSurface, CyberObsidian)
                        )
                    )
                    .border(2.dp, NeonCyan, CircleShape)
                    .clickable(enabled = !isSpinning) { startSpin() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Casino,
                    contentDescription = "Крутить",
                    tint = if (isSpinning) NeonPink else NeonCyan,
                    modifier = Modifier.size(32.dp)
                )
            }

            // Top Pointer Needle
            Canvas(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .size(width = 32.dp, height = 36.dp)
            ) {
                rotate(degrees = needleAngle, pivot = Offset(size.width / 2f, 0f)) {
                    val path = Path().apply {
                        moveTo(size.width / 2f, size.height) // tip pointing down into wheel
                        lineTo(0f, 0f)
                        lineTo(size.width, 0f)
                        close()
                    }
                    // Outer glow
                    drawPath(path = path, color = Color.White)
                    // Inner colored needle
                    val innerPath = Path().apply {
                        moveTo(size.width / 2f, size.height - 4f)
                        lineTo(3f, 3f)
                        lineTo(size.width - 3f, 3f)
                        close()
                    }
                    drawPath(path = innerPath, color = NeonPink)
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Selected / Landing Banner
        if (winningColor != null) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = CyberSurface,
                border = androidx.compose.foundation.BorderStroke(2.dp, winningColor!!.color),
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(winningColor!!.color)
                            .border(2.dp, Color.White, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = "ВЫБРАН ЦВЕТ:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextSecondary
                        )
                        Text(
                            text = winningColor!!.nameRu,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = TextPrimary
                        )
                    }
                }
            }
        } else {
            Spacer(modifier = Modifier.height(64.dp))
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Spin Button
        Button(
            onClick = { startSpin() },
            enabled = !isSpinning,
            colors = ButtonDefaults.buttonColors(
                containerColor = NeonCyan,
                contentColor = Color.Black
            ),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth(0.75f)
                .height(54.dp)
                .testTag("spin_roulette_button")
        ) {
            Icon(Icons.Default.PlayArrow, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (isSpinning) "РУЛЕТКА КРУТИТСЯ..." else "КРУТИТЬ РУЛЕТКУ",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }
    }
}
