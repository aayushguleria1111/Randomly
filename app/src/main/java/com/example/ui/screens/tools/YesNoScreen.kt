package com.example.ui.screens.tools

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RemoveCircle
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ToolType
import com.example.ui.components.ResultDisplayCard
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.EmeraldAccent
import com.example.ui.theme.RoseAccent
import com.example.ui.viewmodel.RandomlyViewModel
import com.example.util.HapticFeedbackUtil
import com.example.util.RandomGenerators
import com.example.util.ShareUtil
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val MAX_QUESTION_LENGTH = 30

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
    var isDeciding by remember { mutableStateOf(false) }

    var yesCount by remember { mutableIntStateOf(0) }
    var noCount by remember { mutableIntStateOf(0) }
    var maybeCount by remember { mutableIntStateOf(0) }

    val scope = androidx.compose.runtime.rememberCoroutineScope()
    val shakeAngle = remember { androidx.compose.animation.core.Animatable(0f) }
    val shakeOffset = remember { androidx.compose.animation.core.Animatable(0f) }
    val popScale = remember { androidx.compose.animation.core.Animatable(1f) }

    fun decide() {
        if (isDeciding) return
        isDeciding = true
        scope.launch {
            if (settings.animationsEnabled) {
                // Mystical wobble/shake
                val shakeJob = launch {
                    for (i in 1..5) {
                        HapticFeedbackUtil.performImpact(context, settings.hapticsEnabled)
                        val angle = if (i % 2 == 0) 7f else -7f
                        val offset = if (i % 2 == 0) 9f else -9f
                        shakeAngle.animateTo(angle, androidx.compose.animation.core.tween(50))
                        shakeOffset.animateTo(offset, androidx.compose.animation.core.tween(50))
                    }
                    shakeAngle.animateTo(0f, androidx.compose.animation.core.tween(60))
                    shakeOffset.animateTo(0f, androidx.compose.animation.core.tween(60))
                }
                shakeJob.join()
            }

            val decision = RandomGenerators.generateYesNo(includeMaybe)
            resultText = decision
            isDeciding = false
            HapticFeedbackUtil.performSuccess(context, settings.hapticsEnabled)

            if (settings.animationsEnabled) {
                popScale.snapTo(0.7f)
                popScale.animateTo(
                    targetValue = 1f,
                    animationSpec = androidx.compose.animation.core.spring(
                        dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy,
                        stiffness = androidx.compose.animation.core.Spring.StiffnessLow
                    )
                )
            }

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
    }

    val accentColor = when (resultText) {
        "YES" -> EmeraldAccent
        "NO" -> RoseAccent
        "MAYBE" -> AmberAccent
        else -> ToolType.YES_NO.accentColor
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
                .padding(horizontal = 12.dp),
            contentPadding = PaddingValues(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Main Decision Stage Box (High-Contrast, Prominent Decision Showcase)
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 520.dp)
                        .graphicsLayer {
                            rotationZ = shakeAngle.value
                            translationX = shakeOffset.value
                            scaleX = popScale.value
                            scaleY = popScale.value
                        }
                        .clip(RoundedCornerShape(24.dp))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            enabled = !isDeciding,
                            onClick = { decide() }
                        ),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = accentColor.copy(alpha = 0.14f)
                    ),
                    border = BorderStroke(2.dp, accentColor)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 28.dp, horizontal = 20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Badge Icon
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(accentColor.copy(alpha = 0.22f)),
                            contentAlignment = Alignment.Center
                        ) {
                            val icon = when {
                                isDeciding -> Icons.Default.HelpOutline
                                resultText == "YES" -> Icons.Default.CheckCircle
                                resultText == "NO" -> Icons.Default.RemoveCircle
                                else -> Icons.Default.HelpOutline
                            }
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = accentColor,
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Large Decision Text
                        AnimatedContent(
                            targetState = if (isDeciding) "?" else (resultText ?: "DECIDE"),
                            transitionSpec = { fadeIn() togetherWith fadeOut() },
                            label = "decisionText"
                        ) { targetDecision ->
                            Text(
                                text = targetDecision,
                                style = MaterialTheme.typography.displayMedium.copy(
                                    fontSize = 44.sp,
                                    letterSpacing = 1.sp
                                ),
                                fontWeight = FontWeight.Black,
                                color = accentColor,
                                textAlign = TextAlign.Center
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        val questionDisplay = when {
                            isDeciding -> "Seeking answer..."
                            questionInput.isNotBlank() -> "\"$questionInput\""
                            else -> "Tap stage or button to decide"
                        }
                        Text(
                            text = questionDisplay,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // Primary Decide Button (Clean, responsive size)
            item {
                Button(
                    onClick = { decide() },
                    enabled = !isDeciding,
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 520.dp)
                        .heightIn(min = 48.dp, max = 52.dp)
                        .testTag("make_decision_button"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = accentColor)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isDeciding) "DECIDING..." else "DECIDE NOW",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Decision Results Display Card (Copy, Share, Regenerate)
            resultText?.let { decision ->
                item {
                    Box(modifier = Modifier.fillMaxWidth().widthIn(max = 520.dp)) {
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
            }

            // Stats row
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 520.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = EmeraldAccent.copy(alpha = 0.12f)),
                        border = BorderStroke(1.dp, EmeraldAccent.copy(alpha = 0.3f))
                    ) {
                        Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("YES", style = MaterialTheme.typography.labelMedium, color = EmeraldAccent, fontWeight = FontWeight.Bold)
                            Text("$yesCount", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
                        }
                    }
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = RoseAccent.copy(alpha = 0.12f)),
                        border = BorderStroke(1.dp, RoseAccent.copy(alpha = 0.3f))
                    ) {
                        Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("NO", style = MaterialTheme.typography.labelMedium, color = RoseAccent, fontWeight = FontWeight.Bold)
                            Text("$noCount", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
                        }
                    }
                    if (includeMaybe) {
                        Card(
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(containerColor = AmberAccent.copy(alpha = 0.12f)),
                            border = BorderStroke(1.dp, AmberAccent.copy(alpha = 0.3f))
                        ) {
                            Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("MAYBE", style = MaterialTheme.typography.labelMedium, color = AmberAccent, fontWeight = FontWeight.Bold)
                                Text("$maybeCount", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
                            }
                        }
                    }
                }
            }

            // Controls & Question Input Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 520.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Decision Setup", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                        Spacer(modifier = Modifier.height(10.dp))

                        // Question Input Field with limit (30 letters)
                        OutlinedTextField(
                            value = questionInput,
                            onValueChange = {
                                if (it.length <= MAX_QUESTION_LENGTH) {
                                    questionInput = it
                                }
                            },
                            label = { Text("What's your question? (${questionInput.length}/$MAX_QUESTION_LENGTH)") },
                            modifier = Modifier.fillMaxWidth().testTag("input_yes_no_question"),
                            singleLine = true,
                            maxLines = 1
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Include 'Maybe' Option", style = MaterialTheme.typography.bodyMedium)
                                Text(
                                    "Allows 3-way decision probability",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = includeMaybe,
                                onCheckedChange = { includeMaybe = it }
                            )
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}
