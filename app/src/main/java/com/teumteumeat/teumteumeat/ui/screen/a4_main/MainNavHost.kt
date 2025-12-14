package com.teumteumeat.teumteumeat.ui.screen.a4_main

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.teumteumeat.teumteumeat.ui.screen.a4_main.a4_1_home.HomeScreen
import com.teumteumeat.teumteumeat.ui.screen.a4_main.a4_1_home.HomeViewModel
import com.teumteumeat.teumteumeat.ui.screen.a4_main.a4_2_library.LibraryScreen
import com.teumteumeat.teumteumeat.ui.screen.a4_main.a4_2_library.LibraryViewModel


@Composable
fun MainNavHost(navController: NavHostController, startDestination: String, modifier: Modifier) {

    val context = LocalContext.current

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
    ) {
        composable(BottomNavItem.Home.route) {
            val parentEntry = remember(it) {
                navController.getBackStackEntry(BottomNavItem.Home.route)
            }

            val viewModel: HomeViewModel = viewModel(parentEntry)
            val uiStateHome by viewModel.uiState.collectAsStateWithLifecycle()

            HomeScreen(
                name = "HomeScreen",
                viewModel = viewModel,
                uiState = uiStateHome,
                onTabOther = {},
            )
        }

        composable(BottomNavItem.Library.route) {
            val parentEntry =
                remember(it) { navController.getBackStackEntry(BottomNavItem.Library.route) }
            val viewModel: LibraryViewModel = viewModel(parentEntry)
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()

            LibraryScreen(
                name = "LibraryScreen",
                viewModel = viewModel,
                uiState = uiState,
                onClickOtherTab = {},
            )
        }

        composable(BottomNavItem.AddingFile.route) {
            // todo. 추후 프로세스에 맞춰 화면 작업하기
            //  1. 액티비티 일시 버튼클릭 이벤트 -> 액티비티로 이동
            //  2. 프레그먼트일시 새로운 화면 추가
           /* val parentEntry =
                remember(it) { navController.getBackStackEntry(BottomNavItem.Library.route) }
            val viewModel: LibraryViewModel = viewModel(parentEntry)
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()

            LibraryScreen(
                name = "LibraryScreen",
                viewModel = viewModel,
                uiState = uiState,
                onClickOtherTab = {},
            )*/
        }
    }
}


sealed class HomeScreens(val route: String) {
    data object HomeScreen : HomeScreens("home")
    data object LibraryScreen : HomeScreens("library")
}