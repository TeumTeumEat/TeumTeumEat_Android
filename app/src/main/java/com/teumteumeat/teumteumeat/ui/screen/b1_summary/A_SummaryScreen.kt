package com.teumteumeat.teumteumeat.ui.screen.b1_summary

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.teumteumeat.teumteumeat.ui.component.FullScreenErrorModal
import com.teumteumeat.teumteumeat.ui.component.MarkdownText
import com.teumteumeat.teumteumeat.ui.component.button.BaseFillButton
import com.teumteumeat.teumteumeat.ui.component.header.TitleBar
import com.teumteumeat.teumteumeat.ui.screen.a4_main.MainActivity
import com.teumteumeat.teumteumeat.ui.screen.common_screen.ErrorState
import com.teumteumeat.teumteumeat.ui.screen.common_screen.UiScreenState
import com.teumteumeat.teumteumeat.utils.LocalActivityContext
import com.teumteumeat.teumteumeat.utils.LocalViewModelContext
import com.teumteumeat.teumteumeat.utils.Utils
import com.teumteumeat.teumteumeat.utils.appTypography
import com.teumteumeat.teumteumeat.utils.extendedColors
import androidx.compose.runtime.getValue
import com.teumteumeat.teumteumeat.BuildConfig
import com.teumteumeat.teumteumeat.ui.component.DefaultMonoBg
import com.teumteumeat.teumteumeat.ui.screen.common_screen.GoalLoadingScreen
import com.teumteumeat.teumteumeat.utils.LocalScreenState

@Composable
fun SummaryScreen(
    uiState: UiStateSummary,
    screenState: UiScreenState,
    onBackClick: () -> Unit,
    onQuizClick: () -> Unit,
    onSetIdleScreen: () -> Unit,
    onRetryApi: () -> Unit
) {

    val theme = MaterialTheme.extendedColors
    val typography = MaterialTheme.appTypography
    val viewModel = LocalViewModelContext.current as SummaryViewModel
    val context = LocalActivityContext.current as SummaryActivity
    val processingState by viewModel.processingState.collectAsStateWithLifecycle()
    val screenState = LocalScreenState.current

    BackHandler {
        onBackClick()
    }

    // 🔴 에러 화면 (핵심)
    if (screenState is UiScreenState.Error) {
        val errorMessage =
            (screenState as UiScreenState.Error).message

        FullScreenErrorModal(
            errorState = ErrorState(
                title = "에러가 발생했습니다.",
                description = errorMessage,
                retryLabel = "다시 시도하기",
                onRetry = onRetryApi
            ),
            onBack = { Utils.UxUtils.moveActivity(context, MainActivity::class.java) },
        )
    } else {
        DefaultMonoBg() {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                content = { padding ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .systemBarsPadding()
                            .padding(),
                        verticalArrangement = Arrangement.SpaceBetween,
                    ) {

                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                        ) {

                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                            ) {
                                /**
                                 * 타이틀 바
                                 */
                                TitleBar(
                                    title = "오늘의 냠냠지식",
                                    onBackClick = { onBackClick() }
                                )

                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .verticalScroll(rememberScrollState())
                                        .padding(horizontal = 20.dp)
                                ) {

                                    Spacer(modifier = Modifier.height(32.dp))

                                    Text(
                                        text = uiState.title,
                                        style = MaterialTheme.appTypography.titleBold24
                                    )

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Text(
                                        text = uiState.dateText,
                                        style = MaterialTheme.appTypography.captionRegular12,
                                        color = Color.Gray
                                    )

                                    Spacer(modifier = Modifier.height(24.dp))

                                    if (uiState.errorMessage != null) {
                                        Text(
                                            text = uiState.errorMessage,
                                            color = Color.Red,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    } else {
                                        MarkdownText(
                                            markdown = uiState.summary,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(110.dp))
                                }
                            }

                            // 🔵 로딩 화면
                            if (screenState is UiScreenState.Loading) {
                                GoalLoadingScreen(
                                    title = "요약글을 생성하는 중",
                                    message = "잠시만 기다려주세요...",
                                    progress = processingState?.progress
                                )
                            }


                            // 2️⃣ 하단 그라데이션 (페이드 효과)
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(170.dp)
                                    .align(Alignment.BottomCenter)
                                    .background(
                                        brush = Brush.verticalGradient(
                                            colors = listOf(
                                                Color.Transparent,
                                                MaterialTheme.colorScheme.surface,
                                            )
                                        )
                                    )
                            )

                            // 하단 퀴즈 버튼
                            BaseFillButton(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(20.dp)
                                    .fillMaxWidth(),
                                onClick = onQuizClick,
                                isEnabled = screenState !is UiScreenState.Loading,
                                text = if ((screenState is UiScreenState.Loading)) {
                                    "잠시만 기다려주세요"
                                } else "퀴즈 풀러가기"
                            )
                        }
                    }
                },
            )
        }
    }


}

private val previewUiState = UiStateSummary(
    summary = """
        휴리스틱 평가는 사용성 테스트 기법 중 하나로,
        실제 사용자 대신 전문가가 인터페이스를 점검하여
        사용성 문제를 찾아내는 방법이다.

        닐슨이 제시한 10가지 휴리스틱 원칙을 기준으로
        화면 흐름, 기능 배치, 피드백 방식을 평가한다.
    """.trimIndent(),
    isLoading = true
)

@Preview(
    showBackground = true,
    showSystemUi = true
)

@Composable
fun SummaryScreenPreview() {
    SummaryScreen(
        uiState = previewUiState,
        onBackClick = {},
        onQuizClick = {},
        screenState = UiScreenState.Idle,
        onSetIdleScreen = {},
        onRetryApi = { }
    )
}


