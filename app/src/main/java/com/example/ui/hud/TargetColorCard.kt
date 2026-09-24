package com.example.ui.hud

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.HuntColor
import com.example.ui.theme.CyberBorder
import com.example.ui.theme.CyberObsidian
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonPink
import com.example.ui.theme.NeonViolet
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun TargetColorCard(
    target: HuntColor,
    accuracy: Float,
    toleranceThreshold: Float,
    isMatching: Boolean,
    onRespinClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val animatedAccuracy by animateFloatAsState(
        targetValue = accuracy / 100f,
        animationSpec = tween(150, easing = FastOutSlowInEasing),
        label = "accuracy_progress"
    )

    val gaugeColor by animateColorAsState(
        targetValue = when {
            accuracy >= toleranceThreshold -> NeonGreen
            accuracy >= toleranceThreshold - 15f -> NeonCyan
            accuracy >= 45f -> NeonPink
            else -> TextSecondary
        },
        label = "gauge_color"
    )

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = CyberSurface.copy(alpha = 0.94f),
        border = androidx.compose.foundation.BorderStroke(
            width = if (isMatching) 2.dp else 1.dp,
            color = if (isMatching) NeonGreen else CyberBorder
        ),
        shadowElevation = 6.dp,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 4.dp)
            .testTag("target_color_card")
    ) {
        Column(
            modifier = Modifier.padding(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Color Preview Disc with Glow
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(target.color)
                        .border(2.5.dp, Color.White, CircleShape)
                        .shadow(8.dp, CircleShape, spotColor = target.color)
                )

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Category Chip
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = CyberObsidian
                        ) {
                            Text(
                                text = target.category.displayNameRu.uppercase(),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = NeonCyan,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                            )
                        }

                        // Rarity Chip
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = target.rarity.badgeColor.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = target.rarity.labelRu.uppercase(),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                color = target.rarity.badgeColor,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                            )
                        }
                    }

                    Text(
                        text = target.nameRu,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        color = TextPrimary
                    )

                    Text(
                        text = "Подсказка: ${target.hintRu}",
                        fontSize = 11.sp,
                        color = TextSecondary,
                        maxLines = 1
                    )
                }

                // Respin Roulette Button
                IconButton(
                    onClick = onRespinClick,
                    modifier = Modifier
                        .size(36.dp)
                        .background(CyberObsidian, CircleShape)
                        .testTag("respin_roulette_icon_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Casino,
                        contentDescription = "Сменить цвет",
                        tint = NeonViolet,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Accuracy progress bar & live label
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = when {
                        isMatching -> "🎯 ТОЧНОЕ ПОПАДАНИЕ! УДЕРЖИВАЙТЕ..."
                        accuracy >= toleranceThreshold - 15f -> "🔥 Очень близко к цели!"
                        accuracy >= 35f -> "🔍 Похожий оттенок"
                        else -> "Наведите камеру на нужный цвет..."
                    },
                    fontSize = 11.sp,
                    fontWeight = if (isMatching) FontWeight.Bold else FontWeight.Normal,
                    color = if (isMatching) NeonGreen else TextSecondary
                )

                Text(
                    text = "${accuracy.toInt()}%",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    color = gaugeColor
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            LinearProgressIndicator(
                progress = { animatedAccuracy },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = gaugeColor,
                trackColor = CyberObsidian,
                strokeCap = StrokeCap.Round
            )
        }
    }
}
