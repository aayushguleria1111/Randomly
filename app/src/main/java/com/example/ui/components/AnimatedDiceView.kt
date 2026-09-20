package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.util.RandomGenerators.DiceType

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AnimatedDiceView(
    diceType: DiceType,
    rolls: List<Int>,
    isRolling: Boolean,
    modifier: Modifier = Modifier
) {
    val rotation = remember { Animatable(0f) }
    val scale = remember { Animatable(1f) }

    LaunchedEffect(isRolling) {
        if (isRolling) {
            rotation.animateTo(
                targetValue = 360f,
                animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing)
            )
            rotation.snapTo(0f)
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalArrangement = Arrangement.Center,
            maxItemsInEachRow = 5
        ) {
            rolls.forEachIndexed { index, value ->
                SingleDiceView(
                    diceType = diceType,
                    value = value,
                    modifier = Modifier
                        .padding(8.dp)
                        .rotate(if (isRolling) rotation.value * ((index % 2) * 2 - 1) else 0f)
                )
            }
        }
    }
}

@Composable
fun SingleDiceView(
    diceType: DiceType,
    value: Int,
    modifier: Modifier = Modifier
) {
    val diceColor = when (diceType) {
        DiceType.D4 -> Color(0xFFE11D48)
        DiceType.D6 -> Color(0xFF4F46E5)
        DiceType.D8 -> Color(0xFF0D9488)
        DiceType.D10 -> Color(0xFFD97706)
        DiceType.D12 -> Color(0xFF7C3AED)
        DiceType.D20 -> Color(0xFF2563EB)
        DiceType.D100 -> Color(0xFF059669)
    }

    if (diceType == DiceType.D6) {
        // Classic D6 with clean dots
        Box(
            modifier = modifier
                .size(72.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surface)
                .border(2.dp, diceColor.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                .padding(10.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val dotRadius = 4.5.dp.toPx()
                val dotColor = diceColor

                val w = size.width
                val h = size.height
                val left = w * 0.25f
                val center = w * 0.5f
                val right = w * 0.75f
                val top = h * 0.25f
                val mid = h * 0.5f
                val bot = h * 0.75f

                when (value) {
                    1 -> {
                        drawCircle(dotColor, dotRadius * 1.3f, Offset(center, mid))
                    }
                    2 -> {
                        drawCircle(dotColor, dotRadius, Offset(left, top))
                        drawCircle(dotColor, dotRadius, Offset(right, bot))
                    }
                    3 -> {
                        drawCircle(dotColor, dotRadius, Offset(left, top))
                        drawCircle(dotColor, dotRadius, Offset(center, mid))
                        drawCircle(dotColor, dotRadius, Offset(right, bot))
                    }
                    4 -> {
                        drawCircle(dotColor, dotRadius, Offset(left, top))
                        drawCircle(dotColor, dotRadius, Offset(right, top))
                        drawCircle(dotColor, dotRadius, Offset(left, bot))
                        drawCircle(dotColor, dotRadius, Offset(right, bot))
                    }
                    5 -> {
                        drawCircle(dotColor, dotRadius, Offset(left, top))
                        drawCircle(dotColor, dotRadius, Offset(right, top))
                        drawCircle(dotColor, dotRadius, Offset(center, mid))
                        drawCircle(dotColor, dotRadius, Offset(left, bot))
                        drawCircle(dotColor, dotRadius, Offset(right, bot))
                    }
                    6 -> {
                        drawCircle(dotColor, dotRadius, Offset(left, top))
                        drawCircle(dotColor, dotRadius, Offset(right, top))
                        drawCircle(dotColor, dotRadius, Offset(left, mid))
                        drawCircle(dotColor, dotRadius, Offset(right, mid))
                        drawCircle(dotColor, dotRadius, Offset(left, bot))
                        drawCircle(dotColor, dotRadius, Offset(right, bot))
                    }
                    else -> {
                        // Fallback numeric
                    }
                }
            }
        }
    } else {
        // Polyhedral Dice (Hexagon / Polygon shaped card)
        Box(
            modifier = modifier
                .size(72.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(diceColor.copy(alpha = 0.12f))
                .border(2.dp, diceColor, RoundedCornerShape(18.dp)),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = diceType.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = diceColor,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "$value",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}
