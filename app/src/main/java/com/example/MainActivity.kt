package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.ui.hud.MainGameScreen
import com.example.ui.theme.ChromaHuntTheme
import com.example.ui.theme.CyberObsidian
import com.example.viewmodel.GameViewModel

class MainActivity : ComponentActivity() {

    private val gameViewModel: GameViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ChromaHuntTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = CyberObsidian
                ) {
                    MainGameScreen(viewModel = gameViewModel)
                }
            }
        }
    }
}
