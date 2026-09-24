package com.example.ui.hud

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonPink
import com.example.ui.theme.NeonRed
import com.example.ui.theme.TextPrimary
import com.example.util.ColorUtils

@Composable
fun CameraReticle(
    sampledR: Int,
    sampledG: Int,
    sampledB: Int,
    accuracy: Float,
    toleranceThreshold: Float,
    lockOnProgress: Float,
    isCelebrating: Boolean,
    modifier: Modifier = Modifier
) {
    val sampledColor = remember(sampledR, sampledG, sampledB) {
        ColorUtils.toColor(sampledR, sampledG, sampledB)
    }

    val isMatching = accuracy >= toleranceThreshold

    val reticleColor by animateColorAsState(
        targetValue = when {
            isCelebrating || lockOnProgress >= 0.95f -> NeonGreen
            isMatching -> NeonGreen
            accuracy >= toleranceThreshold - 15f -> NeonCyan
            accuracy >= 45f -> NeonPink
            else -> Color(0xCCFFFFFF)
        },
        label = "reticle_color"
    )

    val reticleScale by animateFloatAsState(
        targetValue = if (isCelebrating) 1.25f else if (isMatching) 1.08f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "reticle_scale"
    )

    // Rotating tech ticks
    val infiniteTransition = rememberInfiniteTransition(label = "reticle_rotation")
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 16000, easing = LinearEasing)
        ),
        label = "reticle_rotate"
    )

    Box(
        modifier = modifier
            .size(240.dp)
            .scale(reticleScale)
            .testTag("camera_reticle"),
        contentAlignment = Alignment.Center
    ) {
        // Sci-Fi Outer Viewfinder Canvas (Ticks & Corner Brackets)
        Canvas(modifier = Modifier.size(240.dp)) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val bracketLength = 22.dp.toPx()
            val bracketOffset = 24.dp.toPx()
            val strokeWidth = 2.5.dp.toPx()

            // Corner Brackets: Top-Left
            drawLine(
                color = reticleColor,
                start = Offset(bracketOffset, bracketOffset + bracketLength),
                end = Offset(bracketOffset, bracketOffset),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round
            )
            drawLine(
                color = reticleColor,
                start = Offset(bracketOffset, bracketOffset),
                end = Offset(bracketOffset + bracketLength, bracketOffset),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round
            )

            // Top-Right
            drawLine(
                color = reticleColor,
                start = Offset(size.width - bracketOffset - bracketLength, bracketOffset),
                end = Offset(size.width - bracketOffset, bracketOffset),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round
            )
            drawLine(
                color = reticleColor,
                start = Offset(size.width - bracketOffset, bracketOffset),
                end = Offset(size.width - bracketOffset, bracketOffset + bracketLength),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round
            )

            // Bottom-Left
            drawLine(
                color = reticleColor,
                start = Offset(bracketOffset, size.height - bracketOffset - bracketLength),
                end = Offset(bracketOffset, size.height - bracketOffset),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round
            )
            drawLine(
                color = reticleColor,
                start = Offset(bracketOffset, size.height - bracketOffset),
                end = Offset(bracketOffset + bracketLength, size.height - bracketOffset),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round
            )

            // Bottom-Right
            drawLine(
                color = reticleColor,
                start = Offset(size.width - bracketOffset - bracketLength, size.height - bracketOffset),
                end = Offset(size.width - bracketOffset, size.height - bracketOffset),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round
            )
            drawLine(
                color = reticleColor,
                start = Offset(size.width - bracketOffset, size.height - bracketOffset - bracketLength),
                end = Offset(size.width - bracketOffset, size.height - bracketOffset),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round
            )

            // Crosshair Guides
            val guideDist = 58.dp.toPx()
            val guideLen = 14.dp.toPx()
            drawLine(
                color = reticleColor.copy(alpha = 0.6f),
                start = Offset(center.x, center.y - guideDist - guideLen),
                end = Offset(center.x, center.y - guideDist),
                strokeWidth = 2f
            )
            drawLine(
                color = reticleColor.copy(alpha = 0.6f),
                start = Offset(center.x, center.y + guideDist),
                end = Offset(center.x, center.y + guideDist + guideLen),
                strokeWidth = 2f
            )
            drawLine(
                color = reticleColor.copy(alpha = 0.6f),
                start = Offset(center.x - guideDist - guideLen, center.y),
                end = Offset(center.x - guideDist, center.y),
                strokeWidth = 2f
            )
            drawLine(
                color = reticleColor.copy(alpha = 0.6f),
                start = Offset(center.x + guideDist, center.y),
                end = Offset(center.x + guideDist + guideLen, center.y),
                strokeWidth = 2f
            )

            // Lock-On Circular Progress Ring
            if (lockOnProgress > 0f) {
                val lockRadius = 50.dp.toPx()
                drawArc(
                    color = NeonGreen,
                    startAngle = -90f,
                    sweepAngle = lockOnProgress * 360f,
                    useCenter = false,
                    topLeft = Offset(center.x - lockRadius, center.y - lockRadius),
                    size = Size(lockRadius * 2, lockRadius * 2),
                    style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
                )
            }
        }

        // Center Color Sampling Lens & Hub
        if (!isCelebrating) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(sampledColor)
                    .border(3.5.dp, reticleColor, CircleShape)
                    .shadow(16.dp, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                // Center reticle dot
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                )
            }
        } else {
            // Triumphant Checkmark on Success!
            Box(
                modifier = Modifier
                    .size(92.dp)
                    .clip(CircleShape)
                    .background(NeonGreen)
                    .border(4.dp, Color.White, CircleShape)
                    .shadow(24.dp, CircleShape, spotColor = NeonGreen),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Цвет найден!",
                    tint = Color.Black,
                    modifier = Modifier.size(54.dp)
                )
            }
        }

        // Bottom Match Percentage Badge
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 6.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = Color.Black.copy(alpha = 0.75f),
                border = androidx.compose.foundation.BorderStroke(1.dp, reticleColor),
                modifier = Modifier.shadow(8.dp, CircleShape)
            ) {
                Text(
                    text = if (isCelebrating) "ПОЙМАНО! ✓" else "${accuracy.toInt()}% СОВПАДЕНИЕ",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp,
                    color = reticleColor,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                )
            }
        }
    }
}
