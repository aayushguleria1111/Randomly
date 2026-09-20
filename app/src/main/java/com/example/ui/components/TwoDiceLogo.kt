package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun TwoDiceLogo(
    size: Dp = 100.dp,
    animated: Boolean = true,
    modifier: Modifier = Modifier
) {
    val die1Rotation = remember { Animatable(if (animated) -45f else -12f) }
    val die2Rotation = remember { Animatable(if (animated) 55f else 10f) }
    val scale = remember { Animatable(if (animated) 0.3f else 1f) }

    LaunchedEffect(Unit) {
        if (animated) {
            scale.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
            die1Rotation.animateTo(
                targetValue = -12f,
                animationSpec = tween(600, easing = FastOutSlowInEasing)
            )
            die2Rotation.animateTo(
                targetValue = 10f,
                animationSpec = tween(600, easing = FastOutSlowInEasing)
            )
        }
    }

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val totalSize = this.size.minDimension
            val dieSize = totalSize * 0.48f
            val cornerRadius = dieSize * 0.22f
            val pipRadius = dieSize * 0.08f

            // Shadow under both dice
            drawOval(
                color = Color.Black.copy(alpha = 0.18f),
                topLeft = Offset(totalSize * 0.15f, totalSize * 0.74f),
                size = Size(totalSize * 0.70f, totalSize * 0.18f)
            )

            // DIE 1 (Back Left, -12 degrees, showing 5 pips)
            val die1Center = Offset(totalSize * 0.38f, totalSize * 0.44f)
            rotate(degrees = die1Rotation.value, pivot = die1Center) {
                // Die 1 background (Soft Lavender White)
                drawRoundRect(
                    color = Color(0xFFE0E7FF),
                    topLeft = Offset(die1Center.x - dieSize / 2f, die1Center.y - dieSize / 2f),
                    size = Size(dieSize, dieSize),
                    cornerRadius = CornerRadius(cornerRadius, cornerRadius),
                    style = Fill
                )
                // Die 1 border
                drawRoundRect(
                    color = Color(0xFFC7D2FE),
                    topLeft = Offset(die1Center.x - dieSize / 2f, die1Center.y - dieSize / 2f),
                    size = Size(dieSize, dieSize),
                    cornerRadius = CornerRadius(cornerRadius, cornerRadius),
                    style = Stroke(width = totalSize * 0.018f)
                )

                // 5 Pips (Deep Indigo #3730A3)
                val pipColor = Color(0xFF3730A3)
                val offsetP = dieSize * 0.26f

                // Center
                drawCircle(color = pipColor, radius = pipRadius, center = die1Center)
                // 4 corners
                drawCircle(color = pipColor, radius = pipRadius, center = Offset(die1Center.x - offsetP, die1Center.y - offsetP))
                drawCircle(color = pipColor, radius = pipRadius, center = Offset(die1Center.x + offsetP, die1Center.y - offsetP))
                drawCircle(color = pipColor, radius = pipRadius, center = Offset(die1Center.x - offsetP, die1Center.y + offsetP))
                drawCircle(color = pipColor, radius = pipRadius, center = Offset(die1Center.x + offsetP, die1Center.y + offsetP))
            }

            // DIE 2 (Front Right, +10 degrees, overlaps Die 1, showing 3 diagonal pips)
            val die2Center = Offset(totalSize * 0.62f, totalSize * 0.54f)
            rotate(degrees = die2Rotation.value, pivot = die2Center) {
                // Die 2 Drop Shadow on Die 1
                drawRoundRect(
                    color = Color.Black.copy(alpha = 0.15f),
                    topLeft = Offset(die2Center.x - dieSize / 2f - 4f, die2Center.y - dieSize / 2f + 4f),
                    size = Size(dieSize, dieSize),
                    cornerRadius = CornerRadius(cornerRadius, cornerRadius),
                    style = Fill
                )
                // Die 2 Body (Brilliant Pure White)
                drawRoundRect(
                    color = Color.White,
                    topLeft = Offset(die2Center.x - dieSize / 2f, die2Center.y - dieSize / 2f),
                    size = Size(dieSize, dieSize),
                    cornerRadius = CornerRadius(cornerRadius, cornerRadius),
                    style = Fill
                )
                // Die 2 Border
                drawRoundRect(
                    color = Color(0xFFE2E8F0),
                    topLeft = Offset(die2Center.x - dieSize / 2f, die2Center.y - dieSize / 2f),
                    size = Size(dieSize, dieSize),
                    cornerRadius = CornerRadius(cornerRadius, cornerRadius),
                    style = Stroke(width = totalSize * 0.016f)
                )

                // 3 Diagonal Pips (Electric Indigo #4F46E5)
                val pip2Color = Color(0xFF4F46E5)
                val offset2 = dieSize * 0.26f

                drawCircle(color = pip2Color, radius = pipRadius * 1.05f, center = Offset(die2Center.x - offset2, die2Center.y - offset2))
                drawCircle(color = pip2Color, radius = pipRadius * 1.05f, center = die2Center)
                drawCircle(color = pip2Color, radius = pipRadius * 1.05f, center = Offset(die2Center.x + offset2, die2Center.y + offset2))
            }
        }
    }
}
