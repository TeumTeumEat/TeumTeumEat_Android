package com.teumteumeat.teumteumeat.ui.screen.b1_summary

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.teumteumeat.teumteumeat.ui.screen.a4_main.MainActivity
import com.teumteumeat.teumteumeat.ui.screen.b2_quiz.QuizActivity
import com.teumteumeat.teumteumeat.utils.LocalActivityContext
import com.teumteumeat.teumteumeat.utils.LocalScreenState
import com.teumteumeat.teumteumeat.utils.LocalSummaryUiState
import com.teumteumeat.teumteumeat.utils.LocalViewModelContext
import com.teumteumeat.teumteumeat.utils.Utils


sealed class SummaryRoute(val route: String) {

    data object Summary : SummaryRoute("summary")
    data object Guide : SummaryRoute("Guide")
}


@Composable
fun SummaryNavHost(
    navController: NavHostController
) {
    val activityContext = LocalActivityContext.current as SummaryActivity
    val viewModel = LocalViewModelContext.current as SummaryViewModel
    val uiState = LocalSummaryUiState.current
    val screenState = LocalScreenState.current

    NavHost(
        navController = navController,
        startDestination = SummaryRoute.Summary.route
    ) {


        composable(SummaryRoute.Summary.route) {
            SummaryScreen(
                onBackClick = {
                    Utils.UxUtils.moveActivity(
                        activityContext,
                        MainActivity::class.java,
                    )
                },
                onQuizClick = {
                    //  false -  가이드 화면으로 이동
                    //  true - 퀴즈 풀러 이동
                    if (!uiState.isQuizGuideSeen) {
                        navController.navigate(SummaryRoute.Guide.route)
                    } else {
                        Utils.UxUtils.moveActivity(
                            activityContext,
                            QuizActivity::class.java,
                            exitFlag = true
                        )
                    }
                },
                uiState = uiState,
                screenState = screenState,
                onSetIdleScreen = viewModel::resetIdleState,
                onRetryApi = viewModel::loadInitialData,
            )
        }

        composable(SummaryRoute.Guide.route) {
            GuideScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onQuizClick = {
                    viewModel.onQuizClick(uiState.isSkipQuizGuideChecked)
                },
                onCheckedChange = {
                    viewModel.updateSkipGuideSceneFlag(uiState.isSkipQuizGuideChecked)
                },
                isChecked = uiState.isSkipQuizGuideChecked
            )
        }
    }
}

