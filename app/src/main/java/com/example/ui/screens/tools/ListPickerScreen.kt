package com.example.ui.screens.tools

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.ToolPreset
import com.example.data.model.ToolType
import com.example.ui.components.ResultDisplayCard
import com.example.ui.theme.AmberAccent
import com.example.ui.viewmodel.RandomlyViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.example.util.HapticFeedbackUtil
import com.example.util.ShareUtil
import java.security.SecureRandom

private const val MAX_ITEM_LENGTH = 25

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListPickerScreen(
    viewModel: RandomlyViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val settings by viewModel.settings.collectAsState()
    val isFavorite = viewModel.isToolFavorite(ToolType.LIST_PICKER.id)

    val dbPresets by viewModel.getPresetsForTool(ToolType.LIST_PICKER.id).collectAsState(initial = emptyList())

    val defaultItems = remember { listOf("Action", "Comedy", "Sci-Fi", "Horror", "Drama", "Animation") }
    val items = remember { mutableStateListOf<String>().apply { addAll(defaultItems) } }
    var hasLoadedLastUsed by remember { mutableStateOf(false) }

    // Load persisted items
    LaunchedEffect(Unit) {
        viewModel.getLastUsedItems(ToolType.LIST_PICKER.id, defaultItems).collect { saved ->
            if (!hasLoadedLastUsed && saved.isNotEmpty()) {
                items.clear()
                items.addAll(saved)
                hasLoadedLastUsed = true
            }
        }
    }

    fun persistCurrentItems() {
        viewModel.saveLastUsedItems(ToolType.LIST_PICKER.id, items.toList())
    }

    val scope = rememberCoroutineScope()
    var isPicking by remember { mutableStateOf(false) }
    var activeTickerIndex by remember { mutableIntStateOf(-1) }
    val resultScale = remember { Animatable(1f) }

    var newItemInput by remember { mutableStateOf("") }
    var pickCountInput by remember { mutableStateOf("1") }
    var allowDuplicates by remember { mutableStateOf(false) }
    var pickedResults by remember { mutableStateOf<List<String>>(emptyList()) }

    var showSavePresetDialog by remember { mutableStateOf(false) }
    var newPresetNameInput by remember { mutableStateOf("") }
    var presetToDelete by remember { mutableStateOf<ToolPreset?>(null) }

    fun pickItems() {
        if (items.isEmpty() || isPicking) return
        isPicking = true
        HapticFeedbackUtil.performImpact(context, settings.hapticsEnabled)

        scope.launch {
            val count = pickCountInput.toIntOrNull()?.coerceAtLeast(1) ?: 1
            val random = SecureRandom()

            if (settings.animationsEnabled && items.size > 1) {
                val totalTicks = 12
                for (i in 0 until totalTicks) {
                    activeTickerIndex = random.nextInt(items.size)
                    HapticFeedbackUtil.performImpact(context, settings.hapticsEnabled)
                    val delayMs = 35L + (i * 18L)
                    delay(delayMs)
                }
            }
            activeTickerIndex = -1

            val results = if (allowDuplicates) {
                List(count) { items[random.nextInt(items.size)] }
            } else {
                val shuffled = items.shuffled(random)
                shuffled.take(count.coerceAtMost(items.size))
            }

            pickedResults = results
            val resultStr = results.joinToString(", ")

            if (settings.animationsEnabled) {
                resultScale.snapTo(0.7f)
                resultScale.animateTo(
                    1f,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium)
                )
            }

            HapticFeedbackUtil.performSuccess(context, settings.hapticsEnabled)
            isPicking = false

            viewModel.recordResult(
                toolType = ToolType.LIST_PICKER,
                title = "List Picker",
                result = resultStr,
                details = "Picked $count out of ${items.size} items"
            )
        }
    }

    fun shuffleList() {
        if (items.isEmpty() || isPicking) return
        isPicking = true
        HapticFeedbackUtil.performImpact(context, settings.hapticsEnabled)
        scope.launch {
            if (settings.animationsEnabled) {
                repeat(4) {
                    val r = SecureRandom()
                    items.shuffle(r)
                    HapticFeedbackUtil.performImpact(context, settings.hapticsEnabled)
                    delay(70)
                }
            }
            val random = SecureRandom()
            items.shuffle(random)
            pickedResults = items.toList()
            persistCurrentItems()
            isPicking = false
            HapticFeedbackUtil.performSuccess(context, settings.hapticsEnabled)
            viewModel.showMessage("List shuffled completely")
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("List Picker", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.toggleFavorite(ToolType.LIST_PICKER) }) {
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
            contentPadding = PaddingValues(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Result Display
            if (pickedResults.isNotEmpty()) {
                item {
                    val display = pickedResults.joinToString(", ")
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .widthIn(max = 520.dp)
                            .graphicsLayer {
                                scaleX = resultScale.value
                                scaleY = resultScale.value
                            }
                    ) {
                        ResultDisplayCard(
                            resultText = display,
                            detailsText = "Selected from your list of ${items.size} items",
                            accentColor = ToolType.LIST_PICKER.accentColor,
                            onCopy = {
                                ShareUtil.copyToClipboard(context, display)
                                viewModel.showMessage("Copied '$display' to clipboard")
                            },
                            onShare = {
                                ShareUtil.shareText(context, "List Picker Result", display)
                            },
                            onRegenerate = { pickItems() }
                        )
                    }
                }
            }

            // Action Buttons
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 520.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { pickItems() },
                        enabled = items.isNotEmpty() && !isPicking,
                        modifier = Modifier
                            .weight(1.3f)
                            .heightIn(min = 48.dp, max = 52.dp)
                            .testTag("pick_items_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ToolType.LIST_PICKER.accentColor)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            if (isPicking) "Picking..." else "Pick Random",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    OutlinedButton(
                        onClick = { shuffleList() },
                        enabled = items.isNotEmpty() && !isPicking,
                        modifier = Modifier
                            .weight(0.9f)
                            .heightIn(min = 48.dp, max = 52.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Default.Shuffle, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "Shuffle",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // Presets Header & Saved Presets Row (Max 3 built-in)
            item {
                Column(modifier = Modifier.fillMaxWidth().widthIn(max = 520.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Ready Presets",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        TextButton(onClick = { showSavePresetDialog = true }) {
                            Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Save Preset", style = MaterialTheme.typography.labelMedium)
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(dbPresets) { preset ->
                            val isPresetActive = items.toList() == preset.getItems()
                            FilterChip(
                                selected = isPresetActive,
                                onClick = {
                                    items.clear()
                                    items.addAll(preset.getItems())
                                    pickedResults = emptyList()
                                    persistCurrentItems()
                                    viewModel.showMessage("Loaded: ${preset.presetName}")
                                },
                                label = {
                                    Text(
                                        preset.presetName,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = ToolType.LIST_PICKER.accentColor.copy(alpha = 0.2f),
                                    selectedLabelColor = ToolType.LIST_PICKER.accentColor
                                ),
                                trailingIcon = if (!preset.isBuiltIn) {
                                    {
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
                                } else null
                            )
                        }
                    }
                }
            }

            // List Options & Add Item Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 520.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "List Items (${items.size})",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            if (items.isNotEmpty()) {
                                TextButton(onClick = {
                                    items.clear()
                                    persistCurrentItems()
                                }) {
                                    Text("Clear All", color = MaterialTheme.colorScheme.error)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Add Entry Field with Character Limit (25 letters)
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(
                                value = newItemInput,
                                onValueChange = {
                                    if (it.length <= MAX_ITEM_LENGTH) {
                                        newItemInput = it
                                    }
                                },
                                label = { Text("Add item (${newItemInput.length}/$MAX_ITEM_LENGTH)") },
                                modifier = Modifier.weight(1f).testTag("input_list_item"),
                                singleLine = true,
                                maxLines = 1
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    if (newItemInput.isNotBlank()) {
                                        items.add(newItemInput.trim())
                                        newItemInput = ""
                                        persistCurrentItems()
                                    }
                                },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.heightIn(min = 48.dp, max = 52.dp)
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
                                Text("Allow Duplicates", style = MaterialTheme.typography.bodyMedium)
                                Text(
                                    "Can pick the same item multiple times",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = allowDuplicates,
                                onCheckedChange = { allowDuplicates = it }
                            )
                        }
                    }
                }
            }

            // Items List
            itemsIndexed(items) { index, item ->
                val isHighlighted = index == activeTickerIndex
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 520.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isHighlighted) {
                            ToolType.LIST_PICKER.accentColor.copy(alpha = 0.35f)
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                        }
                    ),
                    border = if (isHighlighted) {
                        BorderStroke(2.dp, ToolType.LIST_PICKER.accentColor)
                    } else null
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
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = {
                            items.removeAt(index)
                            persistCurrentItems()
                        }) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }

    // Save Preset Dialog
    if (showSavePresetDialog) {
        AlertDialog(
            onDismissRequest = { showSavePresetDialog = false },
            title = { Text("Save Custom Preset") },
            text = {
                Column {
                    Text("Enter a name for this list preset:")
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = newPresetNameInput,
                        onValueChange = { if (it.length <= 30) newPresetNameInput = it },
                        label = { Text("Preset Name (${newPresetNameInput.length}/30)") },
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
                                toolId = ToolType.LIST_PICKER.id,
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
