package com.teumteumeat.teumteumeat.ui.screen.b2_quiz

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import com.teumteumeat.teumteumeat.ui.component.DefaultMonoBg
import com.teumteumeat.teumteumeat.ui.component.FullScreenErrorModal
import com.teumteumeat.teumteumeat.ui.component.header.ProgressHeader
import com.teumteumeat.teumteumeat.ui.component.modal.BaseModal
import com.teumteumeat.teumteumeat.ui.component.quiz.QuizCompleteCard
import com.teumteumeat.teumteumeat.ui.component.quiz.multi_choice.QuizMultiChoiceCard
import com.teumteumeat.teumteumeat.ui.component.quiz.ox.CardStatus
import com.teumteumeat.teumteumeat.ui.component.quiz.ox.QuizCardStack
import com.teumteumeat.teumteumeat.ui.component.quiz.ox.QuizOXCard
import com.teumteumeat.teumteumeat.ui.screen.common_screen.ErrorState
import com.teumteumeat.teumteumeat.ui.screen.common_screen.LoadingScreen
import com.teumteumeat.teumteumeat.ui.screen.common_screen.UiScreenState
import com.teumteumeat.teumteumeat.utils.LocalActivityContext

@Composable
fun QuizScreen(
    uiState: UiStateQuiz,
    onBackClick: () -> Unit,
    onSelectAnswer: (String) -> Unit,
    screenState: UiScreenState,
    onRetryApi: () -> Unit,
    onCompleteQuiz: () -> Unit,
    onDismissExitDialog: () -> Unit,
    onDestroyActivity: (() -> Unit)?,
) {

    val activity = LocalActivityContext.current as QuizActivity


    // 🔴 에러 화면 (핵심)
    if (screenState is UiScreenState.Error) {
        val errorMessage =
            (screenState as UiScreenState.Error).message

        FullScreenErrorModal(
            errorState = ErrorState(
                title = "문제가 발생했어요",
                description = errorMessage,
                retryLabel = "다시 시도하기",
                onRetry = onRetryApi
            ),
            isShowBackBtn = true,
            onBack = { activity.finish() },
        )
    }else{
        DefaultMonoBg() {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .systemBarsPadding(),
                contentAlignment = Alignment.Center,
            ) {
                when (screenState) {

                    UiScreenState.Idle -> {
                        // 진입 직후 (아직 loadQuizzes 안 했을 수도 있음)
                    }

                    UiScreenState.Loading -> {
                        LoadingScreen(
                            title = "퀴즈 로딩중",
                            message = "틈틈잇이 퀴즈를 만들고 있어요!",
                        )
                    }

                    UiScreenState.Success -> {

                        if (uiState.isCompleted) {
                            QuizCompleteCard(
                                onButtonClick = onCompleteQuiz
                            )
                        } else {
                            // 🔝 상단 헤더
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .align(Alignment.TopCenter)
                            ) {

                                ProgressHeader(
                                    modifier = Modifier
                                        .fillMaxWidth(),
                                    currentStep = uiState.currentStep,
                                    totalSteps = uiState.totalSteps,
                                    onBackClick = onBackClick
                                )
                            }

                            QuizCardStack(
                                quizzes = uiState.quizzes,
                                currentIndex = uiState.currentIndex,
                                onAnswerSubmitted = onSelectAnswer,
                            )
                            /*QuizCardSection(
                                modifier = Modifier.align(Alignment.Center),
                                quiz = uiState.currentQuiz!!,
                                questionIndex = uiState.currentIndex + 1,
                                onSelectAnswer = onSelectAnswer
                            )*/
                        }

                        // 🔴 퇴장 확인 팝업 (가장 상단에 위치하도록 Box 마지막에 배치)
                        if (uiState.showExitDialog) {
                            // 배경을 어둡게 처리하기 위한 Box
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.Black.copy(alpha = 0.5f))
                                    .pointerInput(Unit) {}, // 팝업 뒷면 터치 방지
                                contentAlignment = Alignment.Center
                            ) {
                                BaseModal(
                                    title = "퀴즈를 종료하시겠어요?",
                                    body = "지금 나가시면 진행 사항이 저장되지 않습니다.",
                                    primaryButtonText = "계속하기",
                                    secondaryButtonText = "나가기",
                                    onPrimaryClick = onDismissExitDialog,
                                    onSecondaryClick = onDestroyActivity

                                )
                            }
                        }

                    }

                    is UiScreenState.Error -> {}
                }
            }
        }

        // 🔄 정답 제출 중 오버레이
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
fun QuizCardSection(
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
                isCardStatus = CardStatus.Default,
                onYes = { },
                onNo = { }
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
                },
                onPass = {
                    onSelectAnswer("")
                }
            )
        }
    }
}



