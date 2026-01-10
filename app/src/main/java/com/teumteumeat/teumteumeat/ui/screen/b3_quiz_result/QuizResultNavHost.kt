package com.teumteumeat.teumteumeat.ui.screen.b3_quiz_result

import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.teumteumeat.teumteumeat.ui.screen.a4_main.MainActivity
import com.teumteumeat.teumteumeat.ui.screen.a4_main.MainArgs
import com.teumteumeat.teumteumeat.ui.screen.a4_main.MainScreenType
import com.teumteumeat.teumteumeat.utils.LocalActivityContext
import com.teumteumeat.teumteumeat.utils.LocalQuizResultUiState
import com.teumteumeat.teumteumeat.utils.LocalViewModelContext
import com.teumteumeat.teumteumeat.utils.Utils


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
                onCloseClick = { Utils.UxUtils.moveActivity(activity, MainActivity::class.java, exitFlag = true) },
                goHistory = {
                    val intent = Intent(activity, MainActivity::class.java).apply {
                        putExtra(
                            MainArgs.KEY_TARGET_SCREEN,
                            MainScreenType.LIBRARY.name
                        )
                    }
                    activity.startActivity(intent)
                    activity.finish()
                }
            )
        }
    }
}

