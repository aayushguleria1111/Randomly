package com.example.ui.screens.tools

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ToolType
import com.example.ui.theme.AmberAccent
import com.example.ui.viewmodel.RandomlyViewModel
import com.example.util.HapticFeedbackUtil
import com.example.util.RandomGenerators
import com.example.util.RandomGenerators.ColorMode
import com.example.util.ShareUtil

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ColorScreen(
    viewModel: RandomlyViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val settings by viewModel.settings.collectAsState()
    val isFavorite = viewModel.isToolFavorite(ToolType.COLOR.id)

    var selectedMode by remember { mutableStateOf(ColorMode.ALL) }
    var colorResult by remember { mutableStateOf(RandomGenerators.generateColor(ColorMode.ALL)) }
    var palette by remember { mutableStateOf<List<RandomGenerators.ColorResult>>(emptyList()) }

    fun generate() {
        val newColor = RandomGenerators.generateColor(selectedMode)
        colorResult = newColor
        HapticFeedbackUtil.performSuccess(context, settings.hapticsEnabled)
        palette = (listOf(newColor) + palette).take(6)
        viewModel.recordResult(
            toolType = ToolType.COLOR,
            title = "Random Color",
            result = newColor.hex,
            details = "${newColor.rgbText} • ${selectedMode.name.lowercase().capitalize()}"
        )
    }

    LaunchedEffect(Unit) {
        if (palette.isEmpty()) {
            palette = listOf(colorResult)
        }
    }

    val animatedColor by animateColorAsState(
        targetValue = colorResult.color,
        animationSpec = tween(durationMillis = 400),
        label = "colorTransition"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Random Color", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.toggleFavorite(ToolType.COLOR) }) {
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
            // Large Color Card Preview
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                        .shadow(8.dp, RoundedCornerShape(24.dp))
                        .testTag("color_preview_card"),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = animatedColor)
                ) {
                    val textColor = if (colorResult.isDark) Color.White else Color(0xFF0F172A)
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = selectedMode.name,
                                style = MaterialTheme.typography.labelMedium,
                                color = textColor.copy(alpha = 0.8f),
                                fontWeight = FontWeight.Bold
                            )
                            Row {
                                IconButton(onClick = {
                                    ShareUtil.copyToClipboard(context, colorResult.hex)
                                    viewModel.showMessage("Copied ${colorResult.hex} to clipboard")
                                }) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy HEX", tint = textColor)
                                }
                                IconButton(onClick = {
                                    ShareUtil.shareText(context, "Random Color", "${colorResult.hex}\n${colorResult.rgbText}\n${colorResult.hslText}")
                                }) {
                                    Icon(Icons.Default.Share, contentDescription = "Share Color", tint = textColor)
                                }
                            }
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = colorResult.hex,
                                fontSize = 36.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace,
                                color = textColor
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = colorResult.rgbText,
                                style = MaterialTheme.typography.bodyMedium,
                                color = textColor.copy(alpha = 0.9f)
                            )
                            Text(
                                text = colorResult.hslText,
                                style = MaterialTheme.typography.bodySmall,
                                color = textColor.copy(alpha = 0.8f)
                            )
                        }

                        Text(
                            text = "Sample contrast text preview",
                            style = MaterialTheme.typography.bodySmall,
                            color = textColor.copy(alpha = 0.7f),
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                    }
                }
            }

            // Mode Selector
            item {
                Text("Color Mode", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(ColorMode.entries) { mode ->
                        FilterChip(
                            selected = selectedMode == mode,
                            onClick = {
                                selectedMode = mode
                                generate()
                            },
                            label = { Text(mode.name.lowercase().replaceFirstChar { it.uppercase() }) }
                        )
                    }
                }
            }

            // Generate Button
            item {
                Button(
                    onClick = { generate() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("generate_color_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ToolType.COLOR.accentColor)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Generate Color", style = MaterialTheme.typography.titleMedium)
                }
            }

            // Palette / History of generated colors
            if (palette.isNotEmpty()) {
                item {
                    Text("Color Palette History", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        palette.forEach { palColor ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(56.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(palColor.color)
                                    .clickable {
                                        colorResult = palColor
                                        ShareUtil.copyToClipboard(context, palColor.hex)
                                        viewModel.showMessage("Copied ${palColor.hex}")
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                if (palColor.hex == colorResult.hex) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(if (palColor.isDark) Color.White else Color.Black)
                                    )
                                }
                            }
                        }
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}
