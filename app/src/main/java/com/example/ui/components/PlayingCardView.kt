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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.util.RandomGenerators.PlayingCard
import kotlinx.coroutines.delay

@Composable
fun PlayingCardView(
    card: PlayingCard,
    modifier: Modifier = Modifier,
    dealIndex: Int = 0,
    animateDeal: Boolean = true
) {
    val rotationY = remember { Animatable(if (animateDeal) 180f else 0f) }
    val slideY = remember { Animatable(if (animateDeal) -40f else 0f) }
    val scale = remember { Animatable(if (animateDeal) 0.8f else 1f) }

    LaunchedEffect(card, animateDeal) {
        if (animateDeal) {
            rotationY.snapTo(180f)
            slideY.snapTo(-40f)
            scale.snapTo(0.85f)
            delay(dealIndex * 120L)
            
            // Deal slide-down
            slideY.animateTo(0f, tween(260, easing = FastOutSlowInEasing))
            scale.animateTo(1f, tween(200, easing = FastOutSlowInEasing))
            
            // 3D Flip
            rotationY.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = 420, easing = FastOutSlowInEasing)
            )
        } else {
            rotationY.snapTo(0f)
            slideY.snapTo(0f)
            scale.snapTo(1f)
        }
    }

    val currentRotation = rotationY.value
    val isBackShowing = currentRotation > 90f

    Card(
        modifier = modifier
            .size(width = 88.dp, height = 128.dp)
            .graphicsLayer {
                translationY = slideY.value
                scaleX = scale.value
                scaleY = scale.value
                this.rotationY = currentRotation
                cameraDistance = 14f * density
            },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isBackShowing) Color(0xFF1E293B) else Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        if (isBackShowing) {
            // Card Back Design (Mirrored horizontally so it looks upright while flipped)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { this.rotationY = 180f }
                    .border(2.5.dp, Color(0xFFF1F5F9), RoundedCornerShape(12.dp))
                    .padding(5.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF0F172A)),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val step = 12.dp.toPx()
                    val lineColor = Color(0xFF38BDF8).copy(alpha = 0.25f)
                    for (x in -size.height.toInt()..size.width.toInt() step step.toInt()) {
                        drawLine(
                            color = lineColor,
                            start = Offset(x.toFloat(), 0f),
                            end = Offset(x + size.height, size.height),
                            strokeWidth = 1.5.dp.toPx()
                        )
                        drawLine(
                            color = lineColor,
                            start = Offset(x.toFloat(), size.height),
                            end = Offset(x + size.height, 0f),
                            strokeWidth = 1.5.dp.toPx()
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF1E293B))
                        .border(1.5.dp, Color(0xFF38BDF8), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "♠",
                        color = Color(0xFF38BDF8),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        } else {
            // Card Front Face
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .border(1.5.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
                    .padding(8.dp)
            ) {
                // Top Left
                Column(
                    modifier = Modifier.align(Alignment.TopStart),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = card.rank.symbol,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = card.suit.color
                    )
                    Text(
                        text = card.suit.symbol,
                        fontSize = 12.sp,
                        color = card.suit.color
                    )
                }

                // Center Symbol
                Text(
                    text = card.suit.symbol,
                    fontSize = 34.sp,
                    color = card.suit.color,
                    modifier = Modifier.align(Alignment.Center)
                )

                // Bottom Right
                Column(
                    modifier = Modifier.align(Alignment.BottomEnd),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = card.suit.symbol,
                        fontSize = 12.sp,
                        color = card.suit.color
                    )
                    Text(
                        text = card.rank.symbol,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = card.suit.color
                    )
                }
            }
        }
    }
}

