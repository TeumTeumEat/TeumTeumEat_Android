package com.teumteumeat.teumteumeat.ui.screen.b3_quiz_result

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBackIos
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.teumteumeat.teumteumeat.ui.component.button.BaseFillButton
import com.teumteumeat.teumteumeat.ui.component.quiz.result.QuizResultCard
import com.teumteumeat.teumteumeat.ui.component.quiz.result.QuizResultType
import com.teumteumeat.teumteumeat.utils.LocalQuizResultUiState
import com.teumteumeat.teumteumeat.utils.appTypography
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 24.dp)
        ) {

            /** 🔹 상단 타이틀 영역 */
            TopBar(
                title = "오늘의 정답 확인",
                onBack = onBack
            )

            /** 🔹 퀴즈 결과 리스트 */
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(top = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(
                    top = 20.dp,
                    bottom = 96.dp // ⭐ 버튼 높이 + 여유
                )
            ) {
                itemsIndexed(uiState.quizzes) { index, quiz ->

                    QuizResultCard(
                        questionIndex = index + 1,
                        title = quiz.question,
                        answer = quiz.answer,
                        explanation = quiz.explanation,
                        resultType =
                            if (quiz.isCorrect)
                                QuizResultType.CORRECT
                            else
                                QuizResultType.WRONG
                    )
                }
            }
        }

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
                btnContainerColor = MaterialTheme.extendedColors.buttonFillSecondary,
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

@Composable
private fun TopBar(
    title: String,
    onBack: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        IconButton(
            onClick = onBack,
            modifier = Modifier.size(30.dp)
        ) {
            Icon(
                modifier = Modifier.padding(0.dp)
                    .size(24.dp),
                imageVector = Icons.AutoMirrored.Rounded.ArrowBackIos,
                contentDescription = "previous page",
            )
        }


        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ){
            Text(
                text = title,
                style = MaterialTheme.appTypography.subtitleSemiBold20
            )
        }
    }
}

