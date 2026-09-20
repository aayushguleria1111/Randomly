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
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.ToolType
import com.example.ui.components.ResultDisplayCard
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.EmeraldAccent
import com.example.ui.theme.RoseAccent
import com.example.ui.viewmodel.RandomlyViewModel
import com.example.util.HapticFeedbackUtil
import com.example.util.RandomGenerators
import com.example.util.ShareUtil

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun YesNoScreen(
    viewModel: RandomlyViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val settings by viewModel.settings.collectAsState()
    val isFavorite = viewModel.isToolFavorite(ToolType.YES_NO.id)

    var includeMaybe by remember { mutableStateOf(false) }
    var questionInput by remember { mutableStateOf("") }
    var resultText by remember { mutableStateOf<String?>(null) }

    var yesCount by remember { mutableIntStateOf(0) }
    var noCount by remember { mutableIntStateOf(0) }
    var maybeCount by remember { mutableIntStateOf(0) }

    fun decide() {
        val decision = RandomGenerators.generateYesNo(includeMaybe)
        resultText = decision
        HapticFeedbackUtil.performSuccess(context, settings.hapticsEnabled)

        when (decision) {
            "YES" -> yesCount++
            "NO" -> noCount++
            else -> maybeCount++
        }

        viewModel.recordResult(
            toolType = ToolType.YES_NO,
            title = if (questionInput.isNotBlank()) "Q: $questionInput" else "Yes / No Decision",
            result = decision,
            details = if (includeMaybe) "Yes / No / Maybe Mode" else "Binary Yes / No"
        )
    }

    LaunchedEffect(Unit) {
        if (resultText == null) {
            decide()
        }
    }

    val accentColor = when (resultText) {
        "YES" -> EmeraldAccent
        "NO" -> RoseAccent
        else -> AmberAccent
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Yes / No Decision", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.toggleFavorite(ToolType.YES_NO) }) {
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
            resultText?.let { decision ->
                item {
                    val questionPrompt = if (questionInput.isNotBlank()) "Question: \"$questionInput\"" else "Definitive Oracle Decision"
                    ResultDisplayCard(
                        resultText = decision,
                        detailsText = questionPrompt,
                        accentColor = accentColor,
                        onCopy = {
                            ShareUtil.copyToClipboard(context, "$decision ($questionPrompt)")
                            viewModel.showMessage("Copied decision: $decision")
                        },
                        onShare = {
                            ShareUtil.shareText(context, "Yes/No Decision", "$questionPrompt\nDecision: $decision")
                        },
                        onRegenerate = { decide() }
                    )
                }
            }

            // Stats row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = EmeraldAccent.copy(alpha = 0.12f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("YES", style = MaterialTheme.typography.labelMedium, color = EmeraldAccent, fontWeight = FontWeight.Bold)
                            Text("$yesCount", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
                        }
                    }
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = RoseAccent.copy(alpha = 0.12f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("NO", style = MaterialTheme.typography.labelMedium, color = RoseAccent, fontWeight = FontWeight.Bold)
                            Text("$noCount", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
                        }
                    }
                    if (includeMaybe) {
                        Card(
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(containerColor = AmberAccent.copy(alpha = 0.12f))
                        ) {
                            Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("MAYBE", style = MaterialTheme.typography.labelMedium, color = AmberAccent, fontWeight = FontWeight.Bold)
                                Text("$maybeCount", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
                            }
                        }
                    }
                }
            }

            // Controls
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Decision Options", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = questionInput,
                            onValueChange = { questionInput = it },
                            label = { Text("What's your question? (optional)") },
                            modifier = Modifier.fillMaxWidth().testTag("input_yes_no_question"),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Include 'Maybe' Option", style = MaterialTheme.typography.bodyMedium)
                                Text("Allows 3-way decision logic", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Switch(
                                checked = includeMaybe,
                                onCheckedChange = { includeMaybe = it }
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = { decide() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("make_decision_button"),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = ToolType.YES_NO.accentColor)
                        ) {
                            Icon(Icons.Default.HelpOutline, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Make Decision", style = MaterialTheme.typography.titleMedium)
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}
