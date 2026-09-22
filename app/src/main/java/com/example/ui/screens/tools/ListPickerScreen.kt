package com.example.ui.screens.tools

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Remove
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.ToolPreset
import com.example.data.model.ToolType
import com.example.data.model.WeightedListItem
import com.example.ui.components.ResultDisplayCard
import com.example.ui.theme.AmberAccent
import com.example.ui.viewmodel.RandomlyViewModel
import com.example.util.HapticFeedbackUtil
import com.example.util.ShareUtil
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
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

    val defaultItems = remember {
        listOf("Action", "Comedy", "Sci-Fi", "Horror", "Drama", "Animation")
            .map { WeightedListItem(text = it, weight = 1) }
    }
    val items = remember { mutableStateListOf<WeightedListItem>().apply { addAll(defaultItems) } }
    var hasLoadedLastUsed by remember { mutableStateOf(false) }

    // Load persisted items
    LaunchedEffect(Unit) {
        val defaultSerialized = defaultItems.map { it.toSerialized() }
        viewModel.getLastUsedItems(ToolType.LIST_PICKER.id, defaultSerialized).collect { saved ->
            if (!hasLoadedLastUsed && saved.isNotEmpty()) {
                items.clear()
                items.addAll(saved.map { WeightedListItem.fromSerialized(it) })
                hasLoadedLastUsed = true
            }
        }
    }

    fun persistCurrentItems() {
        viewModel.saveLastUsedItems(ToolType.LIST_PICKER.id, items.map { it.toSerialized() })
    }

    val scope = rememberCoroutineScope()
    var isPicking by remember { mutableStateOf(false) }
    var activeTickerIndex by remember { mutableIntStateOf(-1) }
    val resultScale = remember { Animatable(1f) }

    var newItemInput by remember { mutableStateOf("") }
    var newItemWeight by remember { mutableIntStateOf(1) }
    var pickCount by remember { mutableIntStateOf(1) }
    var allowDuplicates by remember { mutableStateOf(false) }
    var pickedResults by remember { mutableStateOf<List<String>>(emptyList()) }

    var showSavePresetDialog by remember { mutableStateOf(false) }
    var newPresetNameInput by remember { mutableStateOf("") }
    var presetToDelete by remember { mutableStateOf<ToolPreset?>(null) }

    // Direct edit weight dialog state
    var editingItemIndex by remember { mutableIntStateOf(-1) }
    var editWeightDialogText by remember { mutableStateOf("1") }

    val totalWeight = items.sumOf { it.weight.coerceIn(WeightedListItem.MIN_WEIGHT, WeightedListItem.MAX_WEIGHT) }
    val hasCustomWeights = items.any { it.weight > 1 }

    fun resetAllWeights() {
        for (i in items.indices) {
            items[i] = items[i].copy(weight = 1)
        }
        persistCurrentItems()
        viewModel.showMessage("All weights reset to 1×")
    }

    fun pickItems() {
        if (items.isEmpty() || isPicking) return
        isPicking = true
        HapticFeedbackUtil.performImpact(context, settings.hapticsEnabled)

        scope.launch {
            val count = pickCount.coerceAtLeast(1)
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

            val selectedItems = WeightedListItem.sampleWeighted(
                items = items.toList(),
                count = count,
                allowDuplicates = allowDuplicates,
                random = random
            )

            pickedResults = selectedItems.map { it.text }
            val resultStr = pickedResults.joinToString(", ")

            if (settings.animationsEnabled) {
                resultScale.snapTo(0.7f)
                resultScale.animateTo(
                    1f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                    )
                )
            }

            HapticFeedbackUtil.performSuccess(context, settings.hapticsEnabled)
            isPicking = false

            val details = if (hasCustomWeights) {
                "Picked $count item(s) by weight • Total weight: $totalWeight"
            } else {
                "Picked $count out of ${items.size} items"
            }

            viewModel.recordResult(
                toolType = ToolType.LIST_PICKER,
                title = "List Picker",
                result = resultStr,
                details = details
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
            pickedResults = items.map { it.text }
            persistCurrentItems()
            isPicking = false
            HapticFeedbackUtil.performSuccess(context, settings.hapticsEnabled)
            viewModel.showMessage("List order shuffled")
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
                .padding(horizontal = 12.dp),
            contentPadding = PaddingValues(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Result Display
            if (pickedResults.isNotEmpty()) {
                item {
                    val display = pickedResults.joinToString(", ")
                    val detailsText = if (hasCustomWeights) {
                        "Picked with weighted odds • Total weight: $totalWeight"
                    } else {
                        "Selected from your list of ${items.size} items"
                    }
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
                            detailsText = detailsText,
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

            // Presets Header & Saved Presets Row
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
                            val isPresetActive = items.map { it.toSerialized() } == preset.getItems()
                            FilterChip(
                                selected = isPresetActive,
                                onClick = {
                                    items.clear()
                                    items.addAll(preset.getItems().map { WeightedListItem.fromSerialized(it) })
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
                    Column(modifier = Modifier.padding(14.dp)) {
                        // Section Header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    "List Items (${items.size})",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                if (hasCustomWeights) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = ToolType.LIST_PICKER.accentColor.copy(alpha = 0.15f)
                                    ) {
                                        Text(
                                            text = "Total Wt: $totalWeight",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = ToolType.LIST_PICKER.accentColor,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (hasCustomWeights) {
                                    TextButton(
                                        onClick = { resetAllWeights() },
                                        contentPadding = PaddingValues(horizontal = 6.dp)
                                    ) {
                                        Text("Equalize (1×)", style = MaterialTheme.typography.labelSmall)
                                    }
                                }
                                if (items.isNotEmpty()) {
                                    TextButton(
                                        onClick = {
                                            items.clear()
                                            persistCurrentItems()
                                        },
                                        contentPadding = PaddingValues(horizontal = 6.dp)
                                    ) {
                                        Text("Clear", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Add Entry Field with Weight Stepper & Character Limit (25 letters)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = newItemInput,
                                onValueChange = {
                                    if (it.length <= MAX_ITEM_LENGTH) {
                                        newItemInput = it
                                    }
                                },
                                label = { Text("Add item (${newItemInput.length}/$MAX_ITEM_LENGTH)") },
                                placeholder = { Text("New item name") },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("input_list_item"),
                                singleLine = true,
                                maxLines = 1
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            // Compact Initial Weight Stepper
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                                modifier = Modifier.heightIn(min = 48.dp, max = 52.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 2.dp)
                                ) {
                                    IconButton(
                                        onClick = { if (newItemWeight > 1) newItemWeight-- },
                                        enabled = newItemWeight > 1,
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Remove,
                                            contentDescription = "Decrease weight",
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }

                                    Text(
                                        text = "${newItemWeight}×",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (newItemWeight > 1) ToolType.LIST_PICKER.accentColor else MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.padding(horizontal = 4.dp)
                                    )

                                    IconButton(
                                        onClick = { if (newItemWeight < 99) newItemWeight++ },
                                        enabled = newItemWeight < 99,
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Add,
                                            contentDescription = "Increase weight",
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Button(
                                onClick = {
                                    if (newItemInput.isNotBlank()) {
                                        items.add(
                                            WeightedListItem(
                                                text = newItemInput.trim(),
                                                weight = newItemWeight
                                            )
                                        )
                                        newItemInput = ""
                                        newItemWeight = 1
                                        persistCurrentItems()
                                    }
                                },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.heightIn(min = 48.dp, max = 52.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Add")
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Compact Settings Row: Pick Count & Allow Duplicates
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Pick Count Stepper
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    "Pick:",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                IconButton(
                                    onClick = { if (pickCount > 1) pickCount-- },
                                    enabled = pickCount > 1,
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Remove,
                                        contentDescription = "Decrease count",
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                                Text(
                                    text = "$pickCount",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 4.dp)
                                )
                                IconButton(
                                    onClick = {
                                        val maxAllowed = if (allowDuplicates) 50 else items.size.coerceAtLeast(1)
                                        if (pickCount < maxAllowed) pickCount++
                                    },
                                    enabled = (allowDuplicates && pickCount < 50) || (!allowDuplicates && pickCount < items.size),
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Add,
                                        contentDescription = "Increase count",
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }

                            // Allow Repeat Chip
                            FilterChip(
                                selected = allowDuplicates,
                                onClick = {
                                    allowDuplicates = !allowDuplicates
                                    if (!allowDuplicates && pickCount > items.size && items.isNotEmpty()) {
                                        pickCount = items.size
                                    }
                                },
                                label = { Text("Allow Repeat", style = MaterialTheme.typography.labelSmall) },
                                leadingIcon = if (allowDuplicates) {
                                    {
                                        Icon(
                                            Icons.Default.Check,
                                            contentDescription = null,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                } else null
                            )
                        }
                    }
                }
            }

            // Items List
            itemsIndexed(items, key = { _, item -> item.id }) { index, item ->
                val isHighlighted = index == activeTickerIndex
                val pct = if (totalWeight > 0) (item.weight * 100f / totalWeight) else 0f
                val pctFormatted = if (pct >= 10f) "%.0f%%".format(pct) else "%.1f%%".format(pct)

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 520.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isHighlighted) {
                            ToolType.LIST_PICKER.accentColor.copy(alpha = 0.35f)
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                        }
                    ),
                    border = if (isHighlighted) {
                        BorderStroke(2.dp, ToolType.LIST_PICKER.accentColor)
                    } else null
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 14.dp, end = 6.dp, top = 6.dp, bottom = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Item Index & Name & probability badge
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${index + 1}. ${item.text}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = if (item.weight > 1) {
                                    "Weight: ${item.weight}× • $pctFormatted chance"
                                } else {
                                    "Weight: 1× • $pctFormatted chance"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = if (item.weight > 1) {
                                    ToolType.LIST_PICKER.accentColor
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                        }

                        // Weight Stepper Controls: [-] [badge (tap to edit)] [+]
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(end = 4.dp)
                        ) {
                            IconButton(
                                onClick = {
                                    if (item.weight > 1) {
                                        items[index] = item.copy(weight = item.weight - 1)
                                        persistCurrentItems()
                                    }
                                },
                                enabled = item.weight > 1,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    Icons.Default.Remove,
                                    contentDescription = "Decrease weight",
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            // Weight badge - tapping opens quick direct edit dialog
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (item.weight > 1) {
                                    ToolType.LIST_PICKER.accentColor.copy(alpha = 0.18f)
                                } else {
                                    MaterialTheme.colorScheme.surfaceVariant
                                },
                                border = BorderStroke(
                                    1.dp,
                                    if (item.weight > 1) {
                                        ToolType.LIST_PICKER.accentColor.copy(alpha = 0.6f)
                                    } else {
                                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                    }
                                ),
                                modifier = Modifier.clickable {
                                    editingItemIndex = index
                                    editWeightDialogText = item.weight.toString()
                                }
                            ) {
                                Text(
                                    text = "${item.weight}×",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (item.weight > 1) {
                                        ToolType.LIST_PICKER.accentColor
                                    } else {
                                        MaterialTheme.colorScheme.onSurface
                                    },
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                                )
                            }

                            IconButton(
                                onClick = {
                                    if (item.weight < 99) {
                                        items[index] = item.copy(weight = item.weight + 1)
                                        persistCurrentItems()
                                    }
                                },
                                enabled = item.weight < 99,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    Icons.Default.Add,
                                    contentDescription = "Increase weight",
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        // Delete button
                        IconButton(
                            onClick = {
                                items.removeAt(index)
                                persistCurrentItems()
                            },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = MaterialTheme.colorScheme.error.copy(alpha = 0.75f),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }

    // Direct Edit Weight Dialog
    if (editingItemIndex in items.indices) {
        val currentItem = items[editingItemIndex]
        AlertDialog(
            onDismissRequest = { editingItemIndex = -1 },
            title = { Text("Weight for \"${currentItem.text}\"") },
            text = {
                Column {
                    Text(
                        "Higher weights make this item proportionally more likely to be selected.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = editWeightDialogText,
                        onValueChange = { input ->
                            val digitsOnly = input.filter { it.isDigit() }.take(2)
                            editWeightDialogText = digitsOnly
                        },
                        label = { Text("Weight (1-99)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("Quick Presets:", style = MaterialTheme.typography.labelSmall)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        listOf(1, 2, 3, 5, 10).forEach { presetVal ->
                            FilterChip(
                                selected = editWeightDialogText == presetVal.toString(),
                                onClick = { editWeightDialogText = presetVal.toString() },
                                label = { Text("${presetVal}×", style = MaterialTheme.typography.labelSmall) }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val parsed = editWeightDialogText.toIntOrNull()?.coerceIn(1, 99) ?: 1
                        items[editingItemIndex] = currentItem.copy(weight = parsed)
                        persistCurrentItems()
                        editingItemIndex = -1
                    }
                ) {
                    Text("Apply")
                }
            },
            dismissButton = {
                TextButton(onClick = { editingItemIndex = -1 }) {
                    Text("Cancel")
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
                                items = items.map { it.toSerialized() }
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
