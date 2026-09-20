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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FormatColorText
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.model.ToolType
import com.example.ui.components.ResultDisplayCard
import com.example.ui.theme.AmberAccent
import com.example.ui.viewmodel.RandomlyViewModel
import com.example.util.HapticFeedbackUtil
import com.example.util.RandomGenerators
import com.example.util.RandomGenerators.LetterCase
import com.example.util.ShareUtil

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LetterScreen(
    viewModel: RandomlyViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val settings by viewModel.settings.collectAsState()
    val isFavorite = viewModel.isToolFavorite(ToolType.LETTER.id)

    var letterCase by remember { mutableStateOf(LetterCase.UPPERCASE) }
    var countInput by remember { mutableStateOf("1") }
    var allowDuplicates by remember { mutableStateOf(true) }

    var results by remember { mutableStateOf<List<Char>>(emptyList()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    fun generate() {
        errorMessage = null
        val count = countInput.toIntOrNull() ?: 1
        if (count < 1 || count > 52) {
            errorMessage = "Count must be between 1 and 52"
            return
        }

        val genResult = RandomGenerators.generateLetters(count, letterCase, allowDuplicates)
        genResult.fold(
            onSuccess = { letters ->
                results = letters
                HapticFeedbackUtil.performSuccess(context, settings.hapticsEnabled)
                val display = letters.joinToString(" ")
                viewModel.recordResult(
                    toolType = ToolType.LETTER,
                    title = "Random Letter (${letterCase.name})",
                    result = display,
                    details = "Count: $count"
                )
            },
            onFailure = { ex ->
                errorMessage = ex.message ?: "Invalid configuration"
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
            if (results.isNotEmpty()) {
                item {
                    val resultDisplay = results.joinToString(" ")
                    ResultDisplayCard(
                        resultText = resultDisplay,
                        detailsText = "Case: ${letterCase.name} • ${results.size} letter${if (results.size > 1) "s" else ""}",
                        accentColor = ToolType.LETTER.accentColor,
                        onCopy = {
                            ShareUtil.copyToClipboard(context, resultDisplay)
                            viewModel.showMessage("Copied '$resultDisplay' to clipboard")
                        },
                        onShare = {
                            ShareUtil.shareText(context, "Random Letters", resultDisplay)
                        },
                        onRegenerate = { generate() }
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
                        Text("Letter Case", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            LetterCase.entries.forEach { case ->
                                FilterChip(
                                    selected = letterCase == case,
                                    onClick = {
                                        letterCase = case
                                        generate()
                                    },
                                    label = { Text(case.name.lowercase().replaceFirstChar { it.uppercase() }) }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = countInput,
                            onValueChange = { countInput = it },
                            label = { Text("Count") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Allow Duplicates", style = MaterialTheme.typography.bodyMedium)
                            Switch(
                                checked = allowDuplicates,
                                onCheckedChange = { allowDuplicates = it }
                            )
                        }

                        if (errorMessage != null) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text("❌ $errorMessage", color = MaterialTheme.colorScheme.error)
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = { generate() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("generate_letter_button"),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(Icons.Default.FormatColorText, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Generate Letters", style = MaterialTheme.typography.titleMedium)
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}
