package com.example.ui.hud

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.FlipCameraAndroid
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.camera.CameraPreview
import com.example.ui.celebration.ConfettiCanvas
import com.example.ui.dialogs.ColorDexDialog
import com.example.ui.dialogs.QuestsDialog
import com.example.ui.dialogs.SettingsDialog
import com.example.ui.roulette.ColorRouletteWheel
import com.example.ui.theme.CyberBorder
import com.example.ui.theme.CyberObsidian
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonPink
import com.example.ui.theme.NeonViolet
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.ActiveDialog
import com.example.viewmodel.GamePhase
import com.example.viewmodel.GameViewModel
import kotlin.math.roundToInt

@Composable
fun MainGameScreen(
    viewModel: GameViewModel
) {
    val gamePhase by viewModel.gamePhase.collectAsState()
    val targetColor by viewModel.targetColor.collectAsState()
    val sampledR by viewModel.sampledR.collectAsState()
    val sampledG by viewModel.sampledG.collectAsState()
    val sampledB by viewModel.sampledB.collectAsState()
    val liveAccuracy by viewModel.liveAccuracy.collectAsState()
    val lockOnProgress by viewModel.lockOnProgress.collectAsState()
    val toleranceThreshold by viewModel.toleranceThreshold.collectAsState()
    val isTorchOn by viewModel.isTorchOn.collectAsState()
    val useFrontCamera by viewModel.useFrontCamera.collectAsState()
    val showTestPalette by viewModel.showTestPalette.collectAsState()
    val activeDialog by viewModel.activeDialog.collectAsState()
    val foundHistory by viewModel.foundHistory.collectAsState()
    val distinctFoundCount by viewModel.distinctFoundCount.collectAsState()
    val sessionStreak by viewModel.sessionStreak.collectAsState()

    val shakeX = viewModel.shakeOffsetX.value
    val shakeY = viewModel.shakeOffsetY.value

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberObsidian)
            .offset { IntOffset(shakeX.roundToInt(), shakeY.roundToInt()) }
            .testTag("main_game_screen")
    ) {
        // Main Content depending on GamePhase
        if (gamePhase == GamePhase.ROULETTE) {
            ColorRouletteWheel(
                soundManager = viewModel.soundManager,
                onColorSelected = { color -> viewModel.onRouletteColorChosen(color) },
                modifier = Modifier.fillMaxSize()
            )
        } else {
            // Camera Mode (HUNTING or CELEBRATING)
            CameraPreview(
                isTorchOn = isTorchOn,
                useFrontCamera = useFrontCamera,
                onColorSampled = { r, g, b -> viewModel.onColorSampled(r, g, b) },
                modifier = Modifier.fillMaxSize()
            )

            // Centered Reticle Viewfinder ("штучка-выбиратель")
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CameraReticle(
                    sampledR = sampledR,
                    sampledG = sampledG,
                    sampledB = sampledB,
                    accuracy = liveAccuracy,
                    toleranceThreshold = toleranceThreshold,
                    lockOnProgress = lockOnProgress,
                    isCelebrating = gamePhase == GamePhase.CELEBRATING
                )
            }

            // Top Target Card (with status bars padding)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .windowInsetsPadding(WindowInsets.statusBars)
            ) {
                TargetColorCard(
                    target = targetColor,
                    accuracy = liveAccuracy,
                    toleranceThreshold = toleranceThreshold,
                    isMatching = liveAccuracy >= toleranceThreshold,
                    onRespinClick = { viewModel.openRoulette() }
                )
            }

            // Bottom Controls Bar (with navigation bars padding)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .padding(bottom = 12.dp)
            ) {
                // Optional Test Palette strip for emulator or virtual tests
                AnimatedVisibility(
                    visible = showTestPalette,
                    enter = slideInVertically { it } + fadeIn(),
                    exit = slideOutVertically { it } + fadeOut()
                ) {
                    TestPaletteStrip(
                        targetColor = targetColor,
                        onSampleColor = { r, g, b -> viewModel.onColorSampled(r, g, b) }
                    )
                }

                // Floating Action Glass Dock
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = CyberSurface.copy(alpha = 0.92f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CyberBorder),
                    shadowElevation = 12.dp,
                    modifier = Modifier
                        .padding(horizontal = 20.dp, vertical = 6.dp)
                        .fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Flashlight / Torch
                        IconButton(
                            onClick = { viewModel.toggleTorch() },
                            modifier = Modifier
                                .size(44.dp)
                                .background(
                                    if (isTorchOn) NeonAmber.copy(alpha = 0.25f) else CyberObsidian,
                                    CircleShape
                                )
                                .testTag("toggle_torch_button")
                        ) {
                            Icon(
                                imageVector = if (isTorchOn) Icons.Default.FlashOn else Icons.Default.FlashOff,
                                contentDescription = "Фонарик",
                                tint = if (isTorchOn) NeonAmber else TextSecondary
                            )
                        }

                        // Flip Camera
                        IconButton(
                            onClick = { viewModel.toggleFrontCamera() },
                            modifier = Modifier
                                .size(44.dp)
                                .background(CyberObsidian, CircleShape)
                                .testTag("flip_camera_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.FlipCameraAndroid,
                                contentDescription = "Переключить камеру",
                                tint = NeonCyan
                            )
                        }

                        // Re-spin Wheel central button
                        Surface(
                            shape = CircleShape,
                            color = NeonViolet,
                            shadowElevation = 8.dp,
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .clickable { viewModel.openRoulette() }
                                .testTag("reopen_roulette_button")
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Casino,
                                    contentDescription = "Крутить рулетку",
                                    tint = Color.White,
                                    modifier = Modifier.size(30.dp)
                                )
                            }
                        }

                        // Test Palette Toggle
                        IconButton(
                            onClick = { viewModel.toggleTestPalette() },
                            modifier = Modifier
                                .size(44.dp)
                                .background(
                                    if (showTestPalette) NeonCyan.copy(alpha = 0.25f) else CyberObsidian,
                                    CircleShape
                                )
                                .testTag("toggle_test_palette_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Palette,
                                contentDescription = "Тест-палитра",
                                tint = if (showTestPalette) NeonCyan else TextSecondary
                            )
                        }

                        // Quests / Achievements
                        IconButton(
                            onClick = { viewModel.openDialog(ActiveDialog.QUESTS) },
                            modifier = Modifier
                                .size(44.dp)
                                .background(CyberObsidian, CircleShape)
                                .testTag("open_quests_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.EmojiEvents,
                                contentDescription = "Квесты",
                                tint = NeonAmber
                            )
                        }
                    }
                }
            }

            // Celebration Confetti
            ConfettiCanvas(
                isActive = gamePhase == GamePhase.CELEBRATING,
                onFinished = { /* handled by delay */ }
            )
        }

        // Top Navigation Overlays (Common across screens)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // App Branding Chip
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = CyberSurface.copy(alpha = 0.88f),
                border = androidx.compose.foundation.BorderStroke(1.dp, CyberBorder)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Palette,
                        contentDescription = null,
                        tint = NeonCyan,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "CHROMA HUNT",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp,
                        color = TextPrimary
                    )
                }
            }

            // Action Buttons: Collection, Quests, Settings
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Color Dex button with badge
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = CyberSurface.copy(alpha = 0.88f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CyberBorder),
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { viewModel.openDialog(ActiveDialog.COLOR_DEX) }
                        .testTag("open_color_dex_button")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Collections,
                            contentDescription = "Коллекция",
                            tint = NeonPink,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "$distinctFoundCount / 20",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeonPink
                        )
                    }
                }

                // Settings button
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = CyberSurface.copy(alpha = 0.88f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CyberBorder),
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { viewModel.openDialog(ActiveDialog.SETTINGS) }
                        .testTag("open_settings_button")
                ) {
                    Box(modifier = Modifier.padding(8.dp)) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Настройки",
                            tint = TextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        // Active Dialogs
        when (activeDialog) {
            ActiveDialog.COLOR_DEX -> {
                ColorDexDialog(
                    foundHistory = foundHistory,
                    onDismiss = { viewModel.closeDialog() }
                )
            }
            ActiveDialog.QUESTS -> {
                QuestsDialog(
                    history = foundHistory,
                    sessionStreak = sessionStreak,
                    onDismiss = { viewModel.closeDialog() }
                )
            }
            ActiveDialog.SETTINGS -> {
                SettingsDialog(
                    toleranceThreshold = toleranceThreshold,
                    onToleranceChange = { viewModel.setTolerance(it) },
                    isSoundEnabled = viewModel.soundManager.isSoundEnabled,
                    onSoundToggle = { viewModel.soundManager.isSoundEnabled = it },
                    isHapticEnabled = viewModel.soundManager.isHapticEnabled,
                    onHapticToggle = { viewModel.soundManager.isHapticEnabled = it },
                    onResetData = { viewModel.clearHistory() },
                    onDismiss = { viewModel.closeDialog() }
                )
            }
            ActiveDialog.NONE -> {}
        }
    }
}
