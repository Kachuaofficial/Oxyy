package com.invatech.oxy.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.invatech.oxy.navigation.Screen
import com.invatech.oxy.navigation.bottomNavigationItems
import com.invatech.oxy.auth.OxyUser
import com.invatech.oxy.ui.theme.OxyTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    user: OxyUser,
    onSignOut: () -> Unit = {}
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val currentScreen = bottomNavigationItems.firstOrNull { screen ->
        currentDestination?.hierarchy?.any { it.route == screen.route } == true
    } ?: Screen.Home
    
    Scaffold(

        bottomBar = {
            NavigationBar {
                bottomNavigationItems.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = null) },
                        label = { Text(screen.title) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            navController.navigate(screen.route) {
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
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(user = user)
            }
            composable(Screen.Attendance.route) { AttendanceScreen(user = user) }
            composable(Screen.History.route) { AttendanceHistoryScreen(user = user) }
            composable(Screen.Profile.route) { ProfileScreen(user = user, onSignOut = onSignOut) }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    OxyTheme {
        MainScreen(
            user = OxyUser(
                uid = "demo",
                email = "student@example.com",
                displayName = "Student",
                photoUrl = null,
                semester = "5",
                department = "Computer Science",
                rollNumber = "OXY-24-CS-118",
                attendanceStartDate = "2026-05-25"
            )
        )
    }
}
