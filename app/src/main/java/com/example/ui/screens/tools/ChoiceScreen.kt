package com.example.ui.screens.tools

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.material.icons.filled.TipsAndUpdates
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ToolPreset
import com.example.data.model.ToolType
import com.example.ui.components.ResultDisplayCard
import com.example.ui.theme.AmberAccent
import com.example.ui.viewmodel.RandomlyViewModel
import com.example.util.HapticFeedbackUtil
import com.example.util.ShareUtil
import java.security.SecureRandom

private const val MAX_OPTION_LENGTH = 25

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChoiceScreen(
    viewModel: RandomlyViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val settings by viewModel.settings.collectAsState()
    val isFavorite = viewModel.isToolFavorite(ToolType.CHOICE.id)

    val dbPresets by viewModel.getPresetsForTool(ToolType.CHOICE.id).collectAsState(initial = emptyList())

    val defaultOptions = remember { listOf("Cook at Home", "Order Delivery", "Dine Out") }
    val options = remember { mutableStateListOf<String>().apply { addAll(defaultOptions) } }
    var hasLoadedLastUsed by remember { mutableStateOf(false) }

    // Load persisted last used options
    LaunchedEffect(Unit) {
        viewModel.getLastUsedItems(ToolType.CHOICE.id, defaultOptions).collect { saved ->
            if (!hasLoadedLastUsed && saved.isNotEmpty()) {
                options.clear()
                options.addAll(saved)
                hasLoadedLastUsed = true
            }
        }
    }

    fun persistCurrentOptions() {
        viewModel.saveLastUsedItems(ToolType.CHOICE.id, options.toList())
    }

    var newOptionInput by remember { mutableStateOf("") }
    var selectedWinner by remember { mutableStateOf<String?>(null) }

    var showSavePresetDialog by remember { mutableStateOf(false) }
    var newPresetNameInput by remember { mutableStateOf("") }
    var presetToDelete by remember { mutableStateOf<ToolPreset?>(null) }

    fun pickWinner() {
        if (options.isEmpty()) return
        HapticFeedbackUtil.performImpact(context, settings.hapticsEnabled)

        val random = SecureRandom()
        val winner = options[random.nextInt(options.size)]
        selectedWinner = winner

        HapticFeedbackUtil.performSuccess(context, settings.hapticsEnabled)

        viewModel.recordResult(
            toolType = ToolType.CHOICE,
            title = "Random Choice",
            result = winner,
            details = "Chosen from ${options.size} options"
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Make a Choice", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.toggleFavorite(ToolType.CHOICE) }) {
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
            // Choice Selection Stage (Distinct High-Contrast Spotlight)
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 520.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            enabled = options.isNotEmpty(),
                            onClick = { pickWinner() }
                        ),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = ToolType.CHOICE.accentColor.copy(alpha = 0.12f)
                    ),
                    border = BorderStroke(2.dp, ToolType.CHOICE.accentColor)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 26.dp, horizontal = 20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Badge Icon
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .clip(CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.TipsAndUpdates,
                                contentDescription = null,
                                tint = ToolType.CHOICE.accentColor,
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        AnimatedContent(
                            targetState = selectedWinner ?: "Tap to Decide",
                            transitionSpec = { fadeIn() togetherWith fadeOut() },
                            label = "choiceWinner"
                        ) { displayChoice ->
                            Text(
                                text = displayChoice,
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontSize = if (displayChoice.length > 15) 24.sp else 30.sp
                                ),
                                fontWeight = FontWeight.ExtraBold,
                                color = ToolType.CHOICE.accentColor,
                                textAlign = TextAlign.Center,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = if (selectedWinner != null) "Selected from ${options.size} options" else "Tap stage or button to choose from list",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Decide Button (Clean, responsive size)
            item {
                Button(
                    onClick = { pickWinner() },
                    enabled = options.isNotEmpty(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 520.dp)
                        .heightIn(min = 48.dp, max = 52.dp)
                        .testTag("choose_for_me_button"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ToolType.CHOICE.accentColor)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "DECIDE FOR ME",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Result Display Card
            selectedWinner?.let { winner ->
                item {
                    Box(modifier = Modifier.fillMaxWidth().widthIn(max = 520.dp)) {
                        ResultDisplayCard(
                            resultText = winner,
                            detailsText = "The decision is made!",
                            accentColor = ToolType.CHOICE.accentColor,
                            onCopy = {
                                ShareUtil.copyToClipboard(context, winner)
                                viewModel.showMessage("Copied '$winner' to clipboard")
                            },
                            onShare = {
                                ShareUtil.shareText(context, "Chosen Option", winner)
                            },
                            onRegenerate = { pickWinner() }
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
                            val isPresetActive = options.toList() == preset.getItems()
                            FilterChip(
                                selected = isPresetActive,
                                onClick = {
                                    options.clear()
                                    options.addAll(preset.getItems())
                                    selectedWinner = null
                                    persistCurrentOptions()
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
                                    selectedContainerColor = ToolType.CHOICE.accentColor.copy(alpha = 0.2f),
                                    selectedLabelColor = ToolType.CHOICE.accentColor
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

            // Options Input Card with Limit (25 letters)
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
                                "Choice Options (${options.size})",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            IconButton(onClick = {
                                if (options.isNotEmpty()) {
                                    options.shuffle()
                                    persistCurrentOptions()
                                    viewModel.showMessage("Options shuffled")
                                }
                            }) {
                                Icon(Icons.Default.Shuffle, contentDescription = "Shuffle", tint = MaterialTheme.colorScheme.primary)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(
                                value = newOptionInput,
                                onValueChange = {
                                    if (it.length <= MAX_OPTION_LENGTH) {
                                        newOptionInput = it
                                    }
                                },
                                label = { Text("Enter option (${newOptionInput.length}/$MAX_OPTION_LENGTH)") },
                                modifier = Modifier.weight(1f).testTag("input_choice_option"),
                                singleLine = true,
                                maxLines = 1
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    if (newOptionInput.isNotBlank()) {
                                        options.add(newOptionInput.trim())
                                        newOptionInput = ""
                                        persistCurrentOptions()
                                    }
                                },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.heightIn(min = 48.dp, max = 52.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Add")
                            }
                        }
                    }
                }
            }

            // Options List
            itemsIndexed(options) { index, option ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 520.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${index + 1}. $option",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = {
                            options.removeAt(index)
                            persistCurrentOptions()
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
                    Text("Enter a name for this choice preset:")
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
                        if (newPresetNameInput.isNotBlank() && options.isNotEmpty()) {
                            viewModel.savePreset(
                                toolId = ToolType.CHOICE.id,
                                name = newPresetNameInput.trim(),
                                items = options.toList()
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
