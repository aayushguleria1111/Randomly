package com.example.ui.screens.tools

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.ToolPreset
import com.example.data.model.ToolType
import com.example.ui.components.ResultDisplayCard
import com.example.ui.components.SpinWheelCanvas
import com.example.ui.theme.AmberAccent
import com.example.ui.viewmodel.RandomlyViewModel
import com.example.util.HapticFeedbackUtil
import com.example.util.ShareUtil
import kotlinx.coroutines.launch
import java.security.SecureRandom

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WheelScreen(
    viewModel: RandomlyViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val settings by viewModel.settings.collectAsState()
    val isFavorite = viewModel.isToolFavorite(ToolType.SPIN_WHEEL.id)

    val dbPresets by viewModel.getPresetsForTool(ToolType.SPIN_WHEEL.id).collectAsState(initial = emptyList())

    val items = remember {
        mutableStateListOf("Pizza", "Burger", "Sushi", "Tacos", "Pasta", "Salad")
    }

    var newItemText by remember { mutableStateOf("") }
    var removeWinnerAfterSpin by remember { mutableStateOf(false) }
    var isSpinning by remember { mutableStateOf(false) }
    var winningItem by remember { mutableStateOf<String?>(null) }
    var showWinnerDialog by remember { mutableStateOf(false) }

    var showSavePresetDialog by remember { mutableStateOf(false) }
    var newPresetNameInput by remember { mutableStateOf("") }
    var presetToDelete by remember { mutableStateOf<ToolPreset?>(null) }

    val rotation = remember { Animatable(0f) }

    fun spinWheel() {
        if (isSpinning || items.isEmpty()) return
        isSpinning = true
        winningItem = null

        scope.launch {
            val random = SecureRandom()
            val selectedIndex = random.nextInt(items.size)
            val sweepAngle = 360f / items.size
            
            val sectorCenterOffset = selectedIndex * sweepAngle + sweepAngle / 2f
            val targetAngleModulo = (270f - sectorCenterOffset + 360f) % 360f

            val extraFullSpins = (5 + random.nextInt(4)) * 360f
            val currentNormalized = rotation.value % 360f
            val delta = ((targetAngleModulo - currentNormalized + 360f) % 360f)
            val targetRotation = rotation.value + extraFullSpins + delta

            HapticFeedbackUtil.performImpact(context, settings.hapticsEnabled)

            rotation.animateTo(
                targetValue = targetRotation,
                animationSpec = tween(
                    durationMillis = if (settings.animationsEnabled) 3500 else 400,
                    easing = CubicBezierEasing(0.15f, 0.85f, 0.35f, 1f)
                )
            )

            val winner = items[selectedIndex]
            winningItem = winner
            isSpinning = false
            showWinnerDialog = true

            HapticFeedbackUtil.performSuccess(context, settings.hapticsEnabled)

            viewModel.recordResult(
                toolType = ToolType.SPIN_WHEEL,
                title = "Spin Wheel",
                result = winner,
                details = "Out of ${items.size} options"
            )

            if (removeWinnerAfterSpin && items.size > 1) {
                items.removeAt(selectedIndex)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Spin Wheel", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.toggleFavorite(ToolType.SPIN_WHEEL) }) {
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
            // Wheel Canvas
            item {
                SpinWheelCanvas(
                    items = items,
                    currentRotation = rotation.value,
                    isSpinning = isSpinning,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Spin Button
            item {
                Button(
                    onClick = { spinWheel() },
                    enabled = !isSpinning && items.isNotEmpty(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("spin_wheel_button"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ToolType.SPIN_WHEEL.accentColor)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (isSpinning) "Spinning..." else "SPIN THE WHEEL", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
            }

            // Latest Winner Banner
            winningItem?.let { winner ->
                item {
                    ResultDisplayCard(
                        resultText = winner,
                        detailsText = "Winner selected by the wheel!",
                        accentColor = ToolType.SPIN_WHEEL.accentColor,
                        onCopy = {
                            ShareUtil.copyToClipboard(context, winner)
                            viewModel.showMessage("Copied '$winner' to clipboard")
                        },
                        onShare = {
                            ShareUtil.shareText(context, "Wheel Spin Winner", winner)
                        },
                        onRegenerate = { spinWheel() }
                    )
                }
            }

            // Presets Header & Saved Presets Row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Saved & Ready Presets", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    TextButton(onClick = { showSavePresetDialog = true }) {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Save Current as Preset")
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(dbPresets) { preset ->
                        FilterChip(
                            selected = false,
                            onClick = {
                                if (!isSpinning) {
                                    items.clear()
                                    items.addAll(preset.getItems())
                                    viewModel.showMessage("Loaded preset: ${preset.presetName}")
                                }
                            },
                            label = { Text(preset.presetName) },
                            trailingIcon = {
                                IconButton(
                                    onClick = { presetToDelete = preset },
                                    modifier = Modifier.size(18.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Delete preset",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        )
                    }
                }
            }

            // Wheel Settings & Entries Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Wheel Entries (${items.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Row {
                                IconButton(onClick = {
                                    if (!isSpinning && items.isNotEmpty()) {
                                        items.shuffle()
                                        viewModel.showMessage("Entries shuffled")
                                    }
                                }) {
                                    Icon(Icons.Default.Shuffle, contentDescription = "Shuffle", tint = MaterialTheme.colorScheme.primary)
                                }
                                IconButton(onClick = {
                                    if (!isSpinning) {
                                        items.clear()
                                    }
                                }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear All", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Add Entry Field
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(
                                value = newItemText,
                                onValueChange = { newItemText = it },
                                label = { Text("Add new entry") },
                                modifier = Modifier.weight(1f).testTag("input_wheel_entry"),
                                singleLine = true
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    if (newItemText.isNotBlank()) {
                                        items.add(newItemText.trim())
                                        newItemText = ""
                                    }
                                },
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Add")
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Remove Winner After Spin", style = MaterialTheme.typography.bodyMedium)
                                Text("Automatically removes chosen entry", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Switch(
                                checked = removeWinnerAfterSpin,
                                onCheckedChange = { removeWinnerAfterSpin = it }
                            )
                        }
                    }
                }
            }

            // Entries List
            itemsIndexed(items) { index, item ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${index + 1}. $item",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { if (!isSpinning) items.removeAt(index) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f))
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }

    // Winner Dialog
    if (showWinnerDialog && winningItem != null) {
        AlertDialog(
            onDismissRequest = { showWinnerDialog = false },
            title = {
                Text("🎉 Winner Chosen!", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = winningItem ?: "",
                        style = MaterialTheme.typography.headlineMedium,
                        color = ToolType.SPIN_WHEEL.accentColor,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            },
            confirmButton = {
                Button(onClick = { showWinnerDialog = false }) {
                    Text("Awesome!")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = {
                    ShareUtil.shareText(context, "Wheel Decision", winningItem ?: "")
                    showWinnerDialog = false
                }) {
                    Text("Share")
                }
            }
        )
    }

    // Save Preset Dialog
    if (showSavePresetDialog) {
        AlertDialog(
            onDismissRequest = { showSavePresetDialog = false },
            title = { Text("Save Custom Preset") },
            text = {
                Column {
                    Text("Enter a name for this custom wheel preset:")
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = newPresetNameInput,
                        onValueChange = { newPresetNameInput = it },
                        label = { Text("Preset Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newPresetNameInput.isNotBlank() && items.isNotEmpty()) {
                            viewModel.savePreset(
                                toolId = ToolType.SPIN_WHEEL.id,
                                name = newPresetNameInput.trim(),
                                items = items.toList()
                            )
                            newPresetNameInput = ""
                            showSavePresetDialog = false
                        }
                    }
                ) {
                    Text("Save Preset")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSavePresetDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Delete Preset Dialog
    presetToDelete?.let { preset ->
        AlertDialog(
            onDismissRequest = { presetToDelete = null },
            title = { Text("Delete Preset?") },
            text = { Text("Are you sure you want to delete the preset '${preset.presetName}'?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deletePreset(preset.id)
                        presetToDelete = null
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { presetToDelete = null }) { Text("Cancel") }
            }
        )
    }
}
