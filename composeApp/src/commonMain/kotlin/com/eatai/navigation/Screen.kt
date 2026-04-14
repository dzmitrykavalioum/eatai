package com.eatai.navigation

sealed class Screen(val route: String) {
    data object Map : Screen("map")
    data object Preferences : Screen("preferences")
}
