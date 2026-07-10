package com.teumteumeat.teumteumeat.ui.component.quiz

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.teumteumeat.teumteumeat.ui.component.quiz.multi_choice.QuizMultiChoiceCard
import com.teumteumeat.teumteumeat.ui.component.quiz.ox.QuizOXCard
import com.teumteumeat.teumteumeat.ui.screen.b2_quiz.QuizCardUiState
import com.teumteumeat.teumteumeat.ui.screen.b2_quiz.QuizType

/**
 * 스택 최상단 자리에 놓이는 "다음 문제" 슬롯.
 * [progress]가 0f(평소, 회색 스택)에서 1f(다음 카드가 완전히 선명해짐)로 움직이는 동안
 * 회색 데코레이션과 실제 다음 문제 카드를 크로스페이드한다.
 */
@Composable
fun QuizNextCardPreview(
    progress: Float,
    nextQuiz: QuizCardUiState,
    nextQuestionIndex: Int,
) {
    Box(contentAlignment = Alignment.Center) {
        QuizStackBackdrop(
            stackDepth = 1,
            modifier = Modifier.graphicsLayer { alpha = 1f - progress },
        )

        if (progress > 0f) {
            val revealModifier = Modifier.graphicsLayer {
                alpha = progress
                translationY = -6.dp.toPx() * (1f - progress)
                val scale = 0.96f + 0.04f * progress
                scaleX = scale
                scaleY = scale
            }

            when (nextQuiz.type) {
                QuizType.OX -> QuizOXCard(
                    modifier = revealModifier,
                    questionIndex = nextQuestionIndex,
                    question = nextQuiz.question,
                    onYes = {},
                    onNo = {},
                )
                QuizType.MCQ -> QuizMultiChoiceCard(
                    modifier = revealModifier,
                    questionIndex = nextQuestionIndex,
                    question = nextQuiz.question,
                    options = nextQuiz.options,
                    selectedIndex = null,
                    onSelect = {},
                    onPass = {},
                )
            }
        }
    }
}
