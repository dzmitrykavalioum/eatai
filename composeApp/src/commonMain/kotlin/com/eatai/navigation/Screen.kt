package com.eatai.navigation

sealed class Screen(val route: String) {
    data object Map : Screen("map")
    data object History : Screen("history")
    data object Account : Screen("account")
    data object Filters : Screen("filters")
    data object Preferences : Screen("preferences")
}
