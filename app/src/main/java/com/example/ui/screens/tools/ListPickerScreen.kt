package com.example.ui.screens.tools

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
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.Save
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.example.ui.theme.AmberAccent
import com.example.ui.viewmodel.RandomlyViewModel
import com.example.util.HapticFeedbackUtil
import com.example.util.RandomGenerators
import com.example.util.ShareUtil

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

    val items = remember {
        mutableStateListOf("Pizza", "Burger", "Pasta", "Sandwich", "Sushi", "Tacos")
    }

    var newItemText by remember { mutableStateOf("") }
    var pickCount by remember { mutableIntStateOf(1) }
    var allowDuplicates by remember { mutableStateOf(false) }
    var removePickedAfterSelection by remember { mutableStateOf(false) }

    var pickedResults by remember { mutableStateOf<List<String>>(emptyList()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    var showSavePresetDialog by remember { mutableStateOf(false) }
    var newPresetNameInput by remember { mutableStateOf("") }
    var presetToDelete by remember { mutableStateOf<ToolPreset?>(null) }
    var showPasteDialog by remember { mutableStateOf(false) }
    var pasteText by remember { mutableStateOf("") }

    fun pick() {
        errorMessage = null
        val result = RandomGenerators.pickFromList(items, pickCount, allowDuplicates)
        result.fold(
            onSuccess = { picked ->
                pickedResults = picked
                HapticFeedbackUtil.performSuccess(context, settings.hapticsEnabled)
                val resultText = picked.joinToString(", ")
                viewModel.recordResult(
                    toolType = ToolType.LIST_PICKER,
                    title = "List Picker",
                    result = resultText,
                    details = "Picked $pickCount from ${items.size} items"
                )

                if (removePickedAfterSelection) {
                    picked.forEach { items.remove(it) }
                }
            },
            onFailure = { ex ->
                errorMessage = ex.message ?: "Invalid selection"
            }
        )
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Result Display
            if (pickedResults.isNotEmpty()) {
                item {
                    val resultDisplay = pickedResults.joinToString(", ")
                    ResultDisplayCard(
                        resultText = resultDisplay,
                        detailsText = "Selected $pickCount item${if (pickCount > 1) "s" else ""} from list",
                        accentColor = ToolType.LIST_PICKER.accentColor,
                        onCopy = {
                            ShareUtil.copyToClipboard(context, resultDisplay)
                            viewModel.showMessage("Copied '$resultDisplay' to clipboard")
                        },
                        onShare = {
                            ShareUtil.shareText(context, "List Picked Result", resultDisplay)
                        },
                        onRegenerate = { pick() }
                    )
                }
            }

            // Presets selector bar
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
                        Text("Save as Preset")
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(dbPresets) { preset ->
                        FilterChip(
                            selected = false,
                            onClick = {
                                items.clear()
                                items.addAll(preset.getItems())
                                viewModel.showMessage("Loaded preset: ${preset.presetName}")
                            },
                            label = { Text("${preset.presetName} (${preset.getItems().size})") },
                            trailingIcon = {
                                IconButton(
                                    onClick = { presetToDelete = preset },
                                    modifier = Modifier.size(18.dp)
                                ) {
                                    Icon(Icons.Default.Clear, contentDescription = "Delete preset", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        )
                    }
                }
            }

            // Controls Card
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
                            Text("List Items (${items.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            TextButton(onClick = { showPasteDialog = true }) {
                                Text("Paste Multiline")
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Add item
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(
                                value = newItemText,
                                onValueChange = { newItemText = it },
                                label = { Text("Add item") },
                                modifier = Modifier.weight(1f).testTag("input_list_item"),
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

                        // Pick Count selector
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Pick Count", style = MaterialTheme.typography.bodyMedium)
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                listOf(1, 2, 3, 5).forEach { count ->
                                    FilterChip(
                                        selected = pickCount == count,
                                        onClick = { pickCount = count },
                                        label = { Text("$count") }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Remove Picked Item", style = MaterialTheme.typography.bodyMedium)
                                Text("Removes selected item from the list", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Switch(
                                checked = removePickedAfterSelection,
                                onCheckedChange = { removePickedAfterSelection = it }
                            )
                        }

                        if (errorMessage != null) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text("❌ $errorMessage", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = { pick() },
                            enabled = items.isNotEmpty(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("pick_from_list_button"),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = ToolType.LIST_PICKER.accentColor)
                        ) {
                            Icon(Icons.Default.FormatListBulleted, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("PICK FROM LIST", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // List items display
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
                        IconButton(onClick = { items.removeAt(index) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f))
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
                OutlinedTextField(
                    value = newPresetNameInput,
                    onValueChange = { newPresetNameInput = it },
                    label = { Text("Preset Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(onClick = {
                    if (newPresetNameInput.isNotBlank() && items.isNotEmpty()) {
                        viewModel.savePreset(
                            toolId = ToolType.LIST_PICKER.id,
                            name = newPresetNameInput.trim(),
                            items = items.toList()
                        )
                        newPresetNameInput = ""
                        showSavePresetDialog = false
                    }
                }) {
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
            text = { Text("Are you sure you want to delete '${preset.presetName}'?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deletePreset(preset.id)
                    presetToDelete = null
                }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { presetToDelete = null }) { Text("Cancel") }
            }
        )
    }

    // Paste Multiline Items Dialog
    if (showPasteDialog) {
        AlertDialog(
            onDismissRequest = { showPasteDialog = false },
            title = { Text("Paste Multiple Items") },
            text = {
                Column {
                    Text("Enter one item per line:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = pasteText,
                        onValueChange = { pasteText = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp),
                        placeholder = { Text("Pizza\nBurger\nPasta\nSushi") }
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    val lines = pasteText.lines().map { it.trim() }.filter { it.isNotEmpty() }
                    if (lines.isNotEmpty()) {
                        items.addAll(lines)
                        pasteText = ""
                        showPasteDialog = false
                    }
                }) {
                    Text("Add All")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPasteDialog = false }) { Text("Cancel") }
            }
        )
    }
}
