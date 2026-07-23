package com.teumteumeat.teumteumeat.ui.screen.b3_quiz_result

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.teumteumeat.teumteumeat.ui.component.DefaultMonoBg
import com.teumteumeat.teumteumeat.ui.component.MarkdownText
import com.teumteumeat.teumteumeat.ui.component.backgroundGradientBlue
import com.teumteumeat.teumteumeat.ui.component.bottomButtonScrimGradient
import com.teumteumeat.teumteumeat.ui.component.header.TitleBar
import com.teumteumeat.teumteumeat.ui.screen.b1_summary.UiStateSummary
import com.teumteumeat.teumteumeat.ui.theme.TeumTeumEatTheme
import com.teumteumeat.teumteumeat.utils.appTypography

@Composable
fun SummaryScreenForQuizResult(
    uiState: UiStateQuizResult,
    onBackClick: () -> Unit,
) {

    val typography = MaterialTheme.appTypography

    BackHandler {
        onBackClick()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundGradientBlue)
    ) {
        DefaultMonoBg(color = Color.Transparent) {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                containerColor = Color.Transparent,
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
                                    onBackClick = onBackClick,
                                    showDivider = false
                                )

                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .verticalScroll(rememberScrollState())
                                        .padding(horizontal = 20.dp)
                                ) {
                                    Spacer(modifier = Modifier.height(32.dp))

                                    Text(
                                        text = uiState.summary.title,
                                        style = MaterialTheme.appTypography.titleBold24
                                    )

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Text(
                                        text = uiState.summary.dateText,
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
                                            markdown = uiState.summary.summary,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }

                                    // 스크롤 끝에서 마지막 컨텐츠가 화면 하단에 붙지 않도록 여백 확보
                                    Spacer(modifier = Modifier.height(40.dp))
                                }
                            }

                            // 로딩
                            if (uiState.isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.align(Alignment.Center)
                                )
                            }

                            /** 🔹 하단 스크림 (오늘의 정답 확인 화면과 동일 그라데이션) */
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .align(Alignment.BottomCenter)
                                    .height(124.dp)
                                    .background(bottomButtonScrimGradient)
                            )
                        }
                    }
                },
            )
        }
    }
}

private val previewUiState = UiStateQuizResult(
    summary = UiStateSummary(
        title = "휴리스틱 평가란?",
        dateText = "2026년 7월 23일",
        summary = """
            휴리스틱 평가는 사용성 테스트 기법 중 하나로,
            실제 사용자 대신 전문가가 인터페이스를 점검하여
            사용성 문제를 찾아내는 방법이다.

            닐슨이 제시한 10가지 휴리스틱 원칙을 기준으로
            화면 흐름, 기능 배치, 피드백 방식을 평가한다.
        """.trimIndent(),
    ),
)

@Preview(showBackground = true)
@Composable
private fun SummaryScreenForQuizResultPreview() {
    TeumTeumEatTheme {
        SummaryScreenForQuizResult(
            uiState = previewUiState,
            onBackClick = {},
        )
    }
}
