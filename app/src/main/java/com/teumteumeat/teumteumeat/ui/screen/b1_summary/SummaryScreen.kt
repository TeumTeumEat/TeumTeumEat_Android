package com.teumteumeat.teumteumeat.ui.screen.b1_summary

import android.widget.TextView
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBackIos
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.teumteumeat.teumteumeat.ui.component.MarkdownText
import com.teumteumeat.teumteumeat.ui.component.button.BaseFillButton
import com.teumteumeat.teumteumeat.utils.appTypography
import com.teumteumeat.teumteumeat.utils.extendedColors
import io.noties.markwon.Markwon

@Composable
fun SummaryScreen(
    uiState: UiStateSummary,
    onBackClick: () -> Unit,
    onQuizClick: () -> Unit
) {

    val theme = MaterialTheme.extendedColors
    val typography = MaterialTheme.appTypography

    BackHandler {
        onBackClick()
    }

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
                         * 홈화면 타이틀 바
                         */
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 19.dp)
                                .drawBehind {
                                    val strokeWidth = 1.dp.toPx()
                                    drawLine(
                                        color = theme.unableContainer, // 연한 회색
                                        start = Offset(0f, size.height - strokeWidth),
                                        end = Offset(size.width, size.height - strokeWidth),
                                        strokeWidth = strokeWidth
                                    )
                                },
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Start,
                        ) {
                            Row(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(vertical = 16.dp, horizontal = 20.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(
                                    onClick = { onBackClick() },
                                    modifier = Modifier.size(30.dp)
                                ) {
                                    Icon(
                                        modifier = Modifier.padding(0.dp),
                                        imageVector = Icons.AutoMirrored.Rounded.ArrowBackIos,
                                        contentDescription = "previous page"
                                    )
                                }

                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        "오늘의 냠냠지식",
                                        style = MaterialTheme.appTypography.subtitleSemiBold20,
                                    )
                                }
                            }
                        }

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

                    // 로딩
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center)
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
                        // todo. test 코드 개속 풀수 있게 설정
                        isEnabled = true,
                        // isEnabled = !uiState.hasSolvedToday,
                        text = "퀴즈 풀러가기"
                    )
                }
            }
        },
    )

}

private val previewUiState = UiStateSummary(
    isLoading = false,
    title = "휴리스틱 평가",
    dateText = "1월 3일",
    summary = """
        휴리스틱 평가는 사용성 테스트 기법 중 하나로,
        실제 사용자 대신 전문가가 인터페이스를 점검하여
        사용성 문제를 찾아내는 방법이다.

        닐슨이 제시한 10가지 휴리스틱 원칙을 기준으로
        화면 흐름, 기능 배치, 피드백 방식을 평가한다.
    """.trimIndent(),
    hasSolvedToday = false,
    errorMessage = null
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
        onQuizClick = {}
    )
}


