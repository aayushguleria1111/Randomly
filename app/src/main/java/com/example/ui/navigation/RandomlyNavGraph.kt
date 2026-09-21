package com.example.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.data.model.ToolType
import com.example.ui.screens.FavoritesScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.tools.CardScreen
import com.example.ui.screens.tools.ChoiceScreen
import com.example.ui.screens.tools.CoinFlipScreen
import com.example.ui.screens.tools.ColorScreen
import com.example.ui.screens.tools.DateScreen
import com.example.ui.screens.tools.DiceScreen
import com.example.ui.screens.tools.LetterScreen
import com.example.ui.screens.tools.ListPickerScreen
import com.example.ui.screens.tools.NumberScreen
import com.example.ui.screens.tools.StringGeneratorScreen
import com.example.ui.screens.tools.TimeScreen
import com.example.ui.screens.tools.WheelScreen
import com.example.ui.screens.tools.YesNoScreen
import com.example.ui.viewmodel.RandomlyViewModel
import com.example.util.AudioHapticFeedback

@Composable
fun RandomlyNavGraph(
    viewModel: RandomlyViewModel,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    val context = LocalContext.current
    val settings by viewModel.settings.collectAsState()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.snackbarMessage.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    val isTopLevelDestination = BottomNavScreens.any { it.route == currentRoute }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (isTopLevelDestination) {
                NavigationBar {
                    BottomNavScreens.forEach { screen ->
                        val selected = currentRoute == screen.route
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                if (currentRoute != screen.route) {
                                    AudioHapticFeedback.onTabSwitch(context, settings.soundEffectsEnabled, settings.hapticsEnabled)
                                    navController.navigate(screen.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = if (selected) screen.selectedIcon else screen.unselectedIcon,
                                    contentDescription = screen.title
                                )
                            },
                            label = { Text(screen.title) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            // Tab destinations
            composable(Screen.Home.route) {
                HomeScreen(
                    viewModel = viewModel,
                    onNavigateToTool = { tool ->
                        AudioHapticFeedback.onToolClick(context, settings.soundEffectsEnabled, settings.hapticsEnabled)
                        val targetRoute = when (tool) {
                            ToolType.NUMBER -> Screen.Number.route
                            ToolType.COLOR -> Screen.Color.route
                            ToolType.SPIN_WHEEL -> Screen.Wheel.route
                            ToolType.DICE -> Screen.Dice.route
                            ToolType.LIST_PICKER -> Screen.ListPicker.route
                            ToolType.COIN_FLIP -> Screen.CoinFlip.route
                            ToolType.LETTER -> Screen.Letter.route
                            ToolType.STRING_GENERATOR -> Screen.StringGenerator.route
                            ToolType.DATE -> Screen.Date.route
                            ToolType.TIME -> Screen.Time.route
                            ToolType.CARD -> Screen.Card.route
                            ToolType.YES_NO -> Screen.YesNo.route
                            ToolType.CHOICE -> Screen.Choice.route
                        }
                        navController.navigate(targetRoute)
                    }
                )
            }

            composable(Screen.Favorites.route) {
                FavoritesScreen(
                    viewModel = viewModel,
                    onNavigateToTool = { tool ->
                        AudioHapticFeedback.onToolClick(context, settings.soundEffectsEnabled, settings.hapticsEnabled)
                        val targetRoute = when (tool) {
                            ToolType.NUMBER -> Screen.Number.route
                            ToolType.COLOR -> Screen.Color.route
                            ToolType.SPIN_WHEEL -> Screen.Wheel.route
                            ToolType.DICE -> Screen.Dice.route
                            ToolType.LIST_PICKER -> Screen.ListPicker.route
                            ToolType.COIN_FLIP -> Screen.CoinFlip.route
                            ToolType.LETTER -> Screen.Letter.route
                            ToolType.STRING_GENERATOR -> Screen.StringGenerator.route
                            ToolType.DATE -> Screen.Date.route
                            ToolType.TIME -> Screen.Time.route
                            ToolType.CARD -> Screen.Card.route
                            ToolType.YES_NO -> Screen.YesNo.route
                            ToolType.CHOICE -> Screen.Choice.route
                        }
                        navController.navigate(targetRoute)
                    },
                    onNavigateToHome = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                        }
                    }
                )
            }

            composable(Screen.History.route) {
                HistoryScreen(
                    viewModel = viewModel,
                    onNavigateToHome = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                        }
                    }
                )
            }

            composable(Screen.Settings.route) {
                SettingsScreen(viewModel = viewModel)
            }

            // Individual Tool destinations
            composable(Screen.Number.route) {
                NumberScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
            }
            composable(Screen.Color.route) {
                ColorScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
            }
            composable(Screen.Wheel.route) {
                WheelScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
            }
            composable(Screen.Dice.route) {
                DiceScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
            }
            composable(Screen.ListPicker.route) {
                ListPickerScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
            }
            composable(Screen.CoinFlip.route) {
                CoinFlipScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
            }
            composable(Screen.Letter.route) {
                LetterScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
            }
            composable(Screen.StringGenerator.route) {
                StringGeneratorScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
            }
            composable(Screen.Date.route) {
                DateScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
            }
            composable(Screen.Time.route) {
                TimeScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
            }
            composable(Screen.Card.route) {
                CardScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
            }
            composable(Screen.YesNo.route) {
                YesNoScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
            }
            composable(Screen.Choice.route) {
                ChoiceScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
            }
        }
    }
}
