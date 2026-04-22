package com.teumteumeat.teumteumeat.ui.screen.b3_quiz_result

import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.teumteumeat.teumteumeat.ui.screen.a1_login.LoginActivity
import com.teumteumeat.teumteumeat.ui.screen.a4_main.MainActivity
import com.teumteumeat.teumteumeat.ui.screen.a4_main.MainArgs
import com.teumteumeat.teumteumeat.ui.screen.a4_main.MainScreenType
import com.teumteumeat.teumteumeat.ui.screen.a4_main.a4_5_add_goal.AddGoalActivity
import com.teumteumeat.teumteumeat.utils.LocalActivityContext
import com.teumteumeat.teumteumeat.utils.LocalQuizResultUiState
import com.teumteumeat.teumteumeat.utils.LocalScreenState
import com.teumteumeat.teumteumeat.utils.LocalViewModelContext
import com.teumteumeat.teumteumeat.utils.Utils
import kotlinx.coroutines.flow.collectLatest
import kotlin.jvm.java



sealed class QuizResultRoute(val route: String) {

    data object QuizResult : QuizResultRoute("quiz_result")
    data object QuizFinish : QuizResultRoute("quiz_finish")
    data object Summary : QuizResultRoute("summary")
    data object QuizEnding : QuizResultRoute("quiz_ending")
    data object SubjectComplete : QuizResultRoute("subject_complete")
}


@Composable
fun QuizResultNavHost(
    navController: NavHostController
) {
    val context = LocalContext.current
    val activity = LocalActivityContext.current
    val viewModel = LocalViewModelContext.current as QuizResultViewModel
    val uiState = LocalQuizResultUiState.current
    val screenState = LocalScreenState.current

    val sessionManager = viewModel.sessionManager // 세션메니저 정의

    // 🔥 전역 세션 이벤트 감지
    LaunchedEffect(Unit) {
        sessionManager.sessionEvent.collectLatest {
            Utils.UxUtils.moveActivity(activity, LoginActivity::class.java)
        }
    }

    NavHost(
        navController = navController,
        startDestination = QuizResultRoute.QuizFinish.route
    ) {

        composable(QuizResultRoute.QuizFinish.route) {
            QuizFinishScreen(
                correctCount = uiState.correctCount,
                onCloseClick = {
                    // todo. 클릭시 전면광고 뜨게하기
                    //  로딩시에는 화면 클릭 못하게 막기
                    Utils.UxUtils.moveActivity(activity, MainActivity::class.java)
                },
                onNextClick = {
                    navController.navigate(QuizResultRoute.QuizResult.route)
                },
                screenState = screenState,
                onRetryApi = viewModel::initQuizResult,
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
                    if (uiState.userGoal?.isCompleted == true) {
                        navController.navigate(QuizResultRoute.SubjectComplete.route)
                    } else {
                        navController.navigate(QuizResultRoute.QuizEnding.route)
                    }
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
                onCloseClick = { Utils.UxUtils.moveActivity(activity, MainActivity::class.java) },
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

        composable(QuizResultRoute.SubjectComplete.route){
            SubjectCompleteScreen(
                onGoHomeClick = { activity.finish() },
                onStartNewSubjectClick = { Utils.UxUtils.moveActivity(activity, AddGoalActivity::class.java) },
            )
        }

    }
}

