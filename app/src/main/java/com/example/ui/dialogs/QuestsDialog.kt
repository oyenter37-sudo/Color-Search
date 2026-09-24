package com.example.ui.dialogs

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.local.FoundColorEntity
import com.example.model.QuestCatalog
import com.example.ui.theme.CyberBorder
import com.example.ui.theme.CyberObsidian
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.CyberSurfaceVariant
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonViolet
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun QuestsDialog(
    history: List<FoundColorEntity>,
    sessionStreak: Int,
    onDismiss: () -> Unit
) {
    val quests = remember { QuestCatalog.QUESTS }

    // Calculate total XP and level
    val totalXp = remember(history, sessionStreak) {
        quests.sumOf { quest ->
            val progress = quest.evaluateProgress(history, sessionStreak)
            if (progress >= quest.targetCount) quest.rewardXp else 0
        }
    }

    val completedCount = remember(history, sessionStreak) {
        quests.count { quest ->
            quest.evaluateProgress(history, sessionStreak) >= quest.targetCount
        }
    }

    val hunterRank = when {
        completedCount >= 7 -> "Грандмастер Спектра"
        completedCount >= 5 -> "Эксперт Хроматики"
        completedCount >= 3 -> "Следопыт Оттенков"
        completedCount >= 1 -> "Младший Колорист"
        else -> "Новичок Радуги"
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .background(CyberObsidian)
                .testTag("quests_dialog"),
            color = CyberObsidian
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 28.dp, bottom = 16.dp, start = 16.dp, end = 16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "КВЕСТЫ ОХОТНИКА",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp,
                            color = NeonAmber
                        )
                        Text(
                            text = "Выполнено: $completedCount из ${quests.size}",
                            fontSize = 14.sp,
                            color = TextSecondary
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(40.dp)
                            .background(CyberSurfaceVariant, CircleShape)
                            .testTag("close_quests_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Закрыть",
                            tint = TextPrimary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Hunter Rank Card
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = CyberSurface,
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, NeonAmber.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(listOf(NeonAmber, Color(0xFFFF5400)))
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.EmojiEvents,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column {
                            Text(
                                text = "ВАШ РАНГ",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = NeonAmber
                            )
                            Text(
                                text = hunterRank,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Black,
                                color = TextPrimary
                            )
                            Text(
                                text = "Накоплено $totalXp XP",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Quests List
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(quests) { quest ->
                        val currentProgress = quest.evaluateProgress(history, sessionStreak)
                        val isDone = currentProgress >= quest.targetCount
                        val progressFraction = (currentProgress.toFloat() / quest.targetCount.toFloat()).coerceIn(0f, 1f)

                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = CyberSurface,
                            border = androidx.compose.foundation.BorderStroke(
                                width = 1.dp,
                                color = if (isDone) NeonGreen.copy(alpha = 0.6f) else CyberBorder
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = quest.titleRu,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isDone) NeonGreen else TextPrimary
                                    )

                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = if (isDone) NeonGreen.copy(alpha = 0.2f) else NeonAmber.copy(alpha = 0.15f)
                                    ) {
                                        Text(
                                            text = if (isDone) "ВЫПОЛНЕНО" else "+${quest.rewardXp} XP",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Black,
                                            color = if (isDone) NeonGreen else NeonAmber,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = quest.descriptionRu,
                                    fontSize = 12.sp,
                                    color = TextSecondary,
                                    lineHeight = 16.sp
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "Прогресс: $currentProgress / ${quest.targetCount}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (isDone) NeonGreen else TextSecondary
                                    )

                                    if (isDone) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = NeonGreen,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                LinearProgressIndicator(
                                    progress = { progressFraction },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(5.dp)
                                        .clip(RoundedCornerShape(3.dp)),
                                    color = if (isDone) NeonGreen else NeonCyan,
                                    trackColor = CyberObsidian,
                                    strokeCap = StrokeCap.Round
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
