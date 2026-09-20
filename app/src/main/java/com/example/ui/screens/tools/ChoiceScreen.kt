package com.example.ui.screens.tools

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.example.data.model.ToolType
import com.example.ui.components.ResultDisplayCard
import com.example.ui.theme.AmberAccent
import com.example.ui.viewmodel.RandomlyViewModel
import com.example.util.HapticFeedbackUtil
import com.example.util.RandomGenerators
import com.example.util.ShareUtil

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChoiceScreen(
    viewModel: RandomlyViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val settings by viewModel.settings.collectAsState()
    val isFavorite = viewModel.isToolFavorite(ToolType.CHOICE.id)

    val options = remember {
        mutableStateListOf("Option 1", "Option 2")
    }

    var chosenResult by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    fun makeChoice() {
        errorMessage = null
        val validOptions = options.map { it.trim() }.filter { it.isNotEmpty() }
        if (validOptions.size < 2) {
            errorMessage = "Please enter at least 2 non-empty options."
            return
        }

        val result = RandomGenerators.pickFromList(validOptions, 1, false)
        result.fold(
            onSuccess = { pickedList ->
                val picked = pickedList.first()
                chosenResult = picked
                HapticFeedbackUtil.performSuccess(context, settings.hapticsEnabled)

                viewModel.recordResult(
                    toolType = ToolType.CHOICE,
                    title = "Quick Choice",
                    result = picked,
                    details = "Chosen from: ${validOptions.joinToString(", ")}"
                )
            },
            onFailure = { ex ->
                errorMessage = ex.message ?: "Failed to pick an option"
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Quick Choice", style = MaterialTheme.typography.titleLarge) },
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Result Display
            if (chosenResult != null) {
                item {
                    val validCount = options.count { it.isNotBlank() }
                    ResultDisplayCard(
                        resultText = chosenResult ?: "",
                        detailsText = "Selected out of $validCount options",
                        accentColor = ToolType.CHOICE.accentColor,
                        onCopy = {
                            ShareUtil.copyToClipboard(context, chosenResult ?: "")
                            viewModel.showMessage("Copied '${chosenResult}' to clipboard")
                        },
                        onShare = {
                            ShareUtil.shareText(context, "Quick Choice Result", chosenResult ?: "")
                        },
                        onRegenerate = { makeChoice() }
                    )
                }
            }

            // Options Input List
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
                            Text("Your Options (${options.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            if (options.size < 8) {
                                OutlinedButton(onClick = { options.add("Option ${options.size + 1}") }) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Add Option")
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        options.forEachIndexed { index, opt ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = opt,
                                    onValueChange = { options[index] = it },
                                    label = { Text("Option ${index + 1}") },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("option_input_$index"),
                                    singleLine = true
                                )
                                if (options.size > 2) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    IconButton(onClick = { options.removeAt(index) }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Remove", tint = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }
                        }

                        if (errorMessage != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("❌ $errorMessage", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = { makeChoice() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("make_choice_button"),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(Icons.Default.Shuffle, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Choose Randomly", style = MaterialTheme.typography.titleMedium)
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}
