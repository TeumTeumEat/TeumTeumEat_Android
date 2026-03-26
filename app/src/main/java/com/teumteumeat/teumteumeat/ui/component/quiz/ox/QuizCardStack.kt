package com.teumteumeat.teumteumeat.ui.component.quiz.ox

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp
import com.teumteumeat.teumteumeat.ui.component.quiz.multi_choice.QuizMultiChoiceCard
import com.teumteumeat.teumteumeat.ui.component.quiz.multi_choice.VerticalSlideQuizCardWrapper
import com.teumteumeat.teumteumeat.ui.screen.b2_quiz.QuizCardSection
import com.teumteumeat.teumteumeat.ui.screen.b2_quiz.QuizCardUiState
import com.teumteumeat.teumteumeat.ui.screen.b2_quiz.QuizType

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
        // 1. 다음에 올 카드 (배경 레이어)
        if (currentIndex + 1 < quizzes.size) {
            val nextQuiz = quizzes[currentIndex + 1]
            QuizCardSection(
                quiz = nextQuiz,
                questionIndex = currentIndex + 2,
                onSelectAnswer = {}, // 배경 카드는 클릭/스와이프 비활성화
                modifier = Modifier
                    .scale(0.9f)
                    .alpha(0.5f)
                    .offset(y = 10.dp) // 시각적 스택 효과
            )
        }

        // 2. 현재 활성화된 카드 (최상단 레이어)
        if (currentIndex < quizzes.size) {
            val currentQuiz = quizzes[currentIndex]

            // OX 타입일 때만 스와이프 래퍼를 적용하고, MCQ는 일반 섹션으로 표시
            when (currentQuiz.type) {
                QuizType.OX -> {
                    SwipeableQuizCardWrapper(
                        key = currentIndex,
                        onSelectAnswer = onAnswerSubmitted
                    ) { currentStatus, triggerYes, triggerNo -> // 람다 인자로 애니메이션 트리거를 받음
                        QuizOXCard(
                            questionIndex = currentIndex + 1,
                            question = currentQuiz.question,
                            isCardStatus = currentStatus,
                            // 🔹 중요: 바로 onAnswerSubmitted를 부르지 않고 래퍼의 트리거를 사용
                            onYes = triggerYes,
                            onNo = triggerNo
                        )
                    }
                }
                QuizType.MCQ -> {
                    // MCQ 타입일 때 위로 슬라이드 효과 적용
                    VerticalSlideQuizCardWrapper(
                        key = currentIndex,
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
                            }
                        )
                    }
                }
            }

        }
    }
}
