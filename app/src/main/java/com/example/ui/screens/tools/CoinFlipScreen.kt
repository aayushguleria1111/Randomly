package com.example.ui.screens.tools

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shield
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ToolType
import com.example.ui.components.ResultDisplayCard
import com.example.ui.theme.AmberAccent
import com.example.ui.viewmodel.RandomlyViewModel
import com.example.util.HapticFeedbackUtil
import com.example.util.ShareUtil
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import java.security.SecureRandom

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

    var resultText by remember { mutableStateOf("Heads") }
    var hasFlipped by remember { mutableStateOf(false) }
    var isFlipping by remember { mutableStateOf(false) }

    // Physical Toss state variables
    val tossOffsetY = remember { Animatable(0f) }
    val flipRotationX = remember { Animatable(0f) }
    val tumbleY = remember { Animatable(0f) }
    val wobbleZ = remember { Animatable(0f) }

    // Session tallies
    var headsCount by remember { mutableIntStateOf(0) }
    var tailsCount by remember { mutableIntStateOf(0) }

    fun flipCoin() {
        if (isFlipping) return
        isFlipping = true
        HapticFeedbackUtil.performImpact(context, settings.hapticsEnabled)

        scope.launch {
            val random = SecureRandom()
            val outcome = if (random.nextBoolean()) "Heads" else "Tails"

            if (settings.animationsEnabled) {
                // Determine rotation delta so coin tumbles forward multiple times
                // and lands cleanly on Heads (0 mod 360) or Tails (180 mod 360)
                val currentRot = flipRotationX.value
                val currentAngleMod360 = (currentRot % 360f + 360f) % 360f
                val desiredRemainder = if (outcome == "Heads") 0f else 180f
                var diff = desiredRemainder - currentAngleMod360
                while (diff < 0f) {
                    diff += 360f
                }
                // 7 complete end-over-end flips before reaching target face
                val targetRot = currentRot + (7 * 360f) + diff

                // Run flight physics in parallel:
                coroutineScope {
                    // 1. Authentic Vertical parabolic flight arc (Upward deceleration, downward gravity acceleration)
                    val flightJob = async {
                        // High launch into the air (peak apex at -190dp) decelerating against gravity
                        tossOffsetY.animateTo(
                            targetValue = -190f,
                            animationSpec = tween(durationMillis = 480, easing = LinearOutSlowInEasing)
                        )
                        // Downward gravitational acceleration towards the surface
                        tossOffsetY.animateTo(
                            targetValue = 0f,
                            animationSpec = tween(durationMillis = 440, easing = FastOutLinearInEasing)
                        )
                        HapticFeedbackUtil.performImpact(context, settings.hapticsEnabled)
                        // First clatter bounce
                        tossOffsetY.animateTo(
                            targetValue = -28f,
                            animationSpec = tween(durationMillis = 110, easing = LinearOutSlowInEasing)
                        )
                        tossOffsetY.animateTo(
                            targetValue = 0f,
                            animationSpec = tween(durationMillis = 100, easing = FastOutLinearInEasing)
                        )
                        // Second subtle settle micro-bounce
                        tossOffsetY.animateTo(
                            targetValue = -7f,
                            animationSpec = tween(durationMillis = 60, easing = LinearOutSlowInEasing)
                        )
                        tossOffsetY.animateTo(
                            targetValue = 0f,
                            animationSpec = tween(durationMillis = 50, easing = FastOutLinearInEasing)
                        )
                    }

                    // 2. End-over-end 3D tumbling rotation with natural deceleration
                    val rotationJob = async {
                        flipRotationX.animateTo(
                            targetValue = targetRot,
                            animationSpec = tween(
                                durationMillis = 1240,
                                easing = CubicBezierEasing(0.20f, 0.0f, 0.15f, 1.0f)
                            )
                        )
                    }

                    // 3. Natural coin lateral tumble on Y axis
                    val tumbleJob = async {
                        tumbleY.animateTo(16f, tween(260))
                        tumbleY.animateTo(-12f, tween(320))
                        tumbleY.animateTo(6f, tween(320))
                        tumbleY.animateTo(0f, tween(340))
                    }

                    // 4. Natural human toss wobble on Z axis
                    val wobbleJob = async {
                        wobbleZ.animateTo(14f, tween(240))
                        wobbleZ.animateTo(-10f, tween(300))
                        wobbleZ.animateTo(5f, tween(320))
                        wobbleZ.animateTo(0f, tween(380))
                    }

                    // 5. In-flight tumbling haptic pulse
                    val hapticPulseJob = async {
                        kotlinx.coroutines.delay(180)
                        HapticFeedbackUtil.performImpact(context, settings.hapticsEnabled)
                        kotlinx.coroutines.delay(220)
                        HapticFeedbackUtil.performImpact(context, settings.hapticsEnabled)
                        kotlinx.coroutines.delay(220)
                        HapticFeedbackUtil.performImpact(context, settings.hapticsEnabled)
                    }

                    flightJob.await()
                    rotationJob.await()
                    tumbleJob.await()
                    wobbleJob.await()
                    hapticPulseJob.await()
                }
            } else {
                // Instant / reduced animation
                val targetRot = if (outcome == "Heads") 0f else 180f
                flipRotationX.snapTo(targetRot)
                tossOffsetY.snapTo(0f)
                tumbleY.snapTo(0f)
                wobbleZ.snapTo(0f)
            }

            resultText = outcome
            hasFlipped = true
            if (outcome == "Heads") headsCount++ else tailsCount++
            isFlipping = false
            HapticFeedbackUtil.performSuccess(context, settings.hapticsEnabled)

            viewModel.recordResult(
                toolType = ToolType.COIN_FLIP,
                title = "Coin Flip",
                result = outcome
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
                .padding(horizontal = 12.dp),
            contentPadding = PaddingValues(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Interactive 3D Coin Toss Stage
            item {
                Spacer(modifier = Modifier.height(8.dp))
                CoinTossStage(
                    tossOffsetY = tossOffsetY.value,
                    rotationX = flipRotationX.value,
                    tumbleY = tumbleY.value,
                    wobbleZ = wobbleZ.value,
                    isFlipping = isFlipping,
                    resultText = resultText,
                    onCoinTossed = { flipCoin() }
                )
            }

            // Session Stats Tally Pill
            item {
                Surface(
                    modifier = Modifier.widthIn(max = 520.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            Text(
                                text = "Heads: $headsCount",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFB45309)
                            )
                            Text(
                                text = "Tails: $tailsCount",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFD97706)
                            )
                            Text(
                                text = "Total: ${headsCount + tailsCount}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        if (headsCount + tailsCount > 0) {
                            IconButton(
                                onClick = {
                                    headsCount = 0
                                    tailsCount = 0
                                },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    Icons.Default.Refresh,
                                    contentDescription = "Reset stats",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Primary Flip Button
            item {
                Button(
                    onClick = { flipCoin() },
                    enabled = !isFlipping,
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 520.dp)
                        .heightIn(min = 48.dp, max = 52.dp)
                        .testTag("flip_coin_button"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ToolType.COIN_FLIP.accentColor)
                ) {
                    Text(
                        text = if (isFlipping) "Tossing Coin..." else "TOSS COIN",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }
            }

            // Result Display Card
            item {
                Box(modifier = Modifier.fillMaxWidth().widthIn(max = 520.dp)) {
                    ResultDisplayCard(
                        resultText = if (hasFlipped) resultText else "Ready",
                        detailsText = if (hasFlipped) "Fair 50/50 probability" else "Tap TOSS COIN to flip",
                        accentColor = ToolType.COIN_FLIP.accentColor,
                        onCopy = {
                            if (hasFlipped) {
                                ShareUtil.copyToClipboard(context, resultText)
                                viewModel.showMessage("Copied '$resultText' to clipboard")
                            }
                        },
                        onShare = {
                            if (hasFlipped) {
                                ShareUtil.shareText(context, "Coin Flip Result", resultText)
                            }
                        },
                        onRegenerate = { flipCoin() }
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}

/**
 * Stage hosting the 3D parabolic coin flight and dynamic contact shadow.
 */
@Composable
private fun CoinTossStage(
    tossOffsetY: Float,
    rotationX: Float,
    tumbleY: Float,
    wobbleZ: Float,
    isFlipping: Boolean,
    resultText: String,
    onCoinTossed: () -> Unit
) {
    // Ground shadow scales and diffuses based on altitude
    val altitudeFraction = (-tossOffsetY / 190f).coerceIn(0f, 1f)
    val shadowWidth = (150 - 95 * altitudeFraction).dp
    val shadowHeight = (24 - 16 * altitudeFraction).dp
    val shadowAlpha = (0.42f - 0.34f * altitudeFraction).coerceAtLeast(0.05f)

    // Perspective foreshortening: coin grows dramatically as it arches toward camera
    val perspectiveScale = 1.0f + (0.35f * altitudeFraction)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 520.dp)
            .height(290.dp)
            .clip(RoundedCornerShape(24.dp)),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0x33F59E0B),
                            Color.Transparent
                        )
                    )
                )
                .pointerInput(isFlipping) {
                    if (isFlipping) return@pointerInput
                    detectVerticalDragGestures { _, dragAmount ->
                        if (dragAmount < -15f) {
                            onCoinTossed()
                        }
                    }
                }
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    enabled = !isFlipping
                ) {
                    onCoinTossed()
                },
            contentAlignment = Alignment.Center
        ) {
            // Hint Label
            Text(
                text = if (isFlipping) "Coin in flight..." else "Tap or swipe up to toss",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 12.dp)
            )

            // Dynamic Surface Contact Shadow
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 28.dp)
                    .width(shadowWidth)
                    .height(shadowHeight)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = shadowAlpha),
                                Color.Transparent
                            )
                        )
                    )
            )

            // Coin Object in 3D Flight
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 36.dp)
                    .graphicsLayer {
                        this.translationY = tossOffsetY
                        this.rotationX = rotationX
                        this.rotationY = tumbleY
                        this.rotationZ = wobbleZ
                        this.scaleX = perspectiveScale
                        this.scaleY = perspectiveScale
                        cameraDistance = 16f * density
                    }
                    .size(176.dp)
                    .shadow(
                        elevation = if (altitudeFraction > 0.1f) 16.dp else 4.dp,
                        shape = CircleShape,
                        clip = false
                    ),
                contentAlignment = Alignment.Center
            ) {
                // Determine which face is facing the camera
                val normalizedAngle = ((rotationX % 360f) + 360f) % 360f
                val isBackFacing = normalizedAngle in 90f..270f

                if (!isBackFacing) {
                    CoinFaceFront()
                } else {
                    // Back face needs 180° counter-rotation around X so artwork and typography are right-side-up
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer { this.rotationX = 180f },
                        contentAlignment = Alignment.Center
                    ) {
                        CoinFaceBack()
                    }
                }
            }
        }
    }
}

/**
 * Heads Face: Radiant golden finish with classical milled rim and Liberty Star emblem.
 */
@Composable
private fun CoinFaceFront() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        Color(0xFFFEF08A),
                        Color(0xFFFACC15),
                        Color(0xFFCA8A04),
                        Color(0xFF854D0E)
                    )
                )
            )
            .border(
                BorderStroke(
                    6.dp,
                    Brush.sweepGradient(
                        listOf(
                            Color(0xFFB45309),
                            Color(0xFFFEF08A),
                            Color(0xFF78350F),
                            Color(0xFFFEF08A),
                            Color(0xFFB45309)
                        )
                    )
                ),
                CircleShape
            )
            .padding(8.dp)
            .border(1.5.dp, Color(0xFFB45309).copy(alpha = 0.5f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "★ LIBERTY ★",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Black,
                color = Color(0xFF78350F),
                letterSpacing = 1.5.sp
            )

            Spacer(modifier = Modifier.height(2.dp))

            // Central Laurel Star Emblem
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFEF08A).copy(alpha = 0.55f))
                    .border(1.5.dp, Color(0xFF92400E), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Star,
                    contentDescription = null,
                    tint = Color(0xFFB45309),
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = "HEADS",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black,
                color = Color(0xFF78350F),
                textAlign = TextAlign.Center
            )

            Text(
                text = "• 2026 •",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF92400E).copy(alpha = 0.8f)
            )
        }
    }
}

/**
 * Tails Face: Burnished copper-amber finish with heraldic shield and motto.
 */
@Composable
private fun CoinFaceBack() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        Color(0xFFFFFBEB),
                        Color(0xFFF59E0B),
                        Color(0xFFD97706),
                        Color(0xFF78350F)
                    )
                )
            )
            .border(
                BorderStroke(
                    6.dp,
                    Brush.sweepGradient(
                        listOf(
                            Color(0xFF92400E),
                            Color(0xFFFDE68A),
                            Color(0xFF78350F),
                            Color(0xFFFDE68A),
                            Color(0xFF92400E)
                        )
                    )
                ),
                CircleShape
            )
            .padding(8.dp)
            .border(1.5.dp, Color(0xFF78350F).copy(alpha = 0.5f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "★ E PLURIBUS UNUM ★",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Black,
                color = Color(0xFF78350F),
                letterSpacing = 0.5.sp
            )

            Spacer(modifier = Modifier.height(2.dp))

            // Central Heraldic Shield Emblem
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFDE68A).copy(alpha = 0.55f))
                    .border(1.5.dp, Color(0xFF78350F), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Shield,
                    contentDescription = null,
                    tint = Color(0xFF92400E),
                    modifier = Modifier.size(34.dp)
                )
            }

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = "TAILS",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black,
                color = Color(0xFF78350F),
                textAlign = TextAlign.Center
            )

            Text(
                text = "• IN LUCK WE TRUST •",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF92400E).copy(alpha = 0.8f)
            )
        }
    }
}

