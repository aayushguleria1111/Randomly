package com.example.ui.screens.tools

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.ToolType
import com.example.ui.components.ResultDisplayCard
import com.example.ui.theme.AmberAccent
import com.example.ui.viewmodel.RandomlyViewModel
import com.example.util.HapticFeedbackUtil
import com.example.util.ShareUtil
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.security.SecureRandom

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LetterScreen(
    viewModel: RandomlyViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val settings by viewModel.settings.collectAsState()
    val isFavorite = viewModel.isToolFavorite(ToolType.LETTER.id)

    var uppercase by remember { mutableStateOf(true) }
    var letterCount by remember { mutableStateOf(1) }
    var currentResult by remember { mutableStateOf("A") }
    var isGenerating by remember { mutableStateOf(false) }
    val letterScale = remember { Animatable(1f) }

    fun generateLetter() {
        if (isGenerating) return
        isGenerating = true
        HapticFeedbackUtil.performImpact(context, settings.hapticsEnabled)

        scope.launch {
            val alphabet = if (uppercase) "ABCDEFGHIJKLMNOPQRSTUVWXYZ" else "abcdefghijklmnopqrstuvwxyz"
            val random = SecureRandom()

            if (settings.animationsEnabled) {
                val cycleCount = 10
                for (i in 0 until cycleCount) {
                    currentResult = (1..letterCount).map { alphabet[random.nextInt(alphabet.length)] }.joinToString(" ")
                    HapticFeedbackUtil.performImpact(context, settings.hapticsEnabled)
                    val delayMs = 35L + (i * 15L)
                    delay(delayMs)
                }
            }

            val result = (1..letterCount).map { alphabet[random.nextInt(alphabet.length)] }.joinToString(" ")
            currentResult = result

            if (settings.animationsEnabled) {
                letterScale.snapTo(0.65f)
                letterScale.animateTo(
                    1f,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium)
                )
            }

            HapticFeedbackUtil.performSuccess(context, settings.hapticsEnabled)
            isGenerating = false

            viewModel.recordResult(
                toolType = ToolType.LETTER,
                title = "Random Letter",
                result = result,
                details = if (uppercase) "Uppercase (A-Z)" else "Lowercase (a-z)"
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Random Letter", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.toggleFavorite(ToolType.LETTER) }) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Filled.Star else Icons.Outlined.StarBorder,
                            contentDescription = if (isFavorite) "Unfavorite" else "Favorite",
                            tint = if (isFavorite) AmberAccent else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Box(
                    modifier = Modifier.graphicsLayer {
                        scaleX = letterScale.value
                        scaleY = letterScale.value
                    }
                ) {
                    ResultDisplayCard(
                        resultText = currentResult,
                        detailsText = "$letterCount letter${if (letterCount > 1) "s" else ""} • ${if (uppercase) "Uppercase" else "Lowercase"}",
                        accentColor = ToolType.LETTER.accentColor,
                        onCopy = {
                            ShareUtil.copyToClipboard(context, currentResult)
                            viewModel.showMessage("Copied '$currentResult' to clipboard")
                        },
                        onShare = {
                            ShareUtil.shareText(context, "Random Letter Result", currentResult)
                        },
                        onRegenerate = { generateLetter() }
                    )
                }
            }

            item {
                Button(
                    onClick = { generateLetter() },
                    enabled = !isGenerating,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("generate_letter_button"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ToolType.LETTER.accentColor)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null)
                    Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                    Text(
                        if (isGenerating) "Rolling Letters..." else "Generate Random Letter",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Options", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Uppercase (A-Z)", style = MaterialTheme.typography.bodyMedium)
                            Switch(checked = uppercase, onCheckedChange = { uppercase = it })
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text("Count: $letterCount", style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(1, 2, 3, 5).forEach { count ->
                                FilterChip(
                                    selected = letterCount == count,
                                    onClick = { letterCount = count },
                                    label = { Text("$count") }
                                )
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}
