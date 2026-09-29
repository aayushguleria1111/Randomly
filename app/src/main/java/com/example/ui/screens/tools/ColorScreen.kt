package com.example.ui.screens.tools

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.Colorize
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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import com.example.data.model.ToolType
import com.example.ui.theme.AmberAccent

import androidx.compose.material3.Surface
import com.example.ui.viewmodel.RandomlyViewModel
import com.example.util.HapticFeedbackUtil
import com.example.util.ShareUtil
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.security.SecureRandom

enum class ColorFocusMode(
    val title: String,
    val description: String,
    val dotColor: Color
) {
    FULL_SPECTRUM("Full Spectrum", "Any color across 16.7M RGB gamut", Color(0xFF6366F1)),
    WARM_FOCUS("Warm Focus", "Weighted towards Reds, Oranges & Warm Ambers", Color(0xFFEA580C)),
    COOL_FOCUS("Cool Focus", "Weighted towards Blues, Teals & Cyans", Color(0xFF0284C7)),
    NATURE_FOCUS("Nature Focus", "Weighted towards Greens, Emeralds & Olive", Color(0xFF059669)),
    PASTEL_FOCUS("Pastel Focus", "Soft, gentle pastel tints with high brightness", Color(0xFFF472B6)),
    NEON_FOCUS("Neon / Vibrant", "Max saturation and intense electric luminance", Color(0xFF10B981)),
    DEEP_FOCUS("Deep / Moody", "Rich, dark, deep tones with bold presence", Color(0xFF4338CA))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ColorScreen(
    viewModel: RandomlyViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val settings by viewModel.settings.collectAsState()
    val isFavorite = viewModel.isToolFavorite(ToolType.COLOR.id)

    var selectedFocus by remember { mutableStateOf(ColorFocusMode.FULL_SPECTRUM) }
    var currentColor by remember { mutableStateOf(Color(0xFF6366F1)) }
    var isBlending by remember { mutableStateOf(false) }
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    val colorScale = remember { androidx.compose.animation.core.Animatable(1f) }
    val colorRotation = remember { androidx.compose.animation.core.Animatable(0f) }

    fun sampleColorWithFocus(focus: ColorFocusMode, random: SecureRandom): Color {
        val hsv = FloatArray(3)
        when (focus) {
            ColorFocusMode.FULL_SPECTRUM -> {
                hsv[0] = random.nextFloat() * 360f
                hsv[1] = 0.45f + random.nextFloat() * 0.55f
                hsv[2] = 0.45f + random.nextFloat() * 0.55f
            }
            ColorFocusMode.WARM_FOCUS -> {
                hsv[0] = if (random.nextBoolean()) random.nextFloat() * 50f else 340f + random.nextFloat() * 20f
                hsv[1] = 0.65f + random.nextFloat() * 0.35f
                hsv[2] = 0.70f + random.nextFloat() * 0.30f
            }
            ColorFocusMode.COOL_FOCUS -> {
                hsv[0] = 175f + random.nextFloat() * 100f
                hsv[1] = 0.60f + random.nextFloat() * 0.40f
                hsv[2] = 0.65f + random.nextFloat() * 0.35f
            }
            ColorFocusMode.NATURE_FOCUS -> {
                hsv[0] = 75f + random.nextFloat() * 90f
                hsv[1] = 0.55f + random.nextFloat() * 0.45f
                hsv[2] = 0.55f + random.nextFloat() * 0.40f
            }
            ColorFocusMode.PASTEL_FOCUS -> {
                hsv[0] = random.nextFloat() * 360f
                hsv[1] = 0.20f + random.nextFloat() * 0.25f
                hsv[2] = 0.88f + random.nextFloat() * 0.12f
            }
            ColorFocusMode.NEON_FOCUS -> {
                hsv[0] = random.nextFloat() * 360f
                hsv[1] = 0.90f + random.nextFloat() * 0.10f
                hsv[2] = 0.90f + random.nextFloat() * 0.10f
            }
            ColorFocusMode.DEEP_FOCUS -> {
                hsv[0] = random.nextFloat() * 360f
                hsv[1] = 0.60f + random.nextFloat() * 0.40f
                hsv[2] = 0.25f + random.nextFloat() * 0.30f
            }
        }
        val argb = android.graphics.Color.HSVToColor(hsv)
        return Color(argb)
    }

    fun generateColor() {
        if (isBlending) return
        isBlending = true
        scope.launch {
            val random = SecureRandom()
            if (settings.animationsEnabled) {
                val cycleJob = launch {
                    for (i in 1..6) {
                        currentColor = sampleColorWithFocus(selectedFocus, random)
                        HapticFeedbackUtil.performImpact(context, settings.hapticsEnabled)
                        kotlinx.coroutines.delay(60)
                    }
                }
                val scaleJob = launch {
                    colorScale.animateTo(0.92f, androidx.compose.animation.core.tween(150))
                }
                cycleJob.join()
                scaleJob.join()
            }

            val color = sampleColorWithFocus(selectedFocus, random)
            currentColor = color
            isBlending = false
            HapticFeedbackUtil.performSuccess(context, settings.hapticsEnabled)

            if (settings.animationsEnabled) {
                colorScale.animateTo(
                    targetValue = 1f,
                    animationSpec = androidx.compose.animation.core.spring(
                        dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy,
                        stiffness = androidx.compose.animation.core.Spring.StiffnessLow
                    )
                )
            }

            val r = (color.red * 255).toInt()
            val g = (color.green * 255).toInt()
            val b = (color.blue * 255).toInt()
            val hex = String.format("#%02X%02X%02X", r, g, b)
            val focusSuffix = if (selectedFocus == ColorFocusMode.FULL_SPECTRUM) "" else " • Focus: ${selectedFocus.title}"
            viewModel.recordResult(
                toolType = ToolType.COLOR,
                title = "Random Color",
                result = hex,
                details = "RGB: ($r, $g, $b)$focusSuffix"
            )
        }
    }

    val hexString = String.format(
        "#%02X%02X%02X",
        (currentColor.red * 255).toInt(),
        (currentColor.green * 255).toInt(),
        (currentColor.blue * 255).toInt()
    )
    val rgbString = "RGB(${ (currentColor.red * 255).toInt() }, ${ (currentColor.green * 255).toInt() }, ${ (currentColor.blue * 255).toInt() })"

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
                .padding(horizontal = 12.dp),
            contentPadding = PaddingValues(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Large Color Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                        .graphicsLayer {
                            scaleX = colorScale.value
                            scaleY = colorScale.value
                        },
                    shape = RoundedCornerShape(24.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(currentColor)
                            .padding(20.dp),
                        contentAlignment = Alignment.BottomStart
                    ) {
                        Column(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.Black.copy(alpha = 0.5f))
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = hexString,
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                                if (selectedFocus != ColorFocusMode.FULL_SPECTRUM) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = Color.White.copy(alpha = 0.25f)
                                    ) {
                                        Text(
                                            text = selectedFocus.title,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                            Text(
                                text = rgbString,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }

            // Generate Button
            item {
                Button(
                    onClick = { generateColor() },
                    enabled = !isBlending,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("generate_color_button"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = currentColor)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(20.dp), tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        if (isBlending) "Mixing Color..." else "Generate New Color",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            // Color Focus & Weighting Card
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
                            Text(
                                text = "Color Focus & Weighting",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            if (selectedFocus != ColorFocusMode.FULL_SPECTRUM) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = selectedFocus.dotColor.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = selectedFocus.title,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = selectedFocus.dotColor,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = selectedFocus.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(ColorFocusMode.entries.toTypedArray()) { focus ->
                                FilterChip(
                                    selected = selectedFocus == focus,
                                    onClick = { selectedFocus = focus },
                                    label = { Text(focus.title) },
                                    leadingIcon = {
                                        Box(
                                            modifier = Modifier
                                                .size(12.dp)
                                                .clip(CircleShape)
                                                .background(focus.dotColor)
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Details & Quick Copy Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Color Formats", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                        Spacer(modifier = Modifier.height(12.dp))

                        // HEX
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("HEX Code", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(hexString, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            }
                            IconButton(onClick = {
                                ShareUtil.copyToClipboard(context, hexString)
                                viewModel.showMessage("Copied '$hexString' to clipboard")
                            }) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Copy HEX")
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // RGB
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("RGB Values", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(rgbString, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            }
                            IconButton(onClick = {
                                ShareUtil.copyToClipboard(context, rgbString)
                                viewModel.showMessage("Copied '$rgbString' to clipboard")
                            }) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Copy RGB")
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedButton(
                            onClick = {
                                ShareUtil.shareText(context, "Random Color", "$hexString\n$rgbString")
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Share Color")
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}
