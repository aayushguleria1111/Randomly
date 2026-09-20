package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun StartupAnimationOverlay(
    onSplashFinished: () -> Unit
) {
    var isVisible by remember { mutableStateOf(true) }

    val logoScale = remember { Animatable(0.2f) }
    val logoRotation = remember { Animatable(-180f) }
    val textAlpha = remember { Animatable(0f) }

    val infiniteTransition = rememberInfiniteTransition(label = "splashRing")
    val ringRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ringRotation"
    )

    LaunchedEffect(Unit) {
        logoScale.animateTo(
            targetValue = 1f,
            animationSpec = tween(700, easing = FastOutSlowInEasing)
        )
        logoRotation.animateTo(
            targetValue = 0f,
            animationSpec = tween(700, easing = FastOutSlowInEasing)
        )
        textAlpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(500)
        )

        delay(1200)
        isVisible = false
        delay(400)
        onSplashFinished()
    }

    AnimatedVisibility(
        visible = isVisible,
        exit = fadeOut(animationSpec = tween(400)) + scaleOut(targetScale = 1.1f, animationSpec = tween(400))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primaryContainer,
                            MaterialTheme.colorScheme.background,
                            MaterialTheme.colorScheme.surface
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(140.dp)
                ) {
                    val primaryColor = MaterialTheme.colorScheme.primary
                    val secondaryColor = MaterialTheme.colorScheme.secondary
                    val tertiaryColor = MaterialTheme.colorScheme.tertiary

                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawCircle(
                            brush = Brush.sweepGradient(
                                listOf(primaryColor, secondaryColor, tertiaryColor, primaryColor)
                            ),
                            style = Stroke(width = 8.dp.toPx())
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.Casino,
                        contentDescription = "Randomly Logo",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .size(72.dp)
                            .scale(logoScale.value)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Randomly",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Black,
                    fontSize = 36.sp,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier
                        .alpha(textAlpha.value)
                        .scale(logoScale.value)
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "All-in-One Randomness Toolbox",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.alpha(textAlpha.value)
                )
            }
        }
    }
}
