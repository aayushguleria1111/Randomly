package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.example.data.model.AppColorTheme
import com.example.data.model.AppThemeMode

private fun buildLightScheme(primary: Color, secondary: Color) = lightColorScheme(
    primary = primary,
    onPrimary = Color.White,
    primaryContainer = primary.copy(alpha = 0.15f),
    onPrimaryContainer = primary,
    secondary = secondary,
    onSecondary = Color.White,
    secondaryContainer = secondary.copy(alpha = 0.15f),
    onSecondaryContainer = secondary,
    tertiary = TertiaryLight,
    onTertiary = OnTertiaryLight,
    tertiaryContainer = TertiaryContainerLight,
    onTertiaryContainer = OnTertiaryContainerLight,
    background = BackgroundLight,
    onBackground = OnBackgroundLight,
    surface = SurfaceLight,
    onSurface = OnSurfaceLight,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = OnSurfaceVariantLight,
    outline = OutlineLight,
    outlineVariant = OutlineVariantLight
)

private fun buildDarkScheme(primary: Color, secondary: Color) = darkColorScheme(
    primary = primary,
    onPrimary = Color(0xFF0F172A),
    primaryContainer = primary.copy(alpha = 0.25f),
    onPrimaryContainer = Color.White,
    secondary = secondary,
    onSecondary = Color(0xFF0F172A),
    secondaryContainer = secondary.copy(alpha = 0.25f),
    onSecondaryContainer = Color.White,
    tertiary = TertiaryDark,
    onTertiary = OnTertiaryDark,
    tertiaryContainer = TertiaryContainerDark,
    onTertiaryContainer = OnTertiaryContainerDark,
    background = BackgroundDark,
    onBackground = OnBackgroundDark,
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = OnSurfaceVariantDark,
    outline = OutlineDark,
    outlineVariant = OutlineVariantDark
)

@Composable
fun RandomlyTheme(
    themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    colorTheme: AppColorTheme = AppColorTheme.INDIGO,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        AppThemeMode.SYSTEM -> isSystemInDarkTheme()
        AppThemeMode.LIGHT -> false
        AppThemeMode.DARK -> true
    }

    val primary = colorTheme.primaryColor
    val secondary = colorTheme.secondaryColor

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> buildDarkScheme(primary, secondary)
        else -> buildLightScheme(primary, secondary)
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}
