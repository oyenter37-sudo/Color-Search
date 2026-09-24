package com.example.ui.hud

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun TestPaletteStrip(
    targetColor: HuntColor,
    onSampleColor: (r: Int, g: Int, b: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val sampleList = remember(targetColor) {
        // Provide the target color itself, plus a few nearby/distractor shades for testing
        val list = mutableListOf(targetColor)
        list.addAll(ColorCatalog.ALL_COLORS.filter { it.id != targetColor.id }.shuffled().take(6))
        list
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = CyberSurface.copy(alpha = 0.95f),
        border = androidx.compose.foundation.BorderStroke(1.dp, CyberBorder),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .testTag("test_palette_strip")
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "ТЕСТОВЫЙ СЭМПЛЕР (ДЛЯ ЭМУЛЯТОРА)",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = NeonCyan
                )
                Text(
                    text = "Нажмите для имитации цвета",
                    fontSize = 10.sp,
                    color = TextSecondary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(horizontal = 4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(sampleList) { colorItem ->
                    val isTarget = colorItem.id == targetColor.id
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .clickable {
                                onSampleColor(
                                    colorItem.redInt,
                                    colorItem.greenInt,
                                    colorItem.blueInt
                                )
                            }
                            .padding(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(colorItem.color)
                                .border(
                                    width = if (isTarget) 2.5.dp else 1.dp,
                                    color = if (isTarget) NeonCyan else Color.White.copy(alpha = 0.5f),
                                    shape = CircleShape
                                )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (isTarget) "ЦЕЛЬ" else colorItem.nameRu.take(5) + "..",
                            fontSize = 9.sp,
                            fontWeight = if (isTarget) FontWeight.Black else FontWeight.Normal,
                            color = if (isTarget) NeonCyan else TextPrimary
                        )
                    }
                }
            }
        }
    }
}
