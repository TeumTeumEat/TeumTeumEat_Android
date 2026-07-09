package com.teumteumeat.teumteumeat.ui.component.quiz.ox

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.teumteumeat.teumteumeat.ui.component.quiz.QuizNextCardPreview
import com.teumteumeat.teumteumeat.ui.component.quiz.QuizStackBackdrop
import com.teumteumeat.teumteumeat.ui.component.quiz.calculateStackDepth
import com.teumteumeat.teumteumeat.ui.component.quiz.multi_choice.QuizMultiChoiceCard
import com.teumteumeat.teumteumeat.ui.component.quiz.multi_choice.VerticalSlideQuizCardWrapper
import com.teumteumeat.teumteumeat.ui.screen.b2_quiz.QuizCardUiState
import com.teumteumeat.teumteumeat.ui.screen.b2_quiz.QuizType
import kotlin.math.abs

/** 다음 카드가 완전히 선명해지는 데 필요한 스와이프/슬라이드 이동 거리(px). */
private const val REVEAL_DISTANCE_PX = 300f

@Composable
fun QuizCardStack(
    quizzes: List<QuizCardUiState>,
    currentIndex: Int,
    onAnswerSubmitted: (String) -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        if (currentIndex < quizzes.size) {
            val currentQuiz = quizzes[currentIndex]
            val hasNext = currentIndex + 1 < quizzes.size
            val remainingAfterCurrent = quizzes.size - currentIndex - 1
            val stackDepth = calculateStackDepth(remainingAfterCurrent)

            // 1. 정적인 먼 레이어들 (최상단 1장은 QuizNextCardPreview가 담당)
            QuizStackBackdrop(stackDepth = (stackDepth - 1).coerceAtLeast(0))

            when (currentQuiz.type) {
                QuizType.OX -> {
                    val offsetX = remember(currentIndex) { Animatable(0f) }
                    val dismissProgress = (abs(offsetX.value) / REVEAL_DISTANCE_PX).coerceIn(0f, 1f)

                    if (hasNext) {
                        QuizNextCardPreview(
                            progress = dismissProgress,
                            nextQuiz = quizzes[currentIndex + 1],
                            nextQuestionIndex = currentIndex + 2,
                        )
                    }

                    SwipeableQuizCardWrapper(
                        key = currentIndex,
                        offsetX = offsetX,
                        onSelectAnswer = onAnswerSubmitted
                    ) { currentStatus, triggerYes, triggerNo -> // 람다 인자로 애니메이션 트리거를 받음
                        QuizOXCard(
                            questionIndex = currentIndex + 1,
                            question = currentQuiz.question,
                            isCardStatus = currentStatus,
                            // 🔹 중요: 바로 onAnswerSubmitted를 부르지 않고 래퍼의 트리거를 사용
                            onYes = triggerYes,
                            onNo = triggerNo,
                        )
                    }
                }
                QuizType.MCQ -> {
                    val offsetY = remember(currentIndex) { Animatable(0f) }
                    val dismissProgress = (offsetY.value / REVEAL_DISTANCE_PX).coerceIn(0f, 1f)

                    if (hasNext) {
                        QuizNextCardPreview(
                            progress = dismissProgress,
                            nextQuiz = quizzes[currentIndex + 1],
                            nextQuestionIndex = currentIndex + 2,
                        )
                    }

                    // MCQ 타입일 때 위로 슬라이드 효과 적용
                    VerticalSlideQuizCardWrapper(
                        offsetY = offsetY,
                        onSelectAnswer = onAnswerSubmitted
                    ) { triggerSelect ->
                        QuizMultiChoiceCard(
                            modifier = Modifier.fillMaxWidth(),
                            questionIndex = currentIndex + 1,
                            question = currentQuiz.question,
                            options = currentQuiz.options,
                            selectedIndex = currentQuiz.selectedAnswer?.let { currentQuiz.options.indexOf(it) },
                            onSelect = { index ->
                                // 선택 시 애니메이션 트리거 호출
                                triggerSelect(currentQuiz.options[index])
                            },
                            onPass = {
                                triggerSelect("")
                            },
                        )
                    }
                }
            }

        }
    }
}
