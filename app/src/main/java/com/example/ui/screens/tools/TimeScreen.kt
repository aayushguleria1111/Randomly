package com.example.ui.screens.tools

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.example.util.RandomGenerators
import com.example.util.ShareUtil
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeScreen(
    viewModel: RandomlyViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val settings by viewModel.settings.collectAsState()
    val isFavorite = viewModel.isToolFavorite(ToolType.TIME.id)

    var includeSeconds by remember { mutableStateOf(false) }
    var use24HourFormat by remember { mutableStateOf(false) }

    var selectedTimeResult by remember { mutableStateOf<LocalTime?>(null) }
    var isGenerating by remember { mutableStateOf(false) }
    val timeScale = remember { Animatable(1f) }

    val timeFormatter = remember(includeSeconds, use24HourFormat) {
        val pattern = when {
            use24HourFormat && includeSeconds -> "HH:mm:ss"
            use24HourFormat -> "HH:mm"
            includeSeconds -> "hh:mm:ss a"
            else -> "hh:mm a"
        }
        DateTimeFormatter.ofPattern(pattern)
    }

    fun generate() {
        if (isGenerating) return
        isGenerating = true
        HapticFeedbackUtil.performImpact(context, settings.hapticsEnabled)

        scope.launch {
            if (settings.animationsEnabled) {
                val cycleSteps = 8
                for (step in 0 until cycleSteps) {
                    selectedTimeResult = RandomGenerators.generateTime(
                        includeSeconds = includeSeconds,
                        format24Hour = use24HourFormat
                    )
                    HapticFeedbackUtil.performImpact(context, settings.hapticsEnabled)
                    val delayMs = 30L + (step * 15L)
                    delay(delayMs)
                }
            }

            val time = RandomGenerators.generateTime(
                includeSeconds = includeSeconds,
                format24Hour = use24HourFormat
            )
            selectedTimeResult = time

            if (settings.animationsEnabled) {
                timeScale.snapTo(0.7f)
                timeScale.animateTo(
                    1f,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium)
                )
            }

            HapticFeedbackUtil.performSuccess(context, settings.hapticsEnabled)
            val formatted = time.format(timeFormatter)
            viewModel.recordResult(
                toolType = ToolType.TIME,
                title = "Random Time",
                result = formatted,
                details = if (use24HourFormat) "24-Hour Format" else "12-Hour Format (AM/PM)"
            )
            isGenerating = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Random Time", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.toggleFavorite(ToolType.TIME) }) {
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
                .padding(horizontal = 12.dp),
            contentPadding = PaddingValues(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                val hasTime = selectedTimeResult != null
                val formatted = selectedTimeResult?.format(timeFormatter) ?: "--:--"
                Box(
                    modifier = Modifier.graphicsLayer {
                        scaleX = timeScale.value
                        scaleY = timeScale.value
                    }
                ) {
                    ResultDisplayCard(
                        resultText = formatted,
                        detailsText = if (hasTime) {
                            if (use24HourFormat) "24-hour military format" else "Standard 12-hour format"
                        } else {
                            "Tap Generate Time to pick a random time"
                        },
                        accentColor = ToolType.TIME.accentColor,
                        onCopy = {
                            if (hasTime) {
                                ShareUtil.copyToClipboard(context, formatted)
                                viewModel.showMessage("Copied time to clipboard")
                            }
                        },
                        onShare = {
                            if (hasTime) {
                                ShareUtil.shareText(context, "Random Time", formatted)
                            }
                        },
                        onRegenerate = { generate() }
                    )
                }
            }

            // Options
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Time Format Settings", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("24-Hour Clock (e.g. 23:45)", style = MaterialTheme.typography.bodyMedium)
                            Switch(
                                checked = use24HourFormat,
                                onCheckedChange = { use24HourFormat = it }
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Include Seconds (e.g. :30)", style = MaterialTheme.typography.bodyMedium)
                            Switch(
                                checked = includeSeconds,
                                onCheckedChange = { includeSeconds = it }
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = { generate() },
                            enabled = !isGenerating,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("generate_time_button"),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(Icons.Default.AccessTime, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                if (isGenerating) "Spinning Clock..." else "Generate Random Time",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}
