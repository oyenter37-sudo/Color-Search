package com.example.viewmodel

import android.app.Application
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ColorRepository
import com.example.data.local.ChromaHuntDatabase
import com.example.data.local.FoundColorEntity
import com.example.model.ColorCatalog
import com.example.model.HuntColor
import com.example.util.ColorUtils
import com.example.util.SoundManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.random.Random

enum class GamePhase {
    ROULETTE,
    HUNTING,
    CELEBRATING
}

enum class ActiveDialog {
    NONE,
    COLOR_DEX,
    QUESTS,
    SETTINGS
}

class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val database = ChromaHuntDatabase.getDatabase(application)
    private val repository = ColorRepository(database.foundColorDao())
    val soundManager = SoundManager(application)

    val foundHistory: StateFlow<List<FoundColorEntity>> = repository.allFound
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val distinctFoundCount: StateFlow<Int> = repository.distinctCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    private val _gamePhase = MutableStateFlow(GamePhase.ROULETTE)
    val gamePhase: StateFlow<GamePhase> = _gamePhase.asStateFlow()

    private val _targetColor = MutableStateFlow(ColorCatalog.getRandom())
    val targetColor: StateFlow<HuntColor> = _targetColor.asStateFlow()

    private val _sampledR = MutableStateFlow(128)
    val sampledR: StateFlow<Int> = _sampledR.asStateFlow()

    private val _sampledG = MutableStateFlow(128)
    val sampledG: StateFlow<Int> = _sampledG.asStateFlow()

    private val _sampledB = MutableStateFlow(128)
    val sampledB: StateFlow<Int> = _sampledB.asStateFlow()

    private val _liveAccuracy = MutableStateFlow(0f)
    val liveAccuracy: StateFlow<Float> = _liveAccuracy.asStateFlow()

    private val _lockOnProgress = MutableStateFlow(0f)
    val lockOnProgress: StateFlow<Float> = _lockOnProgress.asStateFlow()

    private val _toleranceThreshold = MutableStateFlow(50f)
    val toleranceThreshold: StateFlow<Float> = _toleranceThreshold.asStateFlow()

    private val _isTorchOn = MutableStateFlow(false)
    val isTorchOn: StateFlow<Boolean> = _isTorchOn.asStateFlow()

    private val _useFrontCamera = MutableStateFlow(false)
    val useFrontCamera: StateFlow<Boolean> = _useFrontCamera.asStateFlow()

    private val _showTestPalette = MutableStateFlow(false)
    val showTestPalette: StateFlow<Boolean> = _showTestPalette.asStateFlow()

    private val _sessionStreak = MutableStateFlow(0)
    val sessionStreak: StateFlow<Int> = _sessionStreak.asStateFlow()

    private val _activeDialog = MutableStateFlow(ActiveDialog.NONE)
    val activeDialog: StateFlow<ActiveDialog> = _activeDialog.asStateFlow()

    // Screen Shake Animatable offsets
    val shakeOffsetX = Animatable(0f)
    val shakeOffsetY = Animatable(0f)

    private var lockOnJob: Job? = null
    private var shakeJob: Job? = null
    private var celebrationJob: Job? = null
    private var lastSonarSoundTime = 0L
    private var isProcessingSuccess = false

    fun onRouletteColorChosen(color: HuntColor) {
        lockOnJob?.cancel()
        shakeJob?.cancel()
        celebrationJob?.cancel()
        isProcessingSuccess = false
        _targetColor.value = color
        _lockOnProgress.value = 0f
        _gamePhase.value = GamePhase.HUNTING
    }

    fun openRoulette() {
        lockOnJob?.cancel()
        shakeJob?.cancel()
        celebrationJob?.cancel()
        isProcessingSuccess = false
        _lockOnProgress.value = 0f
        _gamePhase.value = GamePhase.ROULETTE
    }

    fun onColorSampled(r: Int, g: Int, b: Int) {
        if (_gamePhase.value != GamePhase.HUNTING || isProcessingSuccess) return

        _sampledR.value = r
        _sampledG.value = g
        _sampledB.value = b

        val target = _targetColor.value
        val accuracy = ColorUtils.calculateAccuracy(
            sampledR = r,
            sampledG = g,
            sampledB = b,
            targetR = target.redInt,
            targetG = target.greenInt,
            targetB = target.blueInt
        )
        _liveAccuracy.value = accuracy

        val threshold = _toleranceThreshold.value
        if (accuracy >= threshold) {
            // Closeness radar sound (rate limited to avoid audio spam)
            val now = System.currentTimeMillis()
            if (now - lastSonarSoundTime > 380) {
                lastSonarSoundTime = now
                soundManager.playSonarPing(accuracy)
            }

            // Advance lock on progress (~5-6 ticks to lock on)
            advanceLockOn()
        } else {
            // Gentle decay lock-on progress
            if (_lockOnProgress.value > 0f) {
                _lockOnProgress.value = (_lockOnProgress.value - 0.12f).coerceAtLeast(0f)
            }
        }
    }

    private fun advanceLockOn() {
        if (isProcessingSuccess || _gamePhase.value != GamePhase.HUNTING) return
        val current = _lockOnProgress.value
        val next = current + 0.18f
        _lockOnProgress.value = next.coerceAtMost(1f)

        if (next >= 1.0f) {
            isProcessingSuccess = true
            triggerSuccess()
        }
    }

    private fun triggerSuccess() {
        _gamePhase.value = GamePhase.CELEBRATING
        _sessionStreak.value += 1

        val target = _targetColor.value
        val accuracy = _liveAccuracy.value
        val r = _sampledR.value
        val g = _sampledG.value
        val b = _sampledB.value

        // Record in Database
        viewModelScope.launch {
            try {
                repository.recordCapture(target, r, g, b, accuracy)
            } catch (_: Throwable) {
            }
        }

        // Play sounds & haptics safely
        soundManager.playVictoryChord()
        soundManager.vibrateVictory()

        // Screen shake animation
        shakeJob?.cancel()
        shakeJob = viewModelScope.launch {
            try {
                val shakeStrength = 18f
                for (i in 0 until 6) {
                    val dx = (Random.nextFloat() * 2f - 1f) * shakeStrength * (1f - i / 6f)
                    val dy = (Random.nextFloat() * 2f - 1f) * shakeStrength * (1f - i / 6f)
                    shakeOffsetX.animateTo(dx, tween(30))
                    shakeOffsetY.animateTo(dy, tween(30))
                }
                shakeOffsetX.animateTo(0f, tween(40))
                shakeOffsetY.animateTo(0f, tween(40))
            } catch (_: Throwable) {
                try {
                    shakeOffsetX.snapTo(0f)
                    shakeOffsetY.snapTo(0f)
                } catch (_: Throwable) {}
            }
        }

        // Celebrate for 2.2 seconds, then return to roulette wheel to spin again!
        celebrationJob?.cancel()
        celebrationJob = viewModelScope.launch {
            delay(2200)
            isProcessingSuccess = false
            _lockOnProgress.value = 0f
            _gamePhase.value = GamePhase.ROULETTE
        }
    }

    fun toggleTorch() {
        _isTorchOn.value = !_isTorchOn.value
    }

    fun toggleFrontCamera() {
        _useFrontCamera.value = !_useFrontCamera.value
    }

    fun toggleTestPalette() {
        _showTestPalette.value = !_showTestPalette.value
    }

    fun setTolerance(tolerance: Float) {
        _toleranceThreshold.value = tolerance
    }

    fun openDialog(dialog: ActiveDialog) {
        _activeDialog.value = dialog
    }

    fun closeDialog() {
        _activeDialog.value = ActiveDialog.NONE
    }

    fun clearHistory() {
        viewModelScope.launch {
            repository.clearHistory()
            _sessionStreak.value = 0
        }
    }
}
