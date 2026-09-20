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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
fun StringGeneratorScreen(
    viewModel: RandomlyViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val settings by viewModel.settings.collectAsState()
    val isFavorite = viewModel.isToolFavorite(ToolType.STRING_GENERATOR.id)

    var lengthSlider by remember { mutableFloatStateOf(16f) }
    var includeUpper by remember { mutableStateOf(true) }
    var includeLower by remember { mutableStateOf(true) }
    var includeNumbers by remember { mutableStateOf(true) }
    var includeSymbols by remember { mutableStateOf(true) }
    var avoidAmbiguous by remember { mutableStateOf(false) }

    var generatedString by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    fun generate() {
        errorMessage = null
        val length = lengthSlider.toInt()
        val result = RandomGenerators.generateString(
            length = length,
            includeUpper = includeUpper,
            includeLower = includeLower,
            includeNumbers = includeNumbers,
            includeSymbols = includeSymbols,
            avoidAmbiguous = avoidAmbiguous
        )

        result.fold(
            onSuccess = { str ->
                generatedString = str
                HapticFeedbackUtil.performSuccess(context, settings.hapticsEnabled)
                viewModel.recordResult(
                    toolType = ToolType.STRING_GENERATOR,
                    title = "Random String ($length chars)",
                    result = str,
                    details = "Test / Dummy Value"
                )
            },
            onFailure = { ex ->
                errorMessage = ex.message ?: "Select at least one character type"
            }
        )
    }

    LaunchedEffect(Unit) {
        if (generatedString.isEmpty()) {
            generate()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Random String / Pass", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.toggleFavorite(ToolType.STRING_GENERATOR) }) {
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
            // Notice Disclaimer Banner
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Generated strings are random mock values for testing, dummy data, and everyday convenience.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            if (generatedString.isNotEmpty()) {
                item {
                    ResultDisplayCard(
                        resultText = generatedString,
                        detailsText = "Length: ${lengthSlider.toInt()} characters",
                        accentColor = ToolType.STRING_GENERATOR.accentColor,
                        onCopy = {
                            ShareUtil.copyToClipboard(context, generatedString)
                            viewModel.showMessage("Copied string to clipboard")
                        },
                        onShare = {
                            ShareUtil.shareText(context, "Random String", generatedString)
                        },
                        onRegenerate = { generate() }
                    )
                }
            }

            // Options Card
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
                            Text("String Length", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text("${lengthSlider.toInt()}", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        }

                        Slider(
                            value = lengthSlider,
                            onValueChange = { lengthSlider = it },
                            valueRange = 4f..64f,
                            steps = 59,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Toggles
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = includeUpper, onCheckedChange = { includeUpper = it; generate() })
                            Text("Uppercase Letters (A-Z)")
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = includeLower, onCheckedChange = { includeLower = it; generate() })
                            Text("Lowercase Letters (a-z)")
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = includeNumbers, onCheckedChange = { includeNumbers = it; generate() })
                            Text("Numbers (0-9)")
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = includeSymbols, onCheckedChange = { includeSymbols = it; generate() })
                            Text("Symbols (!@#$%...)")
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = avoidAmbiguous, onCheckedChange = { avoidAmbiguous = it; generate() })
                            Text("Avoid Ambiguous (O, 0, I, l, 1)")
                        }

                        if (errorMessage != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("❌ $errorMessage", color = MaterialTheme.colorScheme.error)
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = { generate() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("generate_string_button"),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(Icons.Default.Key, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Generate Random String", style = MaterialTheme.typography.titleMedium)
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}
