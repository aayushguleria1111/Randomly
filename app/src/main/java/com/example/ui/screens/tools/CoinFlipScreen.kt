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
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
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
import com.example.ui.components.AnimatedCoinView
import com.example.ui.components.ResultDisplayCard
import com.example.ui.theme.AmberAccent
import com.example.ui.viewmodel.RandomlyViewModel
import com.example.util.HapticFeedbackUtil
import com.example.util.RandomGenerators
import com.example.util.RandomGenerators.CoinSide
import com.example.util.ShareUtil
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoinFlipScreen(
    viewModel: RandomlyViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val settings by viewModel.settings.collectAsState()
    val isFavorite = viewModel.isToolFavorite(ToolType.COIN_FLIP.id)

    var coinCount by remember { mutableIntStateOf(1) }
    var currentResult by remember { mutableStateOf(CoinSide.HEADS) }
    var multipleResults by remember { mutableStateOf<List<CoinSide>>(listOf(CoinSide.HEADS)) }
    var isFlipping by remember { mutableStateOf(false) }

    var headsTotal by remember { mutableIntStateOf(0) }
    var tailsTotal by remember { mutableIntStateOf(0) }

    fun flip() {
        if (isFlipping) return
        isFlipping = true

        scope.launch {
            HapticFeedbackUtil.performImpact(context, settings.hapticsEnabled)
            val results = RandomGenerators.flipCoins(coinCount)
            val single = results.first()

            if (settings.animationsEnabled) {
                delay(700)
            }

            currentResult = single
            multipleResults = results
            isFlipping = false

            val headsInFlip = results.count { it == CoinSide.HEADS }
            val tailsInFlip = results.count { it == CoinSide.TAILS }
            headsTotal += headsInFlip
            tailsTotal += tailsInFlip

            HapticFeedbackUtil.performSuccess(context, settings.hapticsEnabled)

            val displayResult = if (coinCount == 1) single.label else "$headsInFlip Heads, $tailsInFlip Tails"
            val details = if (coinCount > 1) results.joinToString(", ") { it.label } else "Single Flip"

            viewModel.recordResult(
                toolType = ToolType.COIN_FLIP,
                title = "Coin Flip ($coinCount coin${if (coinCount > 1) "s" else ""})",
                result = displayResult,
                details = details
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Coin Flip", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.toggleFavorite(ToolType.COIN_FLIP) }) {
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
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Coin 3D view
            item {
                Spacer(modifier = Modifier.height(16.dp))
                AnimatedCoinView(
                    result = currentResult,
                    isFlipping = isFlipping,
                    modifier = Modifier.size(180.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Results Card
            item {
                val resultText = if (coinCount == 1) currentResult.label else "${multipleResults.count { it == CoinSide.HEADS }} Heads, ${multipleResults.count { it == CoinSide.TAILS }} Tails"
                val detailsText = if (coinCount > 1) multipleResults.joinToString(" • ") { it.label } else "Streak Stats: $headsTotal Heads vs $tailsTotal Tails"
                ResultDisplayCard(
                    resultText = resultText,
                    detailsText = detailsText,
                    accentColor = ToolType.COIN_FLIP.accentColor,
                    onCopy = {
                        ShareUtil.copyToClipboard(context, resultText)
                        viewModel.showMessage("Copied coin result: $resultText")
                    },
                    onShare = {
                        ShareUtil.shareText(context, "Coin Flip Result", "$resultText\n$detailsText")
                    },
                    onRegenerate = { flip() }
                )
            }

            // Coin Settings Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Number of Coins", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(1, 2, 3, 5, 10).forEach { count ->
                                FilterChip(
                                    selected = coinCount == count,
                                    onClick = {
                                        coinCount = count
                                        flip()
                                    },
                                    label = { Text("$count Coin${if (count > 1) "s" else ""}") }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = { flip() },
                            enabled = !isFlipping,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("flip_coin_button"),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = ToolType.COIN_FLIP.accentColor)
                        ) {
                            Icon(Icons.Default.MonetizationOn, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (isFlipping) "Flipping..." else "FLIP COIN", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}
