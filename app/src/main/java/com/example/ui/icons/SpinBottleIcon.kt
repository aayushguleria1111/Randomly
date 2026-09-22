package com.example.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

/**
 * Custom vector icon representing a party spin bottle.
 */
val SpinBottleIcon: ImageVector by lazy {
    ImageVector.Builder(
        name = "SpinBottleIcon",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        // Bottle silhouette
        path(
            fill = SolidColor(Color.White),
            fillAlpha = 1.0f,
            stroke = null,
            pathFillType = PathFillType.NonZero
        ) {
            moveTo(11.0f, 2.0f)
            lineTo(13.0f, 2.0f)
            lineTo(13.0f, 6.0f)
            curveTo(14.5f, 7.5f, 16.0f, 9.5f, 16.0f, 12.0f)
            lineTo(16.0f, 20.0f)
            curveTo(16.0f, 21.1f, 15.1f, 22.0f, 14.0f, 22.0f)
            lineTo(10.0f, 22.0f)
            curveTo(8.9f, 22.0f, 8.0f, 21.1f, 8.0f, 20.0f)
            lineTo(8.0f, 12.0f)
            curveTo(8.0f, 9.5f, 9.5f, 7.5f, 11.0f, 6.0f)
            close()
        }

        // Bottle Cap / Accent
        path(
            fill = SolidColor(Color(0xFFF59E0B)),
            fillAlpha = 1.0f,
            stroke = null
        ) {
            moveTo(10.5f, 1.0f)
            lineTo(13.5f, 1.0f)
            lineTo(13.5f, 3.0f)
            lineTo(10.5f, 3.0f)
            close()
        }

        // Circular Spin Arc 1 (Left)
        path(
            fill = null,
            stroke = SolidColor(Color.White),
            strokeLineWidth = 1.5f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        ) {
            moveTo(4.5f, 9.0f)
            curveTo(3.5f, 11.0f, 3.5f, 13.5f, 4.5f, 15.5f)
        }

        // Circular Spin Arc 2 (Right)
        path(
            fill = null,
            stroke = SolidColor(Color.White),
            strokeLineWidth = 1.5f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        ) {
            moveTo(19.5f, 15.5f)
            curveTo(20.5f, 13.5f, 20.5f, 11.0f, 19.5f, 9.0f)
        }
    }.build()
}
