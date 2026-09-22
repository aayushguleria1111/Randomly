package com.example.ui.screens.tools

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.FilterNone
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.ToolType
import com.example.ui.components.PlayingCardView
import com.example.ui.components.ResultDisplayCard
import com.example.ui.theme.AmberAccent
import com.example.ui.viewmodel.RandomlyViewModel
import com.example.util.HapticFeedbackUtil
import com.example.util.RandomGenerators
import com.example.util.RandomGenerators.PlayingCard
import com.example.util.ShareUtil
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CardScreen(
    viewModel: RandomlyViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val settings by viewModel.settings.collectAsState()
    val isFavorite = viewModel.isToolFavorite(ToolType.CARD.id)

    var cardCount by remember { mutableIntStateOf(1) }
    var allowDuplicates by remember { mutableStateOf(false) }

    var drawnCards by remember { mutableStateOf<List<PlayingCard>>(emptyList()) }
    var isDealing by remember { mutableStateOf(false) }
    val scope = androidx.compose.runtime.rememberCoroutineScope()

    fun draw() {
        if (isDealing) return
        isDealing = true
        scope.launch {
            HapticFeedbackUtil.performImpact(context, settings.hapticsEnabled)
            val cards = RandomGenerators.drawCards(cardCount, allowDuplicates)
            drawnCards = cards

            if (settings.animationsEnabled) {
                // Haptic feedback as cards flip
                for (i in cards.indices) {
                    kotlinx.coroutines.delay(120L)
                    HapticFeedbackUtil.performImpact(context, settings.hapticsEnabled)
                }
                kotlinx.coroutines.delay(350L)
            }

            isDealing = false
            HapticFeedbackUtil.performSuccess(context, settings.hapticsEnabled)

            val resultDisplay = cards.joinToString(", ") { "${it.rank.symbol}${it.suit.symbol}" }
            val details = cards.joinToString(", ") { "${it.rank.displayName} of ${it.suit.displayName}" }

            viewModel.recordResult(
                toolType = ToolType.CARD,
                title = "Playing Card (${cards.size} card${if (cards.size > 1) "s" else ""})",
                result = resultDisplay,
                details = details
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Playing Cards", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.toggleFavorite(ToolType.CARD) }) {
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Drawn Cards Display
            if (drawnCards.isNotEmpty()) {
                item {
                    FlowRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalArrangement = Arrangement.Center
                    ) {
                        drawnCards.forEachIndexed { index, card ->
                            PlayingCardView(
                                card = card,
                                dealIndex = index,
                                animateDeal = settings.animationsEnabled,
                                modifier = Modifier.padding(6.dp)
                            )
                        }
                    }
                }

                item {
                    val resultDisplay = drawnCards.joinToString(", ") { "${it.rank.symbol}${it.suit.symbol}" }
                    val details = drawnCards.joinToString(" • ") { "${it.rank.displayName} of ${it.suit.displayName}" }
                    ResultDisplayCard(
                        resultText = resultDisplay,
                        detailsText = details,
                        accentColor = ToolType.CARD.accentColor,
                        onCopy = {
                            ShareUtil.copyToClipboard(context, "$resultDisplay ($details)")
                            viewModel.showMessage("Copied card result to clipboard")
                        },
                        onShare = {
                            ShareUtil.shareText(context, "Drawn Cards", "$resultDisplay\n$details")
                        },
                        onRegenerate = { draw() }
                    )
                }
            } else {
                item {
                    ResultDisplayCard(
                        resultText = "🂠",
                        detailsText = "Tap Draw Cards to deal $cardCount card${if (cardCount > 1) "s" else ""}",
                        accentColor = ToolType.CARD.accentColor,
                        onCopy = {},
                        onShare = {},
                        onRegenerate = { draw() }
                    )
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
                        Text("Number of Cards", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(1, 2, 3, 5).forEach { count ->
                                FilterChip(
                                    selected = cardCount == count,
                                    onClick = {
                                        cardCount = count
                                    },
                                    label = { Text("$count Card${if (count > 1) "s" else ""}") }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Allow Replacement (Duplicates)", style = MaterialTheme.typography.bodyMedium)
                                Text("If disabled, cards are drawn without replacement", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Switch(
                                checked = allowDuplicates,
                                onCheckedChange = { allowDuplicates = it }
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = { draw() },
                            enabled = !isDealing,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("draw_card_button"),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(Icons.Default.FilterNone, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (isDealing) "Dealing Cards..." else "Draw Cards", style = MaterialTheme.typography.titleMedium)
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}
