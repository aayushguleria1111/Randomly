package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.ui.components.StartupAnimationOverlay
import com.example.ui.navigation.RandomlyNavGraph
import com.example.ui.theme.RandomlyTheme
import com.example.ui.viewmodel.RandomlyViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: RandomlyViewModel by viewModels {
        RandomlyViewModel.provideFactory(application)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val settings by viewModel.settings.collectAsState()
            var showSplash by remember { mutableStateOf(!viewModel.hasShownSplash) }

            RandomlyTheme(
                themeMode = settings.themeMode,
                colorTheme = settings.colorTheme
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    RandomlyNavGraph(viewModel = viewModel)

                    if (showSplash && !viewModel.hasShownSplash) {
                        StartupAnimationOverlay(
                            onSplashFinished = {
                                viewModel.hasShownSplash = true
                                showSplash = false
                            }
                        )
                    }
                }
            }
        }
    }
}
