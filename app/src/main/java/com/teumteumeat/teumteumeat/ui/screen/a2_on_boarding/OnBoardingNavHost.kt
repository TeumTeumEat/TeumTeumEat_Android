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
                    navController.navigate(OnBoardingScreens.SecondInputNameScreen.route)
                },
            )
        }

        composable(
            route = OnBoardingScreens.SecondInputNameScreen.route,
        ) {
            OnBoardingSetCharNameScreen(
                onNext = {
                    viewModel.nextPage()
                    navController.navigate(OnBoardingScreens.ThirdSetAppTimeScreen.route)
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
            route = OnBoardingScreens.ThirdSetAppTimeScreen.route,
        ) {
            OnBoardingSetApptimeScreen(
                onNext = {
                    viewModel.nextPage()
                    // 4번째 화면 이동 로직 구현
                     navController.navigate(OnBoardingScreens.FourthSetUsingAppTimeScreen.route)
                },

                onPrev = {
                    navController.navigate(OnBoardingScreens.SecondInputNameScreen.route) {
                        viewModel.prevPage()
                        popUpTo(OnBoardingScreens.SecondInputNameScreen.route) { inclusive = true }
                    }
                },
                name = "set_app_time",
                viewModel = viewModel,
                uiState = uiState,
            )
        }

        // 4️⃣ (NEW) 앱 사용 시간 관련 설정 화면
        composable(
            route = OnBoardingScreens.FourthSetUsingAppTimeScreen.route
        ) {
            OnBoardingSetUsingApptimeScreen(
                onNext = {
                    viewModel.nextPage()
                    // 👉 다음 온보딩 화면 or 완료 화면으로 이동
                    // navController.navigate(OnBoardingScreens.FifthScreen.route)
                },
                onPrev = {
                    viewModel.prevPage()
                    navController.popBackStack()
                },
                name = "set_using_app_time",
                viewModel = viewModel,
                uiState = uiState,
            )
        }
    }
}

sealed class OnBoardingScreens(val route: String) {
    data object FirstScreen : OnBoardingScreens("welcome")
    data object SecondInputNameScreen : OnBoardingScreens("input_name")
    data object ThirdSetAppTimeScreen : OnBoardingScreens("set_app_time")
    data object FourthSetUsingAppTimeScreen :
        OnBoardingScreens("set_using_app_time")
}