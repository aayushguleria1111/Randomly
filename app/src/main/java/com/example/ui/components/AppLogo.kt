package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * The official branding logo component for Randomly:
 * Represents the iconic Two Lucky Dice logo with authentic dynamic physics.
 */
@Composable
fun AppLogo(
    size: Dp = 40.dp,
    animated: Boolean = false,
    modifier: Modifier = Modifier
) {
    if (animated) {
        val infiniteTransition = rememberInfiniteTransition(label = "AppLogoPulse")
        val haloAlpha by infiniteTransition.animateFloat(
            initialValue = 0.20f,
            targetValue = 0.45f,
            animationSpec = infiniteRepeatable(
                animation = tween(2000, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "HaloAlpha"
        )

        Box(
            modifier = modifier.size(size * 1.2f),
            contentAlignment = Alignment.Center
        ) {
            // Ambient glowing radial halo
            Box(
                modifier = Modifier
                    .size(size * 1.15f)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFF6366F1).copy(alpha = haloAlpha),
                                Color(0xFFA855F7).copy(alpha = haloAlpha * 0.4f),
                                Color.Transparent
                            )
                        )
                    )
            )

            TwoDiceLogo(
                size = size,
                animated = true
            )
        }
    } else {
        TwoDiceLogo(
            size = size,
            animated = false,
            modifier = modifier
        )
    }
}
