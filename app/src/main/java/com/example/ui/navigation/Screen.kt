package com.example.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    data object Home : Screen("home", "Tools", Icons.Filled.Home, Icons.Outlined.Home)
    data object Favorites : Screen("favorites", "Favorites", Icons.Filled.Star, Icons.Outlined.StarBorder)
    data object History : Screen("history", "History", Icons.Filled.History, Icons.Outlined.History)
    data object Settings : Screen("settings", "Settings", Icons.Filled.Settings, Icons.Outlined.Settings)

    // Tool Screens
    data object Number : Screen("tool/number", "Number", Icons.Filled.Casino, Icons.Filled.Casino)
    data object Color : Screen("tool/color", "Color", Icons.Filled.Casino, Icons.Filled.Casino)
    data object Wheel : Screen("tool/wheel", "Wheel", Icons.Filled.Casino, Icons.Filled.Casino)
    data object Dice : Screen("tool/dice", "Dice", Icons.Filled.Casino, Icons.Filled.Casino)
    data object ListPicker : Screen("tool/list_picker", "List Picker", Icons.Filled.Casino, Icons.Filled.Casino)
    data object CoinFlip : Screen("tool/coin_flip", "Coin Flip", Icons.Filled.Casino, Icons.Filled.Casino)
    data object Letter : Screen("tool/letter", "Letter", Icons.Filled.Casino, Icons.Filled.Casino)
    data object StringGenerator : Screen("tool/string_generator", "String Generator", Icons.Filled.Casino, Icons.Filled.Casino)
    data object Date : Screen("tool/date", "Date", Icons.Filled.Casino, Icons.Filled.Casino)
    data object Time : Screen("tool/time", "Time", Icons.Filled.Casino, Icons.Filled.Casino)
    data object Card : Screen("tool/card", "Card", Icons.Filled.Casino, Icons.Filled.Casino)
    data object YesNo : Screen("tool/yes_no", "Yes / No", Icons.Filled.Casino, Icons.Filled.Casino)
    data object Choice : Screen("tool/choice", "Choice", Icons.Filled.Casino, Icons.Filled.Casino)
    data object Bottle : Screen("tool/bottle", "Spin the Bottle", Icons.Filled.Casino, Icons.Filled.Casino)
}

val BottomNavScreens = listOf(
    Screen.Home,
    Screen.Favorites,
    Screen.History,
    Screen.Settings
)
