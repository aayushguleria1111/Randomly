package com.example.ui.components

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

// Clean, high-contrast, modern harmonious palette (cohesive jewel & slate accents)
private val WheelPalette = listOf(
    Color(0xFF4F46E5), // Electric Indigo
    Color(0xFF0D9488), // Teal
    Color(0xFF7C3AED), // Deep Violet
    Color(0xFFEA580C), // Warm Amber-Orange
    Color(0xFF0284C7), // Sky Blue
    Color(0xFF059669), // Emerald
    Color(0xFFDB2777), // Deep Rose
    Color(0xFF475569)  // Slate
)

@Composable
fun SpinWheelCanvas(
    items: List<String>,
    currentRotation: Float,
    isSpinning: Boolean,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .sizeIn(maxWidth = 320.dp, maxHeight = 320.dp)
            .aspectRatio(1f)
            .padding(12.dp)
            .clip(CircleShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                enabled = !isSpinning && items.isNotEmpty(),
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        val count = items.size.coerceAtLeast(1)
        val sweepAngle = 360f / count

        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasSize = size.minDimension
            val radius = canvasSize / 2f
            val center = Offset(size.width / 2f, size.height / 2f)

            // Outer bezel ring with rich gold and slate drop shadow effect
            drawCircle(
                color = Color.Black.copy(alpha = 0.25f),
                radius = radius,
                center = center
            )
            // Outer golden brass rim
            drawCircle(
                color = Color(0xFFD97706),
                radius = radius - 1f,
                center = center
            )
            // Polished gold ring
            drawCircle(
                color = Color(0xFFFBBF24),
                radius = radius - 3f,
                center = center,
                style = Stroke(width = 3.5f)
            )
            // Inner dark border framing the sectors
            drawCircle(
                color = Color(0xFF0F172A),
                radius = radius - 6.5f,
                center = center,
                style = Stroke(width = 3f)
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
                    color = Color.White.copy(alpha = 0.55f),
                    start = center,
                    end = lineEnd,
                    strokeWidth = 2.5f
                )

                // Draw Text / Symbol along radial spoke
                val rawText = if (items.isNotEmpty()) items[i].trim() else "Empty"
                val midAngle = startAngle + sweepAngle / 2f

                // Rotate canvas around center so the sector ray lies along positive X-axis
                rotate(degrees = midAngle, pivot = center) {
                    drawContext.canvas.nativeCanvas.apply {
                        val baseDistance = center.x + innerRadius * 0.58f
                        val centerY = center.y

                        // Smart text layout: single row vs 2 rows vs symbol/badge
                        val isNarrow = count > 10 || sweepAngle < 36f
                        val words = rawText.split(Regex("\\s+"))

                        val paint = Paint().apply {
                            this.color = android.graphics.Color.WHITE
                            this.textAlign = Paint.Align.CENTER
                            this.isAntiAlias = true
                            this.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                            this.setShadowLayer(4f, 1f, 1f, android.graphics.Color.argb(180, 0, 0, 0))
                        }

                        if (isNarrow && rawText.length > 8) {
                            // When very narrow, show clean index or initial symbol with badge
                            val initialBadge = "${i + 1}. " + rawText.take(6) + "…"
                            paint.textSize = 22f
                            drawText(initialBadge, baseDistance, centerY + 8f, paint)
                        } else if (words.size >= 2 && rawText.length > 7) {
                            // 2 rows: split words across lines
                            val half = (words.size + 1) / 2
                            val line1 = words.take(half).joinToString(" ").let { if (it.length > 10) it.take(9) + "…" else it }
                            val line2 = words.drop(half).joinToString(" ").let { if (it.length > 10) it.take(9) + "…" else it }

                            paint.textSize = if (count > 8) 20f else 23f
                            val lineSpacing = paint.textSize * 0.65f

                            drawText(line1, baseDistance, centerY - lineSpacing + 4f, paint)
                            drawText(line2, baseDistance, centerY + lineSpacing + 4f, paint)
                        } else {
                            // Single row or split long word
                            val textToDraw = if (rawText.length > 11) rawText.take(10) + "…" else rawText
                            paint.textSize = when {
                                count > 8 -> 22f
                                rawText.length > 8 -> 24f
                                else -> 28f
                            }
                            drawText(textToDraw, baseDistance, centerY + paint.textSize * 0.35f, paint)
                        }
                    }
                }
            }

            // Decorative 3D gold pegs on the rim
            val pegCount = (count * 2).coerceIn(12, 24)
            for (p in 0 until pegCount) {
                val pegAngle = currentRotation + (p * 360f / pegCount)
                val pegRad = (pegAngle * PI / 180f).toFloat()
                val pegDist = radius - 4f
                val pegX = center.x + pegDist * cos(pegRad)
                val pegY = center.y + pegDist * sin(pegRad)

                // Peg base shadow / gold body
                drawCircle(
                    color = Color(0xFFD97706),
                    radius = 3.6f,
                    center = Offset(pegX, pegY)
                )
                drawCircle(
                    color = Color(0xFFFBBF24),
                    radius = 2.8f,
                    center = Offset(pegX, pegY)
                )
                // Specular highlight on pin
                drawCircle(
                    color = Color.White.copy(alpha = 0.85f),
                    radius = 1.0f,
                    center = Offset(pegX - 0.8f, pegY - 0.8f)
                )
            }

            // --- Center Hub Wheel Logo Emblem ---
            // 1. Hub drop shadow
            drawCircle(
                color = Color.Black.copy(alpha = 0.45f),
                radius = radius * 0.23f,
                center = center
            )
            // 2. Outer gold bezel rim
            drawCircle(
                color = Color(0xFFD97706),
                radius = radius * 0.22f,
                center = center
            )
            // 3. Polished bright gold ring
            drawCircle(
                color = Color(0xFFFBBF24),
                radius = radius * 0.20f,
                center = center
            )
            // 4. Deep slate center plate
            drawCircle(
                color = Color(0xFF0F172A),
                radius = radius * 0.17f,
                center = center
            )
            // 5. Micro groove ring
            drawCircle(
                color = Color(0xFF334155),
                radius = radius * 0.155f,
                center = center,
                style = Stroke(width = 2f)
            )
            // 6. Center gold jewel button
            drawCircle(
                color = Color(0xFFF59E0B),
                radius = radius * 0.125f,
                center = center
            )
            drawCircle(
                color = Color(0xFFFEF08A),
                radius = radius * 0.11f,
                center = center,
                style = Stroke(width = 1.5f)
            )

            // Center Fortune Emblem / Text
            if (isSpinning) {
                // Shimmering 4-point Fortune Star Emblem
                val starR = radius * 0.08f
                val starInnerR = radius * 0.025f
                val starPath = Path().apply {
                    moveTo(center.x, center.y - starR)
                    lineTo(center.x + starInnerR, center.y - starInnerR)
                    lineTo(center.x + starR, center.y)
                    lineTo(center.x + starInnerR, center.y + starInnerR)
                    lineTo(center.x, center.y + starR)
                    lineTo(center.x - starInnerR, center.y + starInnerR)
                    lineTo(center.x - starR, center.y)
                    lineTo(center.x - starInnerR, center.y - starInnerR)
                    close()
                }
                drawPath(path = starPath, color = Color(0xFFFEF08A))
            } else {
                drawContext.canvas.nativeCanvas.apply {
                    val hubPaint = Paint().apply {
                        this.color = android.graphics.Color.WHITE
                        this.textSize = 21f
                        this.textAlign = Paint.Align.CENTER
                        this.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                        this.isAntiAlias = true
                        this.setShadowLayer(4f, 0f, 1.5f, android.graphics.Color.argb(180, 0, 0, 0))
                    }
                    drawText("SPIN", center.x, center.y + 7.5f, hubPaint)
                }
            }
        }

        // Top 3D Faceted Ruby Pointer Indicator
        Canvas(
            modifier = Modifier
                .size(36.dp)
                .align(Alignment.TopCenter)
        ) {
            val midX = size.width / 2f
            val tipY = size.height - 2f
            val topY = 4f
            val leftX = 4f
            val rightX = size.width - 4f

            // Pointer Shadow
            val shadowPath = Path().apply {
                moveTo(midX, tipY + 3f)
                lineTo(leftX, topY)
                lineTo(rightX, topY)
                close()
            }
            drawPath(path = shadowPath, color = Color.Black.copy(alpha = 0.35f))

            // Left Facet (Light Ruby)
            val leftFacet = Path().apply {
                moveTo(midX, tipY)
                lineTo(leftX, topY)
                lineTo(midX, topY)
                close()
            }
            drawPath(path = leftFacet, color = Color(0xFFEF4444))

            // Right Facet (Darker Ruby)
            val rightFacet = Path().apply {
                moveTo(midX, tipY)
                lineTo(midX, topY)
                lineTo(rightX, topY)
                close()
            }
            drawPath(path = rightFacet, color = Color(0xFFB91C1C))

            // White Edge Outline for crisp contrast
            val outlinePath = Path().apply {
                moveTo(midX, tipY)
                lineTo(leftX, topY)
                lineTo(rightX, topY)
                close()
            }
            drawPath(path = outlinePath, color = Color.White.copy(alpha = 0.9f), style = Stroke(width = 2.0f))

            // Top Mounting Pin (Gold bead with inner shadow)
            drawCircle(
                color = Color(0xFFD97706),
                radius = 5.5f,
                center = Offset(midX, topY)
            )
            drawCircle(
                color = Color(0xFFFBBF24),
                radius = 4.2f,
                center = Offset(midX, topY)
            )
            drawCircle(
                color = Color(0xFF0F172A),
                radius = 1.8f,
                center = Offset(midX, topY)
            )
        }
    }
}
