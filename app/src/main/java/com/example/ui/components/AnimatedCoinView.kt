package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.util.RandomGenerators.CoinSide
import kotlinx.coroutines.async

@Composable
fun AnimatedCoinView(
    result: CoinSide,
    isFlipping: Boolean,
    modifier: Modifier = Modifier
) {
    val rotationX = remember { Animatable(0f) }
    val translationY = remember { Animatable(0f) }
    val scale = remember { Animatable(1f) }

    LaunchedEffect(isFlipping) {
        if (isFlipping) {
            // Authentic vertical toss arc
            rotationX.snapTo(0f)
            val flightUp = async {
                scale.animateTo(1.25f, tween(380, easing = androidx.compose.animation.core.LinearOutSlowInEasing))
                scale.animateTo(1.0f, tween(360, easing = androidx.compose.animation.core.FastOutLinearInEasing))
            }
            val tossUp = async {
                translationY.animateTo(-140f, animationSpec = tween(380, easing = androidx.compose.animation.core.LinearOutSlowInEasing))
                translationY.animateTo(0f, animationSpec = tween(360, easing = androidx.compose.animation.core.FastOutLinearInEasing))
                // Bounce settle
                translationY.animateTo(-18f, animationSpec = tween(90, easing = androidx.compose.animation.core.LinearOutSlowInEasing))
                translationY.animateTo(0f, animationSpec = tween(80, easing = androidx.compose.animation.core.FastOutLinearInEasing))
            }
            val rot = async {
                rotationX.animateTo(
                    targetValue = 2160f + if (result == CoinSide.HEADS) 0f else 180f,
                    animationSpec = tween(910, easing = androidx.compose.animation.core.CubicBezierEasing(0.2f, 0f, 0.15f, 1f))
                )
            }
            flightUp.await()
            tossUp.await()
            rot.await()
        }
    }

    Box(
        modifier = modifier
            .size(160.dp)
            .graphicsLayer {
                this.rotationX = rotationX.value
                this.translationY = translationY.value
                this.scaleX = scale.value
                this.scaleY = scale.value
                cameraDistance = 14f * density
            },
        contentAlignment = Alignment.Center
    ) {
        val isHeadsVisible = (rotationX.value.toInt() / 180) % 2 == 0
        val currentDisplay = if (isHeadsVisible) CoinSide.HEADS else CoinSide.TAILS

        // Outer Coin Disk (Gold Gradient with edge ridges)
        Box(
            modifier = Modifier
                .size(150.dp)
                .shadow(12.dp, CircleShape)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFFEF08A), // Light Gold
                            Color(0xFFF59E0B), // Warm Gold
                            Color(0xFFD97706)  // Dark Gold
                        )
                    )
                )
                .border(6.dp, Color(0xFFB45309), CircleShape)
                .padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            // Inner decorative ring
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .clip(CircleShape)
                    .border(2.dp, Color(0xFFFDE68A), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (currentDisplay == CoinSide.HEADS) "👑" else "⚡",
                        fontSize = 32.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = currentDisplay.label.uppercase(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF78350F),
                        letterSpacing = 1.sp
                    )
                }
            }
        }
    }
}
