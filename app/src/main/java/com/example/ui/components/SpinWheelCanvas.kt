package com.example.ui.components

import android.graphics.Paint
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

private val WheelPalette = listOf(
    Color(0xFF6366F1), // Indigo
    Color(0xFFEC4899), // Pink
    Color(0xFF10B981), // Emerald
    Color(0xFFF59E0B), // Amber
    Color(0xFF8B5CF6), // Violet
    Color(0xFF06B6D4), // Cyan
    Color(0xFFF97316), // Orange
    Color(0xFF3B82F6), // Blue
    Color(0xFF14B8A6), // Teal
    Color(0xFFEF4444)  // Red
)

@Composable
fun SpinWheelCanvas(
    items: List<String>,
    currentRotation: Float,
    isSpinning: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        val count = items.size.coerceAtLeast(1)
        val sweepAngle = 360f / count

        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasSize = size.minDimension
            val radius = canvasSize / 2f
            val center = Offset(size.width / 2f, size.height / 2f)

            // Draw outer border ring
            drawCircle(
                color = Color(0xFF1E293B),
                radius = radius,
                center = center
            )
            drawCircle(
                color = Color(0xFF334155),
                radius = radius - 4f,
                center = center,
                style = Stroke(width = 6f)
            )

            val innerRadius = radius - 8f

            // Draw sectors
            for (i in 0 until count) {
                val startAngle = currentRotation + (i * sweepAngle)
                val color = WheelPalette[i % WheelPalette.size]

                drawArc(
                    color = color,
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = true,
                    topLeft = Offset(center.x - innerRadius, center.y - innerRadius),
                    size = Size(innerRadius * 2f, innerRadius * 2f),
                    style = Fill
                )

                // Divider line between sectors
                val angleRad = (startAngle * PI / 180f).toFloat()
                val lineEnd = Offset(
                    center.x + innerRadius * cos(angleRad),
                    center.y + innerRadius * sin(angleRad)
                )
                drawLine(
                    color = Color.White.copy(alpha = 0.6f),
                    start = center,
                    end = lineEnd,
                    strokeWidth = 2.5f
                )

                // Draw Text inside sector
                val text = if (items.isNotEmpty()) items[i] else "Empty"
                val truncatedText = if (text.length > 14) text.take(12) + "…" else text
                val midAngleRad = ((startAngle + sweepAngle / 2f) * PI / 180f).toFloat()

                val textDistance = innerRadius * 0.62f
                val textX = center.x + textDistance * cos(midAngleRad)
                val textY = center.y + textDistance * sin(midAngleRad)

                drawContext.canvas.nativeCanvas.apply {
                    save()
                    rotate(startAngle + sweepAngle / 2f + 90f, textX, textY)
                    val paint = Paint().apply {
                        this.color = android.graphics.Color.WHITE
                        this.textSize = if (count > 10) 24f else 32f
                        this.textAlign = Paint.Align.CENTER
                        this.isAntiAlias = true
                        this.isFakeBoldText = true
                        this.setShadowLayer(4f, 1f, 1f, android.graphics.Color.BLACK)
                    }
                    drawText(truncatedText, textX, textY, paint)
                    restore()
                }
            }

            // Draw Center Hub
            drawCircle(
                color = Color.White,
                radius = radius * 0.18f,
                center = center
            )
            drawCircle(
                color = Color(0xFF1E293B),
                radius = radius * 0.14f,
                center = center
            )
            drawCircle(
                color = Color(0xFFF59E0B),
                radius = radius * 0.08f,
                center = center
            )
        }

        // Top pointer indicator
        Canvas(
            modifier = Modifier
                .size(36.dp)
                .align(Alignment.TopCenter)
        ) {
            val path = Path().apply {
                moveTo(size.width / 2f, size.height)
                lineTo(0f, 0f)
                lineTo(size.width, 0f)
                close()
            }
            drawPath(path = path, color = Color(0xFFEF4444))
            drawPath(path = path, color = Color.White, style = Stroke(width = 3f))
        }
    }
}
