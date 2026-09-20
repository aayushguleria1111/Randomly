package com.example.data.model

import androidx.compose.ui.graphics.Color

enum class AppThemeMode(val displayName: String) {
    SYSTEM("System Default"),
    LIGHT("Light"),
    DARK("Dark")
}

enum class AppColorTheme(
    val displayName: String,
    val primaryColor: Color,
    val secondaryColor: Color
) {
    INDIGO("Indigo Blue", Color(0xFF6366F1), Color(0xFF4F46E5)),
    EMERALD("Emerald Green", Color(0xFF10B981), Color(0xFF059669)),
    AMBER("Amber Warm", Color(0xFFF59E0B), Color(0xFFD97706)),
    CRIMSON("Crimson Red", Color(0xFFEF4444), Color(0xFFDC2626)),
    PURPLE("Vibrant Violet", Color(0xFF8B5CF6), Color(0xFF7C3AED)),
    CYAN("Ocean Cyan", Color(0xFF06B6D4), Color(0xFF0891B2)),
    ROSE("Neon Rose", Color(0xFFF43F5E), Color(0xFFE11D48))
}

data class AppSettings(
    val themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    val colorTheme: AppColorTheme = AppColorTheme.INDIGO,
    val hapticsEnabled: Boolean = true,
    val animationsEnabled: Boolean = true,
    val saveHistoryEnabled: Boolean = true,
    val defaultMinNumber: Int = 1,
    val defaultMaxNumber: Int = 100,
    val defaultDiceType: String = "d6"
)
