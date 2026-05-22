package com.example.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.prefs.SettingsViewModel
import com.example.ui.syllabuses.DashboardScreen
import com.example.ui.syllabuses.ProgressScreen
import com.example.ui.syllabuses.SettingsScreen
import com.example.ui.syllabuses.SyllabusDetailScreen
import com.example.ui.syllabuses.SyllabusListScreen
import com.example.ui.syllabuses.SyllabusViewModel

@Composable
fun MainScreen(syllabusViewModel: SyllabusViewModel, settingsViewModel: SettingsViewModel) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val items = listOf(
        BottomNavItem("dashboard", "Home", Icons.Default.Home),
        BottomNavItem("syllabuses", "Syllabus", Icons.AutoMirrored.Filled.Assignment),
        BottomNavItem("progress", "Progress", Icons.AutoMirrored.Filled.TrendingUp),
        BottomNavItem("settings", "Settings", Icons.Default.Settings)
    )

    Scaffold(
        bottomBar = {
            if (currentRoute != "detail") {
                NavigationBar {
                    items.forEach { item ->
                        NavigationBarItem(
                            icon = { Icon(item.icon, contentDescription = item.label) },
                            label = { Text(item.label) },
                            selected = currentRoute == item.route,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "dashboard",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("dashboard") {
                DashboardScreen(syllabusViewModel, onNavigateToSyllabus = { id ->
                    syllabusViewModel.selectSyllabus(id)
                    navController.navigate("detail")
                })
            }
            composable("syllabuses") {
                SyllabusListScreen(syllabusViewModel, onNavigateToSyllabus = { id ->
                    syllabusViewModel.selectSyllabus(id)
                    navController.navigate("detail")
                })
            }
            composable("progress") {
                ProgressScreen(syllabusViewModel)
            }
            composable("settings") {
                SettingsScreen(settingsViewModel, syllabusViewModel)
            }
            composable("detail") {
                SyllabusDetailScreen(syllabusViewModel, onNavigateBack = { navController.popBackStack() })
            }
        }
    }
}

data class BottomNavItem(val route: String, val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector)
