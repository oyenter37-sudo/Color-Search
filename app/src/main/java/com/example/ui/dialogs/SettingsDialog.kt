package com.example.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.CyberBorder
import com.example.ui.theme.CyberObsidian
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.CyberSurfaceVariant
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonPink
import com.example.ui.theme.NeonRed
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun SettingsDialog(
    toleranceThreshold: Float,
    onToleranceChange: (Float) -> Unit,
    isSoundEnabled: Boolean,
    onSoundToggle: (Boolean) -> Unit,
    isHapticEnabled: Boolean,
    onHapticToggle: (Boolean) -> Unit,
    onResetData: () -> Unit,
    onDismiss: () -> Unit
) {
    var showResetConfirm by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .background(CyberObsidian)
                .testTag("settings_dialog"),
            color = CyberObsidian
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 28.dp, bottom = 16.dp, start = 16.dp, end = 16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = null,
                            tint = NeonCyan,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "НАСТРОЙКИ",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp,
                            color = NeonCyan
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(40.dp)
                            .background(CyberSurfaceVariant, CircleShape)
                            .testTag("close_settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Закрыть",
                            tint = TextPrimary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Tolerance / Difficulty Level (Only Easy mode active as requested)
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = CyberSurface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, CyberBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "РЕЖИМ СЛОЖНОСТИ",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = NeonCyan
                            )
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = NeonGreen.copy(alpha = 0.18f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, NeonGreen.copy(alpha = 0.6f))
                            ) {
                                Text(
                                    text = "АКТИВЕН",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    color = NeonGreen,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "🟢 Лёгкий режим (50% точности)",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Погрешность смягчена! Вы легко найдете оттенки дома и на улице даже при обычном комнатном освещении и тенях.",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        FilterChip(
                            selected = true,
                            onClick = { onToleranceChange(50f) },
                            label = {
                                Text("✓ Лёгкий — комфортный поиск (50%)", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = NeonCyan,
                                selectedLabelColor = Color.Black,
                                containerColor = CyberObsidian,
                                labelColor = TextSecondary
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = true,
                                borderColor = NeonCyan,
                                selectedBorderColor = NeonCyan
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Sound & Haptics
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = CyberSurface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, CyberBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // Sound switch
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = null, tint = NeonPink)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text("Звуковые эффекты", fontWeight = FontWeight.Bold, color = TextPrimary)
                                    Text("Щелчки рулетки и победные фанфары", fontSize = 12.sp, color = TextSecondary)
                                }
                            }
                            Switch(
                                checked = isSoundEnabled,
                                onCheckedChange = onSoundToggle,
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.Black,
                                    checkedTrackColor = NeonCyan
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Haptic switch
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Vibration, contentDescription = null, tint = NeonCyan)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text("Вибрация", fontWeight = FontWeight.Bold, color = TextPrimary)
                                    Text("Тактильный отклик при захвате", fontSize = 12.sp, color = TextSecondary)
                                }
                            }
                            Switch(
                                checked = isHapticEnabled,
                                onCheckedChange = onHapticToggle,
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.Black,
                                    checkedTrackColor = NeonCyan
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Instructions Card
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = CyberSurface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, CyberBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.AutoMirrored.Filled.HelpOutline, contentDescription = null, tint = NeonGreen)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("КАК ИГРАТЬ", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = NeonGreen)
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        val steps = listOf(
                            "1. Рулетка крутится и выбирает случайный оттенок радуги.",
                            "2. Наведите кружок-выбиратель в центре экрана на предмет такого цвета.",
                            "3. Индикатор точности начнёт расти. При хорошем совпадении (>75%) появится кольцо удержания.",
                            "4. Удерживайте камеру 1 секунду — экран потрясётся, раздастся победный звон, и цвет занесётся в вашу коллекцию!",
                            "5. Выполняйте квесты и откройте все 20 уникальных оттенков!"
                        )

                        for (step in steps) {
                            Text(
                                text = step,
                                fontSize = 13.sp,
                                color = TextSecondary,
                                lineHeight = 18.sp,
                                modifier = Modifier.padding(vertical = 3.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Reset data button
                OutlinedButton(
                    onClick = { showResetConfirm = true },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonRed),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NeonRed.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Очистить коллекцию цветов")
                }
            }
        }
    }

    if (showResetConfirm) {
        AlertDialog(
            onDismissRequest = { showResetConfirm = false },
            confirmButton = {
                Button(
                    onClick = {
                        onResetData()
                        showResetConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonRed, contentColor = Color.White)
                ) {
                    Text("Да, очистить")
                }
            },
            dismissButton = {
                Button(
                    onClick = { showResetConfirm = false },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberObsidian, contentColor = TextPrimary)
                ) {
                    Text("Отмена")
                }
            },
            title = { Text("Очистить коллекцию?") },
            text = { Text("Все сохранённые найденные цвета и прогресс квестов будут сброшены.") },
            containerColor = CyberSurface,
            titleContentColor = TextPrimary,
            textContentColor = TextSecondary
        )
    }
}
