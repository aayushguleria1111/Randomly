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

/**
 * Authentic Two Lucky Dice branding logo matching the reference design:
 * - Die 1 (Left): Soft icy white, tilted -18°, displaying 5 indigo pips
 * - Die 2 (Right): Brilliant pure white, tilted +35°, displaying 3 diagonal pips
 * - Combined composition is optically centered both horizontally and vertically
 */
@Composable
fun TwoDiceLogo(
    size: Dp = 100.dp,
    animated: Boolean = true,
    modifier: Modifier = Modifier
) {
    val die1Rotation = remember { Animatable(if (animated) -45f else -18f) }
    val die2Rotation = remember { Animatable(if (animated) 65f else 35f) }
    val scale = remember { Animatable(if (animated) 0.5f else 1f) }

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
                targetValue = -18f,
                animationSpec = tween(600, easing = FastOutSlowInEasing)
            )
            die2Rotation.animateTo(
                targetValue = 35f,
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
            val dieSize = totalSize * 0.44f
            val cornerRadius = dieSize * 0.22f
            val pipRadius = dieSize * 0.082f

            // 1. Soft Ground Shadow under both dice (Centrally anchored)
            drawOval(
                color = Color.Black.copy(alpha = 0.28f),
                topLeft = Offset(totalSize * 0.18f, totalSize * 0.71f),
                size = Size(totalSize * 0.64f, totalSize * 0.13f)
            )

            // 2. DIE 1 (Back Left, -18 degrees, showing 5 pips)
            val die1Center = Offset(totalSize * 0.385f, totalSize * 0.445f)
            rotate(degrees = die1Rotation.value, pivot = die1Center) {
                // Die 1 background (Soft Icy Lavender White)
                drawRoundRect(
                    color = Color(0xFFF1F5F9),
                    topLeft = Offset(die1Center.x - dieSize / 2f, die1Center.y - dieSize / 2f),
                    size = Size(dieSize, dieSize),
                    cornerRadius = CornerRadius(cornerRadius, cornerRadius),
                    style = Fill
                )
                // Die 1 wider defined border
                drawRoundRect(
                    color = Color(0xFF64748B),
                    topLeft = Offset(die1Center.x - dieSize / 2f, die1Center.y - dieSize / 2f),
                    size = Size(dieSize, dieSize),
                    cornerRadius = CornerRadius(cornerRadius, cornerRadius),
                    style = Stroke(width = totalSize * 0.032f)
                )

                // 5 Pips (Deep Indigo #3730A3)
                val pipColor = Color(0xFF3730A3)
                val offsetP = dieSize * 0.27f

                // Center pip
                drawCircle(color = pipColor, radius = pipRadius, center = die1Center)
                // 4 corner pips
                drawCircle(color = pipColor, radius = pipRadius, center = Offset(die1Center.x - offsetP, die1Center.y - offsetP))
                drawCircle(color = pipColor, radius = pipRadius, center = Offset(die1Center.x + offsetP, die1Center.y - offsetP))
                drawCircle(color = pipColor, radius = pipRadius, center = Offset(die1Center.x - offsetP, die1Center.y + offsetP))
                drawCircle(color = pipColor, radius = pipRadius, center = Offset(die1Center.x + offsetP, die1Center.y + offsetP))
            }

            // 3. DIE 2 (Front Right, +35 degrees, overlaps Die 1, showing 3 diagonal pips)
            val die2Center = Offset(totalSize * 0.595f, totalSize * 0.505f)
            rotate(degrees = die2Rotation.value, pivot = die2Center) {
                // Die 2 Drop Shadow on Die 1 and surface
                drawRoundRect(
                    color = Color.Black.copy(alpha = 0.22f),
                    topLeft = Offset(die2Center.x - dieSize / 2f - totalSize * 0.015f, die2Center.y - dieSize / 2f + totalSize * 0.018f),
                    size = Size(dieSize, dieSize),
                    cornerRadius = CornerRadius(cornerRadius, cornerRadius),
                    style = Fill
                )
                // Die 2 Body (Radiant Pure White)
                drawRoundRect(
                    color = Color.White,
                    topLeft = Offset(die2Center.x - dieSize / 2f, die2Center.y - dieSize / 2f),
                    size = Size(dieSize, dieSize),
                    cornerRadius = CornerRadius(cornerRadius, cornerRadius),
                    style = Fill
                )
                // Die 2 wider crisp border
                drawRoundRect(
                    color = Color(0xFF334155),
                    topLeft = Offset(die2Center.x - dieSize / 2f, die2Center.y - dieSize / 2f),
                    size = Size(dieSize, dieSize),
                    cornerRadius = CornerRadius(cornerRadius, cornerRadius),
                    style = Stroke(width = totalSize * 0.032f)
                )

                // 3 Diagonal Pips (Electric Indigo #4338CA)
                val pip2Color = Color(0xFF4338CA)
                val offset2 = dieSize * 0.27f

                drawCircle(color = pip2Color, radius = pipRadius * 1.05f, center = Offset(die2Center.x - offset2, die2Center.y - offset2))
                drawCircle(color = pip2Color, radius = pipRadius * 1.05f, center = die2Center)
                drawCircle(color = pip2Color, radius = pipRadius * 1.05f, center = Offset(die2Center.x + offset2, die2Center.y + offset2))
            }
        }
    }
}
