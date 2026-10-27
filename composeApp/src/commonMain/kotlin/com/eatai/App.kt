package com.eatai

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.eatai.navigation.NavGraph
import com.eatai.navigation.Screen
import org.koin.compose.KoinContext

private data class BottomNavItem(val screen: Screen, val label: String, val icon: ImageVector)

private val bottomNavItems = listOf(
    BottomNavItem(Screen.Map, "Map", Icons.Filled.Map),
    BottomNavItem(Screen.History, "History", Icons.Filled.History),
    BottomNavItem(Screen.Account, "My account", Icons.Filled.Person),
)

@Composable
fun App() {
    KoinContext {
        MaterialTheme {
            val navController = rememberNavController()
            val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

            Scaffold(
                bottomBar = {
                    // Hidden on secondary screens such as Filters.
                    if (bottomNavItems.any { it.screen.route == currentRoute }) {
                        BottomNavBar(navController, currentRoute)
                    }
                },
            ) { innerPadding ->
                NavGraph(navController = navController, contentPadding = innerPadding)
            }
        }
    }
}

@Composable
private fun BottomNavBar(navController: NavHostController, currentRoute: String?) {
    NavigationBar {
        bottomNavItems.forEach { item ->
            NavigationBarItem(
                selected = currentRoute == item.screen.route,
                onClick = {
                    navController.navigate(item.screen.route) {
                        popUpTo(Screen.Map.route) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) },
            )
        }
    }
}
