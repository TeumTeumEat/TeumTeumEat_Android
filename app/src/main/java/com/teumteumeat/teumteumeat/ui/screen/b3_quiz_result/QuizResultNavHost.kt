package com.teumteumeat.teumteumeat.ui.screen.b3_quiz_result

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.teumteumeat.teumteumeat.utils.LocalActivityContext
import com.teumteumeat.teumteumeat.utils.LocalQuizResultUiState
import com.teumteumeat.teumteumeat.utils.LocalViewModelContext


sealed class QuizResultRoute(val route: String) {

    data object QuizResult : QuizResultRoute("quiz_result")
    data object QuizFinish : QuizResultRoute("quiz_finish")
    data object Summary : QuizResultRoute("summary")
    data object QuizEnding : QuizResultRoute("quiz_ending")
}


@Composable
fun QuizResultNavHost(
    navController: NavHostController
) {
    val context = LocalContext.current
    val activity = LocalActivityContext.current
    val viewModel = LocalViewModelContext.current as QuizResultViewModel
    val uiState = LocalQuizResultUiState.current

    NavHost(
        navController = navController,
        startDestination = QuizResultRoute.QuizFinish.route
    ) {

        composable(QuizResultRoute.QuizFinish.route) {
            QuizFinishScreen(
                correctCount = uiState.correctCount,
                onCloseClick = {
                    // todo. 홈화면으로 이동
                    activity.finish()
                },
                onNextClick = {
                    // todo. 퀴즈 상세 결과 화면으로 이동
                    navController.navigate(QuizResultRoute.QuizResult.route)
                }
            )
        }

        composable(QuizResultRoute.QuizResult.route) {
            QuizResultScreen(
                onNext = {
                    navController.navigate(QuizResultRoute.QuizFinish.route)
                },
                onBack = { navController.popBackStack() },
                onShowSummary = {
                    navController.navigate(QuizResultRoute.Summary.route)
                },
                goEndScreen = {
                    navController.navigate(QuizResultRoute.QuizEnding.route)
                },

            )
        }

        composable(QuizResultRoute.Summary.route) {
            SummaryScreenForQuizResult(
                uiState = uiState,
                onBackClick = { navController.popBackStack() },
            )
        }

        composable(QuizResultRoute.QuizEnding.route) {
            QuizEndingScreen(
                onCloseClick = { activity.finish() },
                goHistory = {
                    activity.finish()
                    // todo. 추후에 홈화면으로 이동 후 히스토리 화면으로 이동
                }
            )
        }
    }
}

