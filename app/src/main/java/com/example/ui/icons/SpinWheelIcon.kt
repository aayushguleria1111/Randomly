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
 * Custom high-detail vector icon representing a Fortune Spin Wheel.
 * Features a circular wheel rim, radial spokes, center hub, and top indicator ticker.
 */
val SpinWheelIcon: ImageVector by lazy {
    ImageVector.Builder(
        name = "SpinWheelIcon",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        // Top Indicator Arrow / Ticker Pin
        path(
            fill = SolidColor(Color.White),
            fillAlpha = 1.0f,
            stroke = null,
            pathFillType = PathFillType.NonZero
        ) {
            moveTo(12.0f, 6.2f)
            lineTo(9.2f, 1.8f)
            lineTo(14.8f, 1.8f)
            close()
        }

        // Outer Wheel Bezel Ring & Spokes (Combined clean path)
        path(
            fill = null,
            stroke = SolidColor(Color.White),
            strokeLineWidth = 1.6f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        ) {
            // Wheel outer circle (Center at 12, 13.5, Radius 8.5)
            // Arc 1
            moveTo(12.0f, 5.0f)
            arcTo(8.5f, 8.5f, 0.0f, isMoreThanHalf = false, isPositiveArc = true, 20.5f, 13.5f)
            arcTo(8.5f, 8.5f, 0.0f, isMoreThanHalf = false, isPositiveArc = true, 12.0f, 22.0f)
            arcTo(8.5f, 8.5f, 0.0f, isMoreThanHalf = false, isPositiveArc = true, 3.5f, 13.5f)
            arcTo(8.5f, 8.5f, 0.0f, isMoreThanHalf = false, isPositiveArc = true, 12.0f, 5.0f)
            close()

            // 6 Radial Spokes dividing the wheel into sectors
            // Spoke 1: Vertical Up (from 12, 11 to 12, 5)
            moveTo(12.0f, 11.2f)
            lineTo(12.0f, 5.0f)

            // Spoke 2: Vertical Down (from 12, 15.8 to 12, 22)
            moveTo(12.0f, 15.8f)
            lineTo(12.0f, 22.0f)

            // Spoke 3: Top-Right (Angle ~30 deg from horizontal: dx=7.36, dy=-4.25)
            moveTo(13.9f, 12.3f)
            lineTo(19.36f, 9.25f)

            // Spoke 4: Bottom-Left (opposite of Spoke 3)
            moveTo(10.1f, 14.7f)
            lineTo(4.64f, 17.75f)

            // Spoke 5: Bottom-Right (Angle ~-30 deg: dx=7.36, dy=4.25)
            moveTo(13.9f, 14.7f)
            lineTo(19.36f, 17.75f)

            // Spoke 6: Top-Left (opposite of Spoke 5)
            moveTo(10.1f, 12.3f)
            lineTo(4.64f, 9.25f)
        }

        // Center Hub Inner Fill Circle (Center 12, 13.5, Radius 2.4)
        path(
            fill = SolidColor(Color.White),
            fillAlpha = 1.0f,
            pathFillType = PathFillType.NonZero
        ) {
            moveTo(12.0f, 11.1f)
            arcTo(2.4f, 2.4f, 0.0f, isMoreThanHalf = false, isPositiveArc = true, 14.4f, 13.5f)
            arcTo(2.4f, 2.4f, 0.0f, isMoreThanHalf = false, isPositiveArc = true, 12.0f, 15.9f)
            arcTo(2.4f, 2.4f, 0.0f, isMoreThanHalf = false, isPositiveArc = true, 9.6f, 13.5f)
            arcTo(2.4f, 2.4f, 0.0f, isMoreThanHalf = false, isPositiveArc = true, 12.0f, 11.1f)
            close()
        }

        // Alternating Sector Dots / Accent Pins on the Rim
        path(
            fill = SolidColor(Color.White),
            fillAlpha = 0.9f
        ) {
            // Pin 1: Top (12, 5.0)
            moveTo(12.0f, 3.8f)
            arcTo(0.9f, 0.9f, 0.0f, isMoreThanHalf = true, isPositiveArc = true, 12.0f, 5.6f)
            arcTo(0.9f, 0.9f, 0.0f, isMoreThanHalf = true, isPositiveArc = true, 12.0f, 3.8f)
            close()

            // Pin 2: Right (20.5, 13.5)
            moveTo(20.5f, 12.6f)
            arcTo(0.9f, 0.9f, 0.0f, isMoreThanHalf = true, isPositiveArc = true, 20.5f, 14.4f)
            arcTo(0.9f, 0.9f, 0.0f, isMoreThanHalf = true, isPositiveArc = true, 20.5f, 12.6f)
            close()

            // Pin 3: Bottom (12, 22.0)
            moveTo(12.0f, 21.1f)
            arcTo(0.9f, 0.9f, 0.0f, isMoreThanHalf = true, isPositiveArc = true, 12.0f, 22.9f)
            arcTo(0.9f, 0.9f, 0.0f, isMoreThanHalf = true, isPositiveArc = true, 12.0f, 21.1f)
            close()

            // Pin 4: Left (3.5, 13.5)
            moveTo(3.5f, 12.6f)
            arcTo(0.9f, 0.9f, 0.0f, isMoreThanHalf = true, isPositiveArc = true, 3.5f, 14.4f)
            arcTo(0.9f, 0.9f, 0.0f, isMoreThanHalf = true, isPositiveArc = true, 3.5f, 12.6f)
            close()
        }
    }.build()
}
