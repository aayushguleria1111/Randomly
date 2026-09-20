package com.example.ui.screens.tools

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import com.example.ui.viewmodel.RandomlyViewModel
import com.example.util.HapticFeedbackUtil
import com.example.util.RandomGenerators
import com.example.util.ShareUtil

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

    var results by remember { mutableStateOf<List<Int>>(emptyList()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val allHistory by viewModel.history.collectAsState()
    val recentHistory = remember(allHistory) {
        allHistory.filter { it.toolType == ToolType.NUMBER.id }.take(5)
    }

    fun generate() {
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

        val genResult = RandomGenerators.generateNumbers(min, max, count, allowDuplicates)
        genResult.fold(
            onSuccess = { generated ->
                results = generated
                HapticFeedbackUtil.performSuccess(context, settings.hapticsEnabled)
                val resultStr = if (generated.size == 1) "${generated.first()}" else generated.joinToString(", ")
                val detailsStr = "Range: $min to $max | Count: $count"
                viewModel.recordResult(
                    toolType = ToolType.NUMBER,
                    title = "Random Number",
                    result = resultStr,
                    details = detailsStr
                )
            },
            onFailure = { ex ->
                errorMessage = ex.message ?: "Invalid range configuration"
            }
        )
    }

    LaunchedEffect(Unit) {
        if (results.isEmpty()) {
            generate()
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
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                if (results.isNotEmpty()) {
                    val resultDisplay = if (results.size == 1) "${results.first()}" else results.joinToString(", ")
                    Box(modifier = Modifier.fillMaxWidth().widthIn(max = 520.dp)) {
                        ResultDisplayCard(
                            resultText = resultDisplay,
                            detailsText = "Range: $minInput → $maxInput (${results.size} number${if (results.size > 1) "s" else ""})",
                            accentColor = ToolType.NUMBER.accentColor,
                            onCopy = {
                                ShareUtil.copyToClipboard(context, resultDisplay)
                                viewModel.showMessage("Copied $resultDisplay to clipboard")
                            },
                            onShare = {
                                ShareUtil.shareText(context, "Random Number Result", resultDisplay)
                            },
                            onRegenerate = { generate() }
                        )
                    }
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
                                        generate()
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
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 48.dp, max = 52.dp)
                                .testTag("generate_number_button"),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(Icons.Default.Casino, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Generate Numbers",
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
