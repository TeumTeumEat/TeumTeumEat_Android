package com.teumteumeat.teumteumeat.ui.component.quiz.result

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.teumteumeat.teumteumeat.ui.component.DefaultMonoBg
import com.teumteumeat.teumteumeat.ui.component.header.TitleBar
import com.teumteumeat.teumteumeat.ui.screen.b3_quiz_result.QuizResultItem

@Composable
fun QuizResultBody(
    title: String,
    quizzes: List<QuizResultItem>,
    onBackClick: () -> Unit,
    bottomBar: @Composable BoxScope.() -> Unit,
) {
    DefaultMonoBg {
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
                    onBackClick = onBackClick
                )

                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(top = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(
                        top = 20.dp,
                        bottom = 96.dp // 버튼 높이 + 여유
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

            bottomBar()
        }
    }
}
