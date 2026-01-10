package com.teumteumeat.teumteumeat.ui.screen.b2_quiz

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teumteumeat.teumteumeat.data.network.model.ApiResultV2
import com.teumteumeat.teumteumeat.data.network.model.uiMessage
import com.teumteumeat.teumteumeat.data.repository.quiz.QuizRepository
import com.teumteumeat.teumteumeat.domain.model.common.GoalTypeUiState
import com.teumteumeat.teumteumeat.ui.screen.common_screen.UiScreenState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class QuizViewModel @Inject constructor(
    private val quizRepository: QuizRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(UiStateQuiz())
    val uiState = _uiState.asStateFlow()

    private val _screenState =
        MutableStateFlow<UiScreenState>(UiScreenState.Idle)
    val screenState = _screenState.asStateFlow()

    fun prevQuiz() {
        _uiState.update { state ->
            if (state.currentIndex <= 0) {
                state // 첫 페이지 → 변화 없음
            } else {
                state.copy(
                    currentIndex = state.currentIndex - 1
                )
            }
        }
    }

    private fun moveToNextQuizIfPossible(isCorrect: Boolean) {
        // if (!isCorrect) return

        _uiState.update { state ->
            val isLastQuiz = state.currentIndex == state.quizzes.lastIndex

            if (isLastQuiz) {
                // 🎉 마지막 문제 정답 → 완료 상태
                state.copy(isCompleted = true)
            } else {
                // 다음 문제로 이동
                state.copy(currentIndex = state.currentIndex + 1)
            }
        }
    }

    fun resetIdleState() {
        _screenState.value = UiScreenState.Idle
    }


    fun loadQuizzes(
        documentId: Int,
        goalTypeUiState: GoalTypeUiState,
    ) {

        viewModelScope.launch {
            _screenState.value = UiScreenState.Loading
            _uiState.update{
                it.copy(
                    isLoading = true,
                    errorMessage = null,
                )
            }

            when (
                val result =
                    quizRepository.getUserQuizzes(documentId, goalTypeUiState)
            ) {
                is ApiResultV2.Success -> {
                    // 🔍 1. Domain 단계 quizId 확인
                    result.data.forEachIndexed { index, quiz ->
                        Log.d(
                            "QuizDebug",
                            "Domain[$index] quizId=${quiz.quizId}, question=${quiz.question}"
                        )
                    }

                    _uiState.update {

                        val uiQuizzes = result.data.map { quiz ->
                            val ui = quiz.toUiState()

                            // 🔍 2. UiState 단계 quizId 확인
                            Log.d(
                                "QuizDebug",
                                "UiState quizId=${ui.quizId}, question=${ui.question}"
                            )

                            ui
                        }

                        it.copy(
                            isLoading = false,
                            quizzes = uiQuizzes,
                            currentIndex = 0
                        )
                    }

                    _screenState.value = UiScreenState.Success
                }

                else -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = result.uiMessage
                        )
                    }
                    _screenState.value =
                        UiScreenState.Error(result.uiMessage)
                }
            }
        }
    }

    fun submitAnswer(answer: String) {
        val state = uiState.value
        val quiz = state.currentQuiz ?: return

        Log.d(
            "QuizViewModel",
            "submit quizId=${quiz?.quizId}, index=${state.currentIndex}"
        )

        viewModelScope.launch {

            // 1️⃣ 카드 submitting 처리
            _uiState.update {
                it.copy(
                    quizzes = it.quizzes.mapIndexed { index, q ->
                        if (index == it.currentIndex)
                            q.copy(
                                selectedAnswer = answer,
                                isSubmitting = true
                            )
                        else q
                    }
                )
            }

            // 2️⃣ 제출 API 호출
            Log.d("QuizViewModel", "제출 api 호출")
            when (
                val result = quizRepository.submitQuiz(
                    quizId = quiz.quizId,
                    userAnswer = answer
                )
            ) {
                is ApiResultV2.Success -> {
                    val isCorrect = result.data.isCorrect

                    _uiState.update { state ->
                        val updatedQuizzes =
                            state.quizzes.mapIndexed { index, q ->
                                if (index == state.currentIndex)
                                    q.copy(
                                        isSubmitting = false,
                                        isSubmitted = true,
                                        isCorrect = isCorrect
                                    )
                                else q
                            }

                        state.copy(
                            quizzes = updatedQuizzes,
                        )
                    }
                    // ✅ 결과 반영 후 "이동 여부"는 여기서 판단
                    moveToNextQuizIfPossible(isCorrect)
                }

                else -> {
                    val message = result.uiMessage

                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = message)
                    }
                    _screenState.value =
                        UiScreenState.Error(message)
                }
            }
        }
    }



}
