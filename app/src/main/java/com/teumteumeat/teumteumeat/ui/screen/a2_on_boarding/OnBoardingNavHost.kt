package com.teumteumeat.teumteumeat.ui.screen.a2_on_boarding

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.teumteumeat.teumteumeat.utils.LocalViewModelContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.teumteumeat.teumteumeat.utils.LocalOnBoardingMainUiState


@Composable
fun OnBoardingNavHost(navController: NavHostController) {
    val context = LocalContext.current
    val viewModel = LocalViewModelContext.current as OnBoardingViewModel
    val uiState = LocalOnBoardingMainUiState.current

    NavHost(navController = navController, startDestination = OnBoardingScreens.FirstScreen.route) {

        composable(
            route = OnBoardingScreens.FirstScreen.route,
        ) {
            OnBoardingFirstScreen(
                name = "OnBoardingFirst",
                viewModel = viewModel,
                uiState = uiState,
                onNext = {
                    viewModel.nextPage()
                    navController.navigate(OnBoardingScreens.InputNameScreen.route)
                },
            )
        }

        composable(
            route = OnBoardingScreens.InputNameScreen.route,
        ) {
            OnBoardingSetCharNameScreen(
                onNext = {
                    viewModel.nextPage()
                    navController.navigate(OnBoardingScreens.ThirdScreen.route)
                },

                onPrev = {
                    navController.navigate(OnBoardingScreens.FirstScreen.route) {
                        viewModel.prevPage()
                        popUpTo(OnBoardingScreens.FirstScreen.route) { inclusive = true }
                    }
                },
                name = "InputName",
                viewModel = viewModel,
                uiState = uiState,
            )
        }

        composable(
            route = OnBoardingScreens.InputNameScreen.route,
        ) {
            OnBoardingSetCharNameScreen(
                onNext = {
                    viewModel.nextPage()
                    navController.navigate(OnBoardingScreens.ThirdScreen.route)
                },

                onPrev = {
                    navController.navigate(OnBoardingScreens.FirstScreen.route) {
                        viewModel.prevPage()
                        popUpTo(OnBoardingScreens.FirstScreen.route) { inclusive = true }
                    }
                },
                name = "OnBoardingSecond",
                viewModel = viewModel,
                uiState = uiState,
            )
        }

        /*composable(AddInfoScreens.AddInfoComplete.route) {
            AddInfoCompleteScreen(
                onFinish = {
                    Utils.UxUtils.moveActivity(context, LevelExamStartActivity::class.java, exitFlag = true)
                    // navController.popBackStack(AddInfoScreens.SelectExamMonth.route, inclusive = true)
                }
            )
        }*/
    }
}

sealed class OnBoardingScreens(val route: String) {
    data object FirstScreen : OnBoardingScreens("select_user_default_lang")
    data object InputNameScreen : OnBoardingScreens("input_name")
    data object ThirdScreen : OnBoardingScreens("add_info_complete")
}