package com.teumteumeat.teumteumeat.ui.screen.b2_quiz

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.teumteumeat.teumteumeat.ui.component.header.ProgressHeader
import com.teumteumeat.teumteumeat.ui.component.quiz.QuizCompleteCard
import com.teumteumeat.teumteumeat.ui.component.quiz.multi_choice.QuizMultiChoiceCard
import com.teumteumeat.teumteumeat.ui.component.quiz.ox.CardStatus
import com.teumteumeat.teumteumeat.ui.component.quiz.ox.QuizOXCard
import com.teumteumeat.teumteumeat.ui.screen.b2_1_quiz_result.QuizResultActivity
import com.teumteumeat.teumteumeat.utils.Utils

@Composable
fun QuizScreen(
    uiState: UiStateQuiz,
    onBackClick: () -> Unit,
    onSelectAnswer: (String) -> Unit
) {

    val context = LocalContext.current

    Box(modifier = Modifier
        .fillMaxSize()
        .systemBarsPadding()
    ) {

        when {
            // 1️⃣ 최초 로딩
            uiState.isLoading && uiState.quizzes.isEmpty() -> {
                Box(contentAlignment = Alignment.Center) {
                    Text(text = "퀴즈를 불러오는 중이에요…")
                }
            }

            // 2️⃣ 에러 상태
            uiState.errorMessage != null -> {
                Box(contentAlignment = Alignment.Center) {
                    Text(text = uiState.errorMessage)
                }
            }

            // 3️⃣ 퀴즈 없음
            uiState.currentQuiz == null -> {
                Box(contentAlignment = Alignment.Center) {
                    Text(text = "오늘의 퀴즈를 모두 풀었어요 🎉")
                }
            }

            uiState.isCompleted -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    QuizCompleteCard(
                        onButtonClick = {
                            Utils.UxUtils.moveActivity(context, QuizResultActivity::class.java, exitFlag = true)
                        }
                    )
                }
            }

            // 4️⃣ 정상 퀴즈 표시
            else -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    // 🔝 상단 헤더
                    ProgressHeader(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .fillMaxWidth(),
                        currentStep = uiState.currentStep,
                        totalSteps = uiState.totalSteps,
                        onBackClick = onBackClick
                    )
                    QuizCardSection(
                        modifier = Modifier.align(Alignment.Center),
                        quiz = uiState.currentQuiz!!,
                        questionIndex = uiState.currentIndex + 1,
                        onSelectAnswer = onSelectAnswer
                    )
                }
            }
        }

        // 🔄 카드 제출 중 오버레이
        if (uiState.currentQuiz?.isSubmitting == true) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.25f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "제출 중...",
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun QuizCardSection(
    quiz: QuizCardUiState,
    questionIndex: Int,
    onSelectAnswer: (String) -> Unit,
    modifier: Modifier
) {
    when (quiz.type) {

        QuizType.OX -> {
            QuizOXCard(
                modifier = modifier,
                questionIndex = questionIndex,
                question = quiz.question,
                isCardStatus = when (quiz.isCorrect) {
                    true -> CardStatus.Accept
                    false -> CardStatus.Reject
                    null -> CardStatus.Default
                },
                onYes = { onSelectAnswer("O") },
                onNo = { onSelectAnswer("X") }
            )
        }

        QuizType.MCQ -> {
            QuizMultiChoiceCard(
                modifier = modifier,
                questionIndex = questionIndex,
                question = quiz.question,
                options = quiz.options,
                selectedIndex = quiz.selectedAnswer
                    ?.let { quiz.options.indexOf(it) },
                onSelect = { index ->
                    onSelectAnswer(quiz.options[index])
                }
            )
        }
    }
}



