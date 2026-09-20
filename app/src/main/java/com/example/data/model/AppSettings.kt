package com.example.data.model

import androidx.compose.ui.graphics.Color

enum class AppThemeMode {
    SYSTEM,
    LIGHT,
    DARK
}

enum class AppColorTheme(
    val displayName: String,
    val primaryColor: Color,
    val secondaryColor: Color
) {
    INDIGO("Indigo", Color(0xFF6366F1), Color(0xFF818CF8)),
    EMERALD("Emerald", Color(0xFF10B981), Color(0xFF34D399)),
    AMBER("Amber Sunset", Color(0xFFF59E0B), Color(0xFFFBBF24)),
    ROSE("Rose Pink", Color(0xFFEC4899), Color(0xFFF472B6)),
    CYAN("Ocean Cyan", Color(0xFF06B6D4), Color(0xFF22D3EE)),
    VIOLET("Deep Violet", Color(0xFF8B5CF6), Color(0xFFA78BFA))
}

data class AppSettings(
    val themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    val colorTheme: AppColorTheme = AppColorTheme.INDIGO,
    val hapticsEnabled: Boolean = true,
    val animationsEnabled: Boolean = true,
    val saveHistoryEnabled: Boolean = true,
    val defaultMinNumber: Int = 1,
    val defaultMaxNumber: Int = 100,
    val defaultDiceType: String = "d6",
    val defaultDiceCount: Int = 1
)
