package com.teumteumeat.teumteumeat.ui.screen.a4_main.a4_4_daily_quiz_result

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.teumteumeat.teumteumeat.ui.component.FullScreenErrorModal
import com.teumteumeat.teumteumeat.ui.component.button.BaseFillButton
import com.teumteumeat.teumteumeat.ui.component.quiz.result.QuizResultBody
import com.teumteumeat.teumteumeat.ui.screen.a1_login.LoginActivity
import com.teumteumeat.teumteumeat.ui.screen.common_screen.ErrorState
import com.teumteumeat.teumteumeat.ui.screen.common_screen.LoadingScreen
import com.teumteumeat.teumteumeat.ui.screen.common_screen.UiScreenState
import com.teumteumeat.teumteumeat.utils.LocalActivityContext
import com.teumteumeat.teumteumeat.utils.LocalViewModelContext
import com.teumteumeat.teumteumeat.utils.Utils
import kotlinx.coroutines.flow.collectLatest

@Composable
fun DailyQuizResultScreen(
    uiState: UiStateDailyQuizResult,
    screenState: UiScreenState,
    onBack: () -> Unit = {},
    onViewSummaryClick: () -> Unit = {},
) {
    val viewModel = LocalViewModelContext.current as DailyQuizResultViewModel
    val activity = LocalActivityContext.current as DailyQuizResultActivity

    val sessionManager = viewModel.sessionManager // 세션메니저 정의

    // 🔥 전역 세션 이벤트 감지
    LaunchedEffect(Unit) {
        sessionManager.sessionEvent.collectLatest {
            Utils.UxUtils.moveActivity(activity, LoginActivity::class.java, clearTask = true)
        }
    }

    BackHandler {
        onViewSummaryClick()
    }

    when (screenState) {
        is UiScreenState.Error -> {
            FullScreenErrorModal(
                errorState = ErrorState(
                    title = "에러가 발생했습니다.",
                    description = screenState.message,
                    retryLabel = "다시 시도하기",
                    onRetry = { viewModel.loadQuizResults() }
                ),
                onBack = onViewSummaryClick,
            )
        }

        UiScreenState.Idle, UiScreenState.Loading -> {
            LoadingScreen(
                title = "퀴즈 결과를 불러오는 중",
                message = "잠시만 기다려주세요",
            )
        }

        UiScreenState.Success -> {
            QuizResultBody(
                title = "오늘의 정답 확인",
                quizzes = uiState.quizzes,
                onBackClick = onBack,
            ) {
                /** 🔹 하단 버튼 영역 */
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                        .align(Alignment.BottomCenter),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    BaseFillButton(
                        modifier = Modifier
                            .weight(1f),
                        onClick = onViewSummaryClick,
                        text = "요약글 보기"
                    )
                }
            }
        }
    }
}

