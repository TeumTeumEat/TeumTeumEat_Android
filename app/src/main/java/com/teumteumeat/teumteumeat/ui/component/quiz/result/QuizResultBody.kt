package com.teumteumeat.teumteumeat.ui.component.quiz.result

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.teumteumeat.teumteumeat.ui.component.DefaultMonoBg
import com.teumteumeat.teumteumeat.ui.component.header.TitleBar
import com.teumteumeat.teumteumeat.ui.component.topScrimGradient
import com.teumteumeat.teumteumeat.ui.screen.b3_quiz_result.QuizResultItem
import com.teumteumeat.teumteumeat.utils.extendedColors

@Composable
fun QuizResultBody(
    title: String,
    quizzes: List<QuizResultItem>,
    onBackClick: () -> Unit,
    backgroundColor: Color = MaterialTheme.extendedColors.backSurface,
    bottomBar: @Composable BoxScope.() -> Unit,
) {
    DefaultMonoBg(color = backgroundColor) {
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
                TitleBar(
                    title = title,
                    onBackClick = onBackClick,
                    showDivider = false
                )

                Box(
                    modifier = Modifier.weight(1f)
                ) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(
                            top = 16.dp, // 상단 스크림(20dp) + 첫 카드 여백(16dp)
                            bottom = 144.dp // 버튼 높이 + 상하 32dp 스크림 여백(128dp) + 마지막 카드 여백(16dp)
                        )
                    ) {
                        itemsIndexed(quizzes) { index, quiz ->
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
            }

            bottomBar()
        }
    }
}
