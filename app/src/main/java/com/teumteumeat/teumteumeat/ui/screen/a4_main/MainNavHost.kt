package com.teumteumeat.teumteumeat.ui.screen.a4_main

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.teumteumeat.teumteumeat.ui.screen.a4_main.a4_1_home.HomeScreen
import com.teumteumeat.teumteumeat.ui.screen.a4_main.a4_1_home.HomeViewModel
import com.teumteumeat.teumteumeat.ui.screen.a4_main.a4_2_library.LibraryScreen
import com.teumteumeat.teumteumeat.ui.screen.a4_main.a4_2_library.LibraryViewModel
import java.time.LocalDate
import java.time.YearMonth


@Composable
fun MainNavHost(
    modifier: Modifier,
    navController: NavHostController,
    startDestination: String = "",
    paddingValue: PaddingValues,
    mainViewModel: MainViewModel,
) {

    NavHost(
        navController = navController,
        startDestination = startDestination,
    ) {
        composable(BottomNavItem.Home.route) {
            val parentEntry = remember(it) {
                navController.getBackStackEntry(BottomNavItem.Home.route)
            }

            val viewModel: HomeViewModel = hiltViewModel(parentEntry)
            val uiStateHome by viewModel.uiState.collectAsStateWithLifecycle()
            val screenState by viewModel.screenState.collectAsStateWithLifecycle()

            LaunchedEffect(Unit) {
                mainViewModel.retryEvent.collect {
                    viewModel.loadHomeState() // 본인의 데이터 로드
                }
            }

            HomeScreen(
                modifier = modifier,
                paddingValue = paddingValue,
                viewModel = viewModel,
                uiState = uiStateHome,
                screenState = screenState,
                onRetryApi = { viewModel.loadHomeState() },

                )
        }

        composable(BottomNavItem.Library.route) {
            val parentEntry =
                remember(it) { navController.getBackStackEntry(BottomNavItem.Library.route) }
            val viewModel: LibraryViewModel = hiltViewModel(parentEntry)
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()

            LaunchedEffect(Unit) {
                mainViewModel.retryEvent.collect {
                    viewModel.loadCalendarHistory(YearMonth.now()) // 현재 일자의 데이터 로드
                }
            }

            LibraryScreen(
                name = "LibraryScreen",
                viewModel = viewModel,
                uiState = uiState,
                onClickOtherTab = {},
                innerPadding = paddingValue,
            )
        }

    }
}


sealed class HomeScreens(val route: String) {
    data object HomeScreen : HomeScreens("home")
    data object LibraryScreen : HomeScreens("library")
}