package com.teumteumeat.teumteumeat.ui.screen.b2_quiz

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teumteumeat.teumteumeat.data.network.model.ApiResultV2
import com.teumteumeat.teumteumeat.data.network.model.uiMessage
import com.teumteumeat.teumteumeat.data.repository.goal.GoalRepository
import com.teumteumeat.teumteumeat.data.repository.quiz.QuizRepository
import com.teumteumeat.teumteumeat.domain.model.common.GoalTypeUiState
import com.teumteumeat.teumteumeat.domain.usecase.SessionManager
import com.teumteumeat.teumteumeat.ui.screen.common_screen.UiScreenState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class QuizViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val quizRepository: QuizRepository,
    private val goalRepository: GoalRepository,
    val sessionManager: SessionManager,
) : ViewModel() {

    // Intent로 전달된 값을 가져옵니다. (SummaryActivity에서 넣은 Key와 일치해야 함)
// 1. 안전하게 데이터를 꺼내와서 Enum으로 변환
    val goalType: GoalTypeUiState = GoalTypeUiState.fromString(
        savedStateHandle.get<String>("goalType")
    )
    val documentId: Long = savedStateHandle.get<Long>("documentId") ?: -1L

    private val _uiState = MutableStateFlow(UiStateQuiz())
    val uiState = _uiState.asStateFlow()

    private val _screenState =
        MutableStateFlow<UiScreenState>(UiScreenState.Idle)
    val screenState = _screenState.asStateFlow()

    fun completeQuiz() {
        // ✅ 2️⃣ 전역 시그널 방출
        // Repository 내부의 MutableSharedFlow에 신호를 보냅니다.
        // 이 신호는 MainActivity 등에서 감지하여 데이터를 새로고침하게 됩니다.
        viewModelScope.launch {
            completeCurrentQuizSet()
            goalRepository.emitRefreshSignal()
        }
    }

    /**
     * 퀴즈 완료를 API 호출 시 - 유저 쿠폰수 차감 및 퀴즈 풀이 횟수 1증가 API 호출됨
     */
    private fun completeCurrentQuizSet() {
        viewModelScope.launch {
            when (val response = quizRepository.submitCompleteQuizSet()) {
                is ApiResultV2.Success -> {}
                else -> { moveToError(response) }
            }
        }
    }

    fun prevQuiz() {
        _uiState.update { state ->
            if (state.currentIndex <= 0) {
                // 첫 페이지일 때 팝업 상태를 true로 변경
                state.copy(showExitDialog = true)
            } else {
                state.copy(
                    currentIndex = state.currentIndex - 1
                )
            }
        }
    }

    // 팝업 닫기 기능 (취소 버튼용)
    fun dismissExitDialog() {
        _uiState.update { it.copy(showExitDialog = false) }
    }

    private fun moveToNextQuizIfPossible(isCorrect: Boolean) {

        _uiState.update { state ->
            val isLastQuiz = state.currentIndex == state.quizzes.lastIndex

            if (isLastQuiz) {
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


    fun loadQuizzes(documentId: Long, goalType: GoalTypeUiState) {

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
                    quizRepository.getUserQuizzes(documentId.toInt(), goalType)
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

                is ApiResultV2.SessionExpired -> {
                    sessionManager.expireSession()
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
        // todo. answer 값이 비어있으면 정답에 값 선택안됨 처리
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

    private suspend fun moveToError(result: ApiResultV2<*>) {
        when (result) {
            is ApiResultV2.SessionExpired -> {
                sessionManager.expireSession()
            }

            is ApiResultV2.NetworkError -> {
                _uiState.update {
                    it.copy(
                        errorMessage = result.uiMessage
                    )
                }
            }

            is ApiResultV2.ServerError -> {
                _uiState.update {
                    it.copy(
                        errorMessage = result.uiMessage
                    )
                }
            }

            else -> {

                _uiState.update {
                    it.copy(
                        errorMessage = "알 수 없는 오류가 발생했습니다."
                    )
                }
            }
        }

    }
}
