package com.example.ui.screens.tools

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.ToolType
import com.example.ui.components.ResultDisplayCard
import com.example.ui.theme.AmberAccent
import com.example.ui.viewmodel.RandomlyViewModel
import com.example.util.AudioHapticFeedback
import com.example.util.HapticFeedbackUtil
import com.example.util.ShareUtil
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.security.SecureRandom
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

private val PLAYER_COLORS = listOf(
    Color(0xFFEF4444), // Red
    Color(0xFF3B82F6), // Blue
    Color(0xFF10B981), // Emerald
    Color(0xFFF59E0B), // Amber
    Color(0xFF8B5CF6), // Purple
    Color(0xFFEC4899), // Pink
    Color(0xFF06B6D4), // Cyan
    Color(0xFFF97316), // Orange
    Color(0xFF14B8A6), // Teal
    Color(0xFF6366F1), // Indigo
    Color(0xFF84CC16), // Lime
    Color(0xFFA855F7)  // Violet
)

private val TRUTH_QUESTIONS = listOf(
    "What is your biggest guilty pleasure?",
    "What is the most embarrassing thing you've done?",
    "If you could trade lives with anyone for a day, who?",
    "What is a secret talent nobody here knows about?",
    "What is the weirdest habit you have?",
    "What's the funniest rumor you've ever heard about yourself?"
)

private val DARE_CHALLENGES = listOf(
    "Do your best impression of a famous celebrity.",
    "Speak in a dramatic accent for the next 2 rounds.",
    "Show the last photo saved in your phone's camera roll.",
    "Do 10 jumping jacks while chanting your name.",
    "Sing the chorus of your favorite song right now.",
    "Let someone in the group style your hair for 5 minutes."
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpinBottleScreen(
    viewModel: RandomlyViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val settings by viewModel.settings.collectAsState()
    val isFavorite = viewModel.isToolFavorite(ToolType.SPIN_BOTTLE.id)

    val scope = rememberCoroutineScope()
    val random = remember { SecureRandom() }

    // Players state
    val defaultPlayers = remember { listOf("Player 1", "Player 2", "Player 3", "Player 4") }
    val players = remember { mutableStateListOf<String>().apply { addAll(defaultPlayers) } }

    var isPartyMode by remember { mutableStateOf(false) } // Truth or Dare prompt
    var selectedPlayerName by remember { mutableStateOf<String?>(null) }
    var selectedPlayerIndex by remember { mutableIntStateOf(-1) }
    var partyPrompt by remember { mutableStateOf<String?>(null) }
    var showEditPlayersDialog by remember { mutableStateOf(false) }

    // Animation & physics state
    var bottleAngle by remember { mutableFloatStateOf(0f) }
    var isSpinning by remember { mutableStateOf(false) }
    var spinJob by remember { mutableStateOf<Job?>(null) }

    // Calculated pointed player from normalized angle
    val normalizedAngle = ((bottleAngle % 360f) + 360f) % 360f
    val livePointedIndex = if (players.isNotEmpty()) {
        val slice = 360f / players.size
        (((normalizedAngle + slice / 2f) % 360f) / slice).toInt() % players.size
    } else -1

    fun spinBottle(initialVelocity: Float = 0f) {
        if (isSpinning) return
        isSpinning = true
        selectedPlayerName = null
        selectedPlayerIndex = -1
        partyPrompt = null

        AudioHapticFeedback.onToolUse(context, settings.soundEffectsEnabled, settings.hapticsEnabled)

        spinJob?.cancel()
        spinJob = scope.launch {
            // Determine spin duration and final angle
            // 4 to 8 full spins + random stopping angle
            val fullSpins = random.nextInt(5) + 5
            val randomOffset = random.nextFloat() * 360f
            val targetRotation = bottleAngle + (fullSpins * 360f) + randomOffset + (initialVelocity.coerceIn(-720f, 720f))

            val anim = Animatable(bottleAngle)
            val durationMs = 2800 + random.nextInt(600)

            // Dynamic ticking as it sweeps past items
            var lastTickAngle = bottleAngle
            val tickThreshold = 360f / (players.size.coerceAtLeast(4) * 2f)

            launch {
                while (anim.isRunning) {
                    val currentVal = anim.value
                    if (kotlin.math.abs(currentVal - lastTickAngle) >= tickThreshold) {
                        lastTickAngle = currentVal
                        HapticFeedbackUtil.performImpact(context, settings.hapticsEnabled)
                    }
                    delay(16)
                }
            }

            anim.animateTo(
                targetValue = targetRotation,
                animationSpec = tween(
                    durationMillis = durationMs,
                    easing = LinearOutSlowInEasing
                )
            ) {
                bottleAngle = value
            }

            bottleAngle = anim.value % 360f
            isSpinning = false
            HapticFeedbackUtil.performSuccess(context, settings.hapticsEnabled)

            // Determine final winner
            val slice = 360f / players.size
            val winnerIdx = (((((bottleAngle % 360f) + 360f) % 360f) + slice / 2f) % 360f / slice).toInt() % players.size
            selectedPlayerIndex = winnerIdx
            val winnerName = players[winnerIdx]
            selectedPlayerName = winnerName

            val degreesStr = "${bottleAngle.roundToInt()}°"
            var details = "Bottle pointed at $degreesStr"

            if (isPartyMode) {
                val isDare = random.nextBoolean()
                val promptList = if (isDare) DARE_CHALLENGES else TRUTH_QUESTIONS
                val prompt = promptList[random.nextInt(promptList.size)]
                partyPrompt = "${if (isDare) "DARE" else "TRUTH"}: $prompt"
                details = "$winnerName • $partyPrompt"
            }

            viewModel.recordResult(
                toolType = ToolType.SPIN_BOTTLE,
                title = "Spin the Bottle",
                result = "$winnerName ($degreesStr)",
                details = details
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Spin the Bottle", style = MaterialTheme.typography.titleLarge)
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFFEF4444),
                            contentColor = Color.White
                        ) {
                            Text(
                                text = "NEW",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 9.sp,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.5.dp)
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.toggleFavorite(ToolType.SPIN_BOTTLE) }) {
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
                .padding(horizontal = 14.dp),
            contentPadding = PaddingValues(vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Selected Result Banner if bottle settled
            selectedPlayerName?.let { winner ->
                item {
                    ResultDisplayCard(
                        resultText = "Points to: $winner",
                        detailsText = partyPrompt ?: "Bottle stopped at ${bottleAngle.roundToInt()}°",
                        accentColor = ToolType.SPIN_BOTTLE.accentColor,
                        onCopy = {
                            val text = "Spin the bottle pointed to: $winner! ${partyPrompt ?: ""}"
                            ShareUtil.copyToClipboard(context, text)
                            viewModel.showMessage("Copied result to clipboard")
                        },
                        onShare = {
                            val text = "Spin the bottle pointed to: $winner! ${partyPrompt ?: ""}"
                            ShareUtil.shareText(context, "Spin the Bottle Result", text)
                        },
                        onRegenerate = { spinBottle() }
                    )
                }
            }

            // Interactive Bottle Arena
            item {
                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 500.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val density = LocalDensity.current
                    val arenaSizeDp = maxWidth.coerceAtMost(360.dp)
                    val arenaSizePx = with(density) { arenaSizeDp.toPx() }
                    val centerPx = Offset(arenaSizePx / 2f, arenaSizePx / 2f)
                    val radiusDp = (arenaSizeDp / 2) - 34.dp

                    Box(
                        modifier = Modifier
                            .size(arenaSizeDp)
                            .shadow(6.dp, CircleShape)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
                                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.15f)
                                    )
                                )
                            )
                            .pointerInput(isSpinning) {
                                if (!isSpinning) {
                                    var lastTouchAngle = 0f
                                    var initialSwipeAngle = 0f
                                    detectDragGestures(
                                        onDragStart = { offset ->
                                            val dx = offset.x - centerPx.x
                                            val dy = offset.y - centerPx.y
                                            lastTouchAngle = (atan2(dy, dx) * (180f / PI.toFloat()))
                                            initialSwipeAngle = lastTouchAngle
                                        },
                                        onDrag = { change, _ ->
                                            change.consume()
                                            val dx = change.position.x - centerPx.x
                                            val dy = change.position.y - centerPx.y
                                            val currentTouchAngle = (atan2(dy, dx) * (180f / PI.toFloat()))
                                            val diff = currentTouchAngle - lastTouchAngle
                                            bottleAngle += diff
                                            lastTouchAngle = currentTouchAngle
                                        },
                                        onDragEnd = {
                                            val totalDelta = lastTouchAngle - initialSwipeAngle
                                            val velocity = totalDelta * 12f
                                            spinBottle(velocity)
                                        }
                                    )
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        // Outer Compass Direction Markings
                        listOf(0 to "N", 90 to "E", 180 to "S", 270 to "W").forEach { (deg, label) ->
                            val rad = (deg - 90) * (PI / 180.0)
                            val rPx = with(density) { (arenaSizeDp / 2 - 12.dp).toPx() }
                            val xOffset = (rPx * cos(rad)).roundToInt()
                            val yOffset = (rPx * sin(rad)).roundToInt()

                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                modifier = Modifier.offset { IntOffset(xOffset, yOffset) }
                            )
                        }

                        // Players placed radially around the circle
                        val count = players.size
                        players.forEachIndexed { index, player ->
                            val angleDeg = (index.toFloat() / count) * 360f
                            // 0 deg points North (up): rad = (angleDeg - 90) * PI / 180
                            val rad = (angleDeg - 90f) * (PI / 180.0)
                            val rPx = with(density) { radiusDp.toPx() }
                            val xOffset = (rPx * cos(rad)).roundToInt()
                            val yOffset = (rPx * sin(rad)).roundToInt()

                            val isTargeted = if (isSpinning) {
                                index == livePointedIndex
                            } else {
                                index == selectedPlayerIndex
                            }

                            val playerColor = PLAYER_COLORS[index % PLAYER_COLORS.size]

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isTargeted) playerColor else playerColor.copy(alpha = 0.15f),
                                contentColor = if (isTargeted) Color.White else playerColor,
                                border = BorderStroke(
                                    if (isTargeted) 2.dp else 1.dp,
                                    if (isTargeted) Color.White else playerColor.copy(alpha = 0.5f)
                                ),
                                shadowElevation = if (isTargeted) 6.dp else 1.dp,
                                modifier = Modifier
                                    .offset { IntOffset(xOffset, yOffset) }
                            ) {
                                Text(
                                    text = player,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (isTargeted) FontWeight.ExtraBold else FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        // Center Circle Hub / Table Ring
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                            border = BorderStroke(2.dp, ToolType.SPIN_BOTTLE.accentColor.copy(alpha = 0.3f)),
                            modifier = Modifier.size(100.dp)
                        ) {}

                        // Spinning 3D Bottle
                        Image(
                            painter = painterResource(R.drawable.ic_bottle_3d),
                            contentDescription = "Spinning bottle",
                            modifier = Modifier
                                .size(width = 68.dp, height = 140.dp)
                                .rotate(bottleAngle)
                                .testTag("spinning_bottle_image")
                        )

                        // Center Pivot Pin
                        Surface(
                            shape = CircleShape,
                            color = ToolType.SPIN_BOTTLE.accentColor,
                            border = BorderStroke(2.dp, Color.White),
                            shadowElevation = 3.dp,
                            modifier = Modifier.size(14.dp)
                        ) {}
                    }
                }
            }

            // Spin Action Button & Prompt
            item {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 500.dp)
                ) {
                    Button(
                        onClick = { spinBottle() },
                        enabled = !isSpinning && players.isNotEmpty(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 50.dp, max = 54.dp)
                            .testTag("spin_bottle_button"),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ToolType.SPIN_BOTTLE.accentColor
                        )
                    ) {
                        Icon(
                            if (isSpinning) Icons.Default.Refresh else Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isSpinning) "Spinning..." else "Spin Bottle",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Tip: You can also swipe the bottle directly to spin with touch physics!",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Compact Configuration Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 500.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Game Options",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )

                            TextButton(
                                onClick = { showEditPlayersDialog = true },
                                contentPadding = PaddingValues(horizontal = 6.dp)
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Edit Names", style = MaterialTheme.typography.labelSmall)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Players Stepper & Mode Switch
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Player count stepper
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    "Players:",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                IconButton(
                                    onClick = {
                                        if (players.size > 2) {
                                            players.removeAt(players.lastIndex)
                                        }
                                    },
                                    enabled = players.size > 2 && !isSpinning,
                                    modifier = Modifier.size(30.dp)
                                ) {
                                    Icon(Icons.Default.Remove, contentDescription = "Remove player", modifier = Modifier.size(14.dp))
                                }
                                Text(
                                    text = "${players.size}",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 4.dp)
                                )
                                IconButton(
                                    onClick = {
                                        if (players.size < 12) {
                                            players.add("Player ${players.size + 1}")
                                        }
                                    },
                                    enabled = players.size < 12 && !isSpinning,
                                    modifier = Modifier.size(30.dp)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = "Add player", modifier = Modifier.size(14.dp))
                                }
                            }

                            // Truth or Dare Party Mode Chip
                            FilterChip(
                                selected = isPartyMode,
                                onClick = { isPartyMode = !isPartyMode },
                                label = { Text("Truth or Dare", style = MaterialTheme.typography.labelSmall) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = ToolType.SPIN_BOTTLE.accentColor.copy(alpha = 0.2f),
                                    selectedLabelColor = ToolType.SPIN_BOTTLE.accentColor
                                )
                            )
                        }
                    }
                }
            }
        }
    }

    // Edit Player Names Dialog
    if (showEditPlayersDialog) {
        val tempNames = remember { mutableStateListOf<String>().apply { addAll(players) } }

        AlertDialog(
            onDismissRequest = { showEditPlayersDialog = false },
            title = { Text("Customize Player Names") },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 380.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    tempNames.indices.forEach { idx ->
                        OutlinedTextField(
                            value = tempNames[idx],
                            onValueChange = { if (it.length <= 16) tempNames[idx] = it },
                            label = { Text("Player ${idx + 1}") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        for (i in tempNames.indices) {
                            val trimmed = tempNames[i].trim()
                            players[i] = if (trimmed.isNotBlank()) trimmed else "Player ${i + 1}"
                        }
                        showEditPlayersDialog = false
                        viewModel.showMessage("Player names updated")
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditPlayersDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
