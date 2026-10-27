package com.eatai.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.eatai.screens.AccountScreen
import com.eatai.screens.FiltersScreen
import com.eatai.screens.HistoryScreen
import com.eatai.screens.MapScreen
import com.eatai.screens.PreferencesScreen
import com.eatai.viewmodel.MapViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun NavGraph(navController: NavHostController, contentPadding: PaddingValues) {
    // Shared between the map and filters screens, so Apply can update the filter and trigger a search.
    val mapViewModel = koinViewModel<MapViewModel>()

    NavHost(
        navController = navController,
        startDestination = Screen.Map.route,
        modifier = Modifier.padding(contentPadding),
    ) {
        composable(Screen.Map.route) {
            MapScreen(
                viewModel = mapViewModel,
                onOpenFilters = { navController.navigate(Screen.Filters.route) },
            )
        }
        composable(Screen.History.route) { HistoryScreen() }
        composable(Screen.Account.route) { AccountScreen() }
        composable(Screen.Filters.route) {
            val state by mapViewModel.uiState.collectAsState()
            FiltersScreen(
                initialFilter = state.filter,
                onBack = { navController.popBackStack() },
                onApply = { filter ->
                    mapViewModel.onApplyFilter(filter)
                    navController.popBackStack()
                },
            )
        }
        composable(Screen.Preferences.route) {
            PreferencesScreen(onBack = { navController.popBackStack() })
        }
    }
}
