package com.example.data.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.FormatColorText
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.LooksOne
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.Style
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.ui.icons.SpinBottleIcon
import com.example.ui.icons.SpinWheelIcon

enum class ToolCategory(val title: String) {
    ESSENTIALS("Essentials"),
    GAMES("Games & Fun"),
    DECISIONS("Decisions"),
    UTILITIES("Utilities")
}

enum class ToolType(
    val id: String,
    val title: String,
    val subtitle: String,
    val category: ToolCategory,
    val icon: ImageVector,
    val accentColor: Color,
    val route: String,
    val isNew: Boolean = false
) {
    NUMBER(
        id = "number",
        title = "Random Number",
        subtitle = "Generate numbers instantly",
        category = ToolCategory.ESSENTIALS,
        icon = Icons.Default.LooksOne,
        accentColor = Color(0xFF6366F1),
        route = "tool_number"
    ),
    COLOR(
        id = "color",
        title = "Random Color",
        subtitle = "Discover vibrant palettes & hex codes",
        category = ToolCategory.ESSENTIALS,
        icon = Icons.Default.ColorLens,
        accentColor = Color(0xFFEC4899),
        route = "tool_color"
    ),
    SPIN_WHEEL(
        id = "spin_wheel",
        title = "Spin Wheel",
        subtitle = "Let the wheel decide for you",
        category = ToolCategory.DECISIONS,
        icon = SpinWheelIcon,
        accentColor = Color(0xFFF59E0B),
        route = "tool_wheel"
    ),
    DICE(
        id = "dice",
        title = "Dice Roller",
        subtitle = "Roll d4, d6, d8, d10, d12, d20, d100",
        category = ToolCategory.GAMES,
        icon = Icons.Default.Casino,
        accentColor = Color(0xFF10B981),
        route = "tool_dice"
    ),
    LIST_PICKER(
        id = "list_picker",
        title = "List Picker",
        subtitle = "Pick items or shuffle your custom list",
        category = ToolCategory.DECISIONS,
        icon = Icons.Default.FormatListBulleted,
        accentColor = Color(0xFF8B5CF6),
        route = "tool_list"
    ),
    COIN_FLIP(
        id = "coin_flip",
        title = "Coin Flip",
        subtitle = "Heads or tails with 3D flip",
        category = ToolCategory.GAMES,
        icon = Icons.Default.MonetizationOn,
        accentColor = Color(0xFFEAB308),
        route = "tool_coin"
    ),
    LETTER(
        id = "letter",
        title = "Random Letter",
        subtitle = "Generate uppercase or lowercase letters",
        category = ToolCategory.UTILITIES,
        icon = Icons.Default.FormatColorText,
        accentColor = Color(0xFF3B82F6),
        route = "tool_letter"
    ),
    STRING_GENERATOR(
        id = "string_generator",
        title = "Random String",
        subtitle = "Generate test strings & passwords",
        category = ToolCategory.UTILITIES,
        icon = Icons.Default.Key,
        accentColor = Color(0xFF06B6D4),
        route = "tool_string"
    ),
    DATE(
        id = "date",
        title = "Random Date",
        subtitle = "Pick a random date between two points",
        category = ToolCategory.UTILITIES,
        icon = Icons.Default.DateRange,
        accentColor = Color(0xFF14B8A6),
        route = "tool_date"
    ),
    TIME(
        id = "time",
        title = "Random Time",
        subtitle = "Generate random hours, minutes & seconds",
        category = ToolCategory.UTILITIES,
        icon = Icons.Default.HourglassBottom,
        accentColor = Color(0xFFF97316),
        route = "tool_time"
    ),
    CARD(
        id = "card",
        title = "Playing Card",
        subtitle = "Draw cards from a 52-card deck",
        category = ToolCategory.GAMES,
        icon = Icons.Default.Style,
        accentColor = Color(0xFFEF4444),
        route = "tool_card"
    ),
    YES_NO(
        id = "yes_no",
        title = "Yes / No",
        subtitle = "Quick animated binary decision",
        category = ToolCategory.DECISIONS,
        icon = Icons.Default.QuestionMark,
        accentColor = Color(0xFFA855F7),
        route = "tool_yes_no"
    ),
    CHOICE(
        id = "choice",
        title = "Quick Choice",
        subtitle = "Decide between 2 to 6 quick options",
        category = ToolCategory.DECISIONS,
        icon = Icons.Default.Shuffle,
        accentColor = Color(0xFF0EA5E9),
        route = "tool_choice"
    ),
    SPIN_BOTTLE(
        id = "spin_bottle",
        title = "Spin the Bottle",
        subtitle = "Party spinner with swipe physics & player circle",
        category = ToolCategory.GAMES,
        icon = SpinBottleIcon,
        accentColor = Color(0xFF0D9488),
        route = "tool_bottle",
        isNew = true
    );

    companion object {
        fun fromId(id: String): ToolType? = entries.find { it.id == id }
    }
}
