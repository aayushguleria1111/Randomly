package com.example.ui.screens.tools

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.data.model.ToolType
import com.example.ui.components.AnimatedDiceView
import com.example.ui.components.ResultDisplayCard
import com.example.ui.components.SectionHeader
import com.example.ui.theme.AmberAccent
import com.example.ui.viewmodel.RandomlyViewModel
import com.example.util.HapticFeedbackUtil
import com.example.util.RandomGenerators
import com.example.util.RandomGenerators.DiceRollResult
import com.example.util.RandomGenerators.DiceType
import com.example.util.ShareUtil
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiceScreen(
    viewModel: RandomlyViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val settings by viewModel.settings.collectAsState()
    val isFavorite = viewModel.isToolFavorite(ToolType.DICE.id)

    var selectedDiceType by remember {
        mutableStateOf(DiceType.fromLabel(settings.defaultDiceType))
    }
    var diceCount by remember { mutableIntStateOf(1) }
    var modifierValue by remember { mutableIntStateOf(0) }

    var isRolling by remember { mutableStateOf(false) }
    var rollResult by remember { mutableStateOf<DiceRollResult?>(null) }

    fun roll() {
        if (isRolling) return
        isRolling = true

        scope.launch {
            HapticFeedbackUtil.performImpact(context, settings.hapticsEnabled)
            if (settings.animationsEnabled) {
                // Rhythmic tumble haptics
                for (i in 1..4) {
                    delay(130)
                    HapticFeedbackUtil.performImpact(context, settings.hapticsEnabled)
                }
                delay(130)
            }
            val result = RandomGenerators.rollDice(selectedDiceType, diceCount, modifierValue)
            rollResult = result
            isRolling = false
            HapticFeedbackUtil.performSuccess(context, settings.hapticsEnabled)

            viewModel.recordResult(
                toolType = ToolType.DICE,
                title = "Dice Roll (${diceCount}×${selectedDiceType.label})",
                result = "${result.total}",
                details = result.equation
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dice Roller", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.toggleFavorite(ToolType.DICE) }) {
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
            // Animated Dice View
            item {
                Box(modifier = Modifier.fillMaxWidth().widthIn(max = 520.dp)) {
                    AnimatedDiceView(
                        diceType = rollResult?.diceType ?: selectedDiceType,
                        rolls = rollResult?.rolls ?: List(diceCount) { 1 },
                        isRolling = isRolling,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Result Card
            item {
                Box(modifier = Modifier.fillMaxWidth().widthIn(max = 520.dp)) {
                    ResultDisplayCard(
                        resultText = rollResult?.let { "${it.total}" } ?: "—",
                        detailsText = rollResult?.equation ?: "Ready to roll ${diceCount}×${selectedDiceType.label}${if (modifierValue != 0) (if (modifierValue > 0) " +$modifierValue" else " $modifierValue") else ""}",
                        accentColor = ToolType.DICE.accentColor,
                        onCopy = {
                            rollResult?.let { result ->
                                ShareUtil.copyToClipboard(context, "${result.total} (${result.equation})")
                                viewModel.showMessage("Copied dice result: ${result.total}")
                            }
                        },
                        onShare = {
                            rollResult?.let { result ->
                                ShareUtil.shareText(
                                    context,
                                    "Dice Roll (${diceCount}×${result.diceType.label})",
                                    "Result: ${result.total}\nEquation: ${result.equation}"
                                )
                            }
                        },
                        onRegenerate = { roll() }
                    )
                }
            }

            // Controls Card
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
                            text = "Choose Dice Type",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(DiceType.entries) { type ->
                                FilterChip(
                                    selected = selectedDiceType == type,
                                    onClick = {
                                        selectedDiceType = type
                                    },
                                    label = { Text(type.label) }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Dice Count Selector (1 to 10)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Number of Dice", style = MaterialTheme.typography.bodyLarge)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                FilledTonalIconButton(
                                    onClick = { if (diceCount > 1) diceCount-- },
                                    enabled = diceCount > 1
                                ) {
                                    Icon(Icons.Default.Remove, contentDescription = "Decrease count")
                                }
                                Text(
                                    text = "$diceCount",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )
                                FilledTonalIconButton(
                                    onClick = { if (diceCount < 10) diceCount++ },
                                    enabled = diceCount < 10
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = "Increase count")
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Modifier Selector (-10 to +10)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Modifier (+ / -)", style = MaterialTheme.typography.bodyLarge)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                FilledTonalIconButton(
                                    onClick = { modifierValue-- }
                                ) {
                                    Icon(Icons.Default.Remove, contentDescription = "Decrease modifier")
                                }
                                Text(
                                    text = if (modifierValue >= 0) "+$modifierValue" else "$modifierValue",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 12.dp)
                                )
                                FilledTonalIconButton(
                                    onClick = { modifierValue++ }
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = "Increase modifier")
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = { roll() },
                            enabled = !isRolling,
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 48.dp, max = 52.dp)
                                .testTag("roll_dice_button"),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = ToolType.DICE.accentColor)
                        ) {
                            Icon(Icons.Default.Casino, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                if (isRolling) "ROLLING..." else "ROLL DICE",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}
