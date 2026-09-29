package com.example.ui.screens.tools

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.model.ToolType
import com.example.ui.components.ResultDisplayCard
import com.example.ui.components.SectionHeader
import com.example.ui.theme.AmberAccent
import java.security.SecureRandom
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.sqrt

import com.example.ui.viewmodel.RandomlyViewModel
import com.example.util.HapticFeedbackUtil
import com.example.util.RandomGenerators
import com.example.util.ShareUtil
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class NumberWeightMode(val label: String, val shortDesc: String) {
    UNIFORM("Equal (1×)", "Equal probability for every number"),
    FAVOR_LOW("Favor Lower", "Higher probability for numbers near min"),
    FAVOR_HIGH("Favor Higher", "Higher probability for numbers near max"),
    BELL_CURVE("Center / Mid", "Higher probability for numbers near center"),
    FAVOR_ODD("Favor Odd (3×)", "3× higher probability for odd numbers"),
    FAVOR_EVEN("Favor Even (3×)", "3× higher probability for even numbers"),
    LUCKY_BOOST("Lucky Boost (5×)", "Selected lucky number gets 5× boost")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NumberScreen(
    viewModel: RandomlyViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val settings by viewModel.settings.collectAsState()
    val isFavorite = viewModel.isToolFavorite(ToolType.NUMBER.id)

    var minInput by remember { mutableStateOf(settings.defaultMinNumber.toString()) }
    var maxInput by remember { mutableStateOf(settings.defaultMaxNumber.toString()) }
    var countInput by remember { mutableStateOf("1") }
    var allowDuplicates by remember { mutableStateOf(false) }

    var selectedWeightMode by remember { mutableStateOf(NumberWeightMode.UNIFORM) }
    var luckyNumberInput by remember { mutableStateOf("") }

    var results by remember { mutableStateOf<List<Int>>(emptyList()) }
    var displayResults by remember { mutableStateOf<List<Int>>(emptyList()) }
    var isRolling by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    val rollScale = remember { androidx.compose.animation.core.Animatable(1f) }
    val shakeX = remember { androidx.compose.animation.core.Animatable(0f) }

    val allHistory by viewModel.history.collectAsState()
    val recentHistory = remember(allHistory) {
        allHistory.filter { it.toolType == ToolType.NUMBER.id }.take(5)
    }

    fun sampleWeightedNumbers(
        min: Int,
        max: Int,
        count: Int,
        allowDuplicates: Boolean,
        weightMode: NumberWeightMode,
        luckyNumber: Int?,
        random: SecureRandom
    ): List<Int> {
        if (min > max || count <= 0) return emptyList()
        val rangeSize = (max.toLong() - min.toLong() + 1).coerceAtMost(100_000L).toInt()

        if (weightMode == NumberWeightMode.UNIFORM) {
            val gen = RandomGenerators.generateNumbers(min, max, count, allowDuplicates)
            return gen.getOrNull() ?: emptyList()
        }

        if (rangeSize <= 5000) {
            val pool = (min..max).map { n ->
                val w = when (weightMode) {
                    NumberWeightMode.UNIFORM -> 1
                    NumberWeightMode.FAVOR_LOW -> (max - n + 1).coerceIn(1, 1000)
                    NumberWeightMode.FAVOR_HIGH -> (n - min + 1).coerceIn(1, 1000)
                    NumberWeightMode.BELL_CURVE -> {
                        val mid = (min + max) / 2.0
                        val dist = abs(n - mid)
                        val maxDist = max(1.0, (max - min) / 2.0)
                        ((1.0 - (dist / maxDist)) * 9.0 + 1.0).toInt().coerceAtLeast(1)
                    }
                    NumberWeightMode.FAVOR_ODD -> if (n % 2 != 0) 3 else 1
                    NumberWeightMode.FAVOR_EVEN -> if (n % 2 == 0) 3 else 1
                    NumberWeightMode.LUCKY_BOOST -> if (luckyNumber != null && n == luckyNumber) 5 else 1
                }
                Pair(n, w)
            }.toMutableList()

            val chosen = mutableListOf<Int>()
            for (step in 0 until count) {
                if (pool.isEmpty()) break
                val totalW = pool.sumOf { it.second.toLong() }
                if (totalW <= 0) break
                var target = (random.nextDouble() * totalW).toLong()
                var selectedIdx = 0
                for (i in pool.indices) {
                    target -= pool[i].second
                    if (target <= 0) {
                        selectedIdx = i
                        break
                    }
                }
                val pick = pool[selectedIdx].first
                chosen.add(pick)
                if (!allowDuplicates) {
                    pool.removeAt(selectedIdx)
                }
            }
            return chosen
        } else {
            val chosen = mutableListOf<Int>()
            var attempts = 0
            while (chosen.size < count && attempts < count * 20) {
                attempts++
                val u1 = random.nextDouble()
                val pick = when (weightMode) {
                    NumberWeightMode.FAVOR_LOW -> {
                        val skewed = 1.0 - sqrt(1.0 - u1)
                        min + (skewed * (max - min)).toInt()
                    }
                    NumberWeightMode.FAVOR_HIGH -> {
                        val skewed = sqrt(u1)
                        min + (skewed * (max - min)).toInt()
                    }
                    NumberWeightMode.BELL_CURVE -> {
                        val u2 = random.nextDouble()
                        val mean = (u1 + u2) / 2.0
                        min + (mean * (max - min)).toInt()
                    }
                    NumberWeightMode.FAVOR_ODD -> {
                        var n = min + random.nextInt(rangeSize)
                        if (n % 2 == 0 && random.nextInt(3) != 0) {
                            n = if (n < max) n + 1 else if (n > min) n - 1 else n
                        }
                        n
                    }
                    NumberWeightMode.FAVOR_EVEN -> {
                        var n = min + random.nextInt(rangeSize)
                        if (n % 2 != 0 && random.nextInt(3) != 0) {
                            n = if (n < max) n + 1 else if (n > min) n - 1 else n
                        }
                        n
                    }
                    NumberWeightMode.LUCKY_BOOST -> {
                        if (luckyNumber != null && luckyNumber in min..max && random.nextInt(5) == 0) {
                            luckyNumber
                        } else {
                            min + random.nextInt(rangeSize)
                        }
                    }
                    else -> min + random.nextInt(rangeSize)
                }.coerceIn(min, max)

                if (allowDuplicates || !chosen.contains(pick)) {
                    chosen.add(pick)
                }
            }
            return chosen
        }
    }

    fun generate() {
        if (isRolling) return
        errorMessage = null
        val min = minInput.toIntOrNull()
        val max = maxInput.toIntOrNull()
        val count = countInput.toIntOrNull() ?: 1

        if (min == null || max == null) {
            errorMessage = "Please enter valid integers for min and max"
            return
        }
        if (min > max) {
            errorMessage = "Minimum ($min) must be less than or equal to maximum ($max)"
            return
        }
        if (count < 1 || count > 100) {
            errorMessage = "Count must be between 1 and 100"
            return
        }
        if (!allowDuplicates && count > (max - min + 1)) {
            errorMessage = "Cannot generate $count unique numbers in range of size ${max - min + 1}"
            return
        }

        val luckyNum = luckyNumberInput.toIntOrNull()
        if (selectedWeightMode == NumberWeightMode.LUCKY_BOOST) {
            if (luckyNum == null || luckyNum !in min..max) {
                errorMessage = "Please enter a lucky number within range ($min to $max)"
                return
            }
        }

        scope.launch {
            val random = SecureRandom()
            val generated = sampleWeightedNumbers(
                min = min,
                max = max,
                count = count,
                allowDuplicates = allowDuplicates,
                weightMode = selectedWeightMode,
                luckyNumber = luckyNum,
                random = random
            )

            if (generated.isEmpty()) {
                errorMessage = "Could not generate numbers with current settings"
                return@launch
            }

            isRolling = true
            if (settings.animationsEnabled) {
                val iterations = 8
                for (i in 0 until iterations) {
                    displayResults = List(count) { min + random.nextInt(max - min + 1) }
                    HapticFeedbackUtil.performImpact(context, settings.hapticsEnabled)
                    shakeX.animateTo(if (i % 2 == 0) -4f else 4f, androidx.compose.animation.core.tween(50))
                    kotlinx.coroutines.delay(50)
                }
                shakeX.snapTo(0f)
            }

            results = generated
            displayResults = generated
            isRolling = false
            HapticFeedbackUtil.performSuccess(context, settings.hapticsEnabled)

            if (settings.animationsEnabled) {
                rollScale.snapTo(1.15f)
                rollScale.animateTo(
                    targetValue = 1f,
                    animationSpec = androidx.compose.animation.core.spring(
                        dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy,
                        stiffness = androidx.compose.animation.core.Spring.StiffnessLow
                    )
                )
            }

            val resultStr = if (generated.size == 1) "${generated.first()}" else generated.joinToString(", ")
            val weightSuffix = when (selectedWeightMode) {
                NumberWeightMode.UNIFORM -> ""
                NumberWeightMode.LUCKY_BOOST -> " • Weight: Lucky #$luckyNum (5×)"
                else -> " • Weight: ${selectedWeightMode.label}"
            }
            val detailsStr = "Range: $min to $max | Count: $count$weightSuffix"
            viewModel.recordResult(
                toolType = ToolType.NUMBER,
                title = "Random Number",
                result = resultStr,
                details = detailsStr
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Random Number", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.toggleFavorite(ToolType.NUMBER) }) {
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
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                val hasResults = displayResults.isNotEmpty() || results.isNotEmpty()
                val activeList = if (displayResults.isNotEmpty()) displayResults else results
                val resultDisplay = if (hasResults) {
                    if (activeList.size == 1) "${activeList.first()}" else activeList.joinToString(", ")
                } else {
                    "?"
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 520.dp)
                        .graphicsLayer {
                            scaleX = rollScale.value
                            scaleY = rollScale.value
                            translationX = shakeX.value
                        }
                ) {
                    ResultDisplayCard(
                        resultText = resultDisplay,
                        detailsText = if (isRolling) "Rolling..." else if (hasResults) {
                            "Range: $minInput → $maxInput (${activeList.size} number${if (activeList.size > 1) "s" else ""})"
                        } else {
                            "Range: $minInput to $maxInput • Tap Generate to start"
                        },
                        accentColor = ToolType.NUMBER.accentColor,
                        onCopy = {
                            if (hasResults) {
                                ShareUtil.copyToClipboard(context, resultDisplay)
                                viewModel.showMessage("Copied $resultDisplay to clipboard")
                            }
                        },
                        onShare = {
                            if (hasResults) {
                                ShareUtil.shareText(context, "Random Number Result", resultDisplay)
                            }
                        },
                        onRegenerate = { generate() }
                    )
                }
            }

            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 520.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Range Settings",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedTextField(
                                value = minInput,
                                onValueChange = { minInput = it },
                                label = { Text("Min") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f).testTag("input_min_number"),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = maxInput,
                                onValueChange = { maxInput = it },
                                label = { Text("Max") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f).testTag("input_max_number"),
                                singleLine = true
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Quick Presets
                        Text("Presets:", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(4.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            val presets = listOf(1 to 6, 1 to 10, 1 to 100, 1 to 1000, 0 to 1, -100 to 100)
                            items(presets) { (pMin, pMax) ->
                                FilterChip(
                                    selected = minInput == pMin.toString() && maxInput == pMax.toString(),
                                    onClick = {
                                        minInput = pMin.toString()
                                        maxInput = pMax.toString()
                                    },
                                    label = { Text("$pMin – $pMax") }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedTextField(
                                value = countInput,
                                onValueChange = { countInput = it },
                                label = { Text("Count") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f).testTag("input_count_number"),
                                singleLine = true
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Allow Duplicates", style = MaterialTheme.typography.bodyMedium)
                                Text("Can generate identical numbers", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Switch(
                                checked = allowDuplicates,
                                onCheckedChange = { allowDuplicates = it }
                            )
                        }

                        // Probability Weighting Options
                        Spacer(modifier = Modifier.height(14.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Probability Weighting",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            if (selectedWeightMode != NumberWeightMode.UNIFORM) {
                                Text(
                                    text = "Active Bias",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = ToolType.NUMBER.accentColor,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Text(
                            text = selectedWeightMode.shortDesc,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(NumberWeightMode.entries.toTypedArray()) { mode ->
                                FilterChip(
                                    selected = selectedWeightMode == mode,
                                    onClick = { selectedWeightMode = mode },
                                    label = { Text(mode.label) }
                                )
                            }
                        }

                        if (selectedWeightMode == NumberWeightMode.LUCKY_BOOST) {
                            Spacer(modifier = Modifier.height(10.dp))
                            OutlinedTextField(
                                value = luckyNumberInput,
                                onValueChange = { luckyNumberInput = it },
                                label = { Text("Enter Lucky Number (e.g. 7)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        if (errorMessage != null) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "❌ $errorMessage",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = { generate() },
                            enabled = !isRolling,
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 48.dp, max = 52.dp)
                                .testTag("generate_number_button"),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(Icons.Default.Casino, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                if (isRolling) "Rolling Numbers..." else "Generate Numbers",
                                style = MaterialTheme.typography.titleMedium,
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            if (recentHistory.isNotEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().widthIn(max = 520.dp)) {
                        SectionHeader(title = "Recent Numbers")
                    }
                }
                items(recentHistory) { item ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .widthIn(max = 520.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(item.result, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Text(item.details, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            IconButton(onClick = {
                                ShareUtil.copyToClipboard(context, item.result)
                                viewModel.showMessage("Copied ${item.result}")
                            }) {
                                Icon(Icons.Default.Casino, contentDescription = "Use", tint = ToolType.NUMBER.accentColor)
                            }
                        }
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}
