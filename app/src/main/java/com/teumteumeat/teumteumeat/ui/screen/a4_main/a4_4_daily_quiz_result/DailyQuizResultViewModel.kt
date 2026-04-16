package com.teumteumeat.teumteumeat.ui.screen.a4_main.a4_4_daily_quiz_result

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teumteumeat.teumteumeat.data.network.model.ApiResultV2
import com.teumteumeat.teumteumeat.data.network.model.uiMessage
import com.teumteumeat.teumteumeat.data.repository.quiz.QuizRepository
import com.teumteumeat.teumteumeat.domain.model.common.DomainGoalType_v1
import com.teumteumeat.teumteumeat.domain.model.goal.DomainGoalType
import com.teumteumeat.teumteumeat.domain.usecase.SessionManager
import com.teumteumeat.teumteumeat.ui.screen.common_screen.UiScreenState
import com.teumteumeat.teumteumeat.utils.Utils.TypeUtils.toYyyyMmDd
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class DailyQuizResultViewModel @Inject constructor(
    private val quizRepository: QuizRepository,
    val sessionManager: SessionManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow(UiStateDailyQuizResult())
    val uiState = _uiState.asStateFlow()

    private val _screenState =
        MutableStateFlow<UiScreenState>(UiScreenState.Idle)
    val screenState = _screenState.asStateFlow()

    /** 🔹 Activity 진입 시 초기 값 세팅 */
    fun initArgs(
        id: Long,
        type: DomainGoalType,
        date: LocalDate
    ) {
        _uiState.update {
            it.copy(
                id = id,
                type = type,
                date = date,
            )
        }
    }

    fun loadQuizResults(
    ) {
        val state = _uiState.value
        val id = state.id
        val type = state.type
        val date = state.date

        // 🔐 안전성 체크
        if (id == null || type == null || date == null) {
            _screenState.value = UiScreenState.Error("잘못된 접근입니다.")
            return
        }

        viewModelScope.launch {

            // 1️⃣ 로딩 시작
            _screenState.value = UiScreenState.Loading
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            // 2️⃣ API 호출
            when (val result =
                quizRepository.getQuizHistory(type, id, date.toYyyyMmDd())) {

                is ApiResultV2.Success -> {
                    val history = result.data

                    val quizzes = history.quizzes
                    val correctCount = quizzes.count { it.isCorrect }

                    // 3️⃣ 성공 상태 반영
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            quizzes = quizzes,
                            createdAt = history.createdAt,
                            correctCount = correctCount
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


}

