package com.teumteumeat.teumteumeat.ui.screen.b3_quiz_result

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.teumteumeat.teumteumeat.ui.component.button.BaseFillButton
import com.teumteumeat.teumteumeat.ui.component.quiz.result.QuizResultBody
import com.teumteumeat.teumteumeat.ui.theme.TeumTeumEatTheme
import com.teumteumeat.teumteumeat.utils.LocalQuizResultUiState
import com.teumteumeat.teumteumeat.utils.extendedColors

@Composable
fun QuizResultScreen(
    onBack: () -> Unit = {},
    onShowSummary: () -> Unit = {},
    onNext: () -> Unit,
    goEndScreen: () -> Unit,
    goSummaryScreen: () -> Unit = {},
) {
    val uiState = LocalQuizResultUiState.current

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
                onClick = onShowSummary,
                text = "글보기",
                btnContainerColor = MaterialTheme.extendedColors.btnFillSecondary,
                btnContentColor = MaterialTheme.extendedColors.textPointBlue
            )

            BaseFillButton(
                modifier = Modifier
                    .weight(1f),
                onClick = goEndScreen,
                text = "다음으로"
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun QuizResultScreenPreview(){
    TeumTeumEatTheme {
        CompositionLocalProvider(
            LocalQuizResultUiState provides UiStateQuizResult(
                quizzes = listOf(
                    QuizResultItem(
                        quizId = 1,
                        question = "탄수화물은 몸에서 주로 어떤 역할을 하나요?",
                        options = listOf("에너지 공급", "뼈 형성", "호르몬 합성", "수분 조절"),
                        answer = "에너지 공급",
                        explanation = "탄수화물은 우리 몸의 주요 에너지원으로, 1g당 4kcal의 에너지를 공급합니다.",
                        isCorrect = true,
                        type = "MULTIPLE_CHOICE"
                    ),
                    QuizResultItem(
                        quizId = 2,
                        question = "단백질이 부족할 때 나타날 수 있는 증상은?",
                        options = listOf("근육량 감소", "혈압 상승", "시력 저하", "수면 과다"),
                        answer = "근육량 감소",
                        explanation = "단백질은 근육 합성에 필수적이므로, 부족 시 근육량이 감소할 수 있습니다.",
                        isCorrect = false,
                        type = "MULTIPLE_CHOICE"
                    ),
                    QuizResultItem(
                        quizId = 3,
                        question = "식이섬유가 풍부한 식품은?",
                        options = listOf("고구마", "닭가슴살", "달걀", "치즈"),
                        answer = "고구마",
                        explanation = "고구마는 식이섬유가 풍부하여 장 건강과 포만감 유지에 도움을 줍니다.",
                        isCorrect = true,
                        type = "MULTIPLE_CHOICE"
                    )
                ),
                correctCount = 2,
                createdAt = "2026-04-22"
            )
        ) {
            QuizResultScreen(
                onBack = {},
                onShowSummary = {},
                onNext = {},
                goEndScreen = {

                }
            )
        }
    }
}
