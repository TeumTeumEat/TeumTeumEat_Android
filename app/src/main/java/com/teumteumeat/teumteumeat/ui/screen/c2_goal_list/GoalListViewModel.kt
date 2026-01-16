package com.teumteumeat.teumteumeat.ui.screen.c2_goal_list

import android.app.Application
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teumteumeat.teumteumeat.data.network.model.ApiResultV2
import com.teumteumeat.teumteumeat.data.network.model.uiMessage
import com.teumteumeat.teumteumeat.data.network.model_request.UpdateGoalRequest
import com.teumteumeat.teumteumeat.data.repository.document.DocumentRepository
import com.teumteumeat.teumteumeat.data.repository.goal.GoalRepository
import com.teumteumeat.teumteumeat.domain.usecase.GetGoalListUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GoalListViewModel @Inject constructor(
    application: Application,
    private val documentRepository: DocumentRepository,
    private val getGoalListUseCase: GetGoalListUseCase,
    private val goalRepository: GoalRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(UiStateGoalList())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            loadUserGoal()
            loadGoals()
        }
    }

    private fun loadGoals() {
        viewModelScope.launch {

            // 1️⃣ 로딩 시작
            _uiState.update {
                it.copy(isLoading = true, errorMessage = null)
            }

            when (val result = getGoalListUseCase()) {

                is ApiResultV2.Success -> {
                    val goals = result.data.goalResponses
                    val currentGoalId = _uiState.value.currentGoalId
                    Log.d("currentGoalId 디버깅", "$currentGoalId")
                    // 2️⃣ 서버 → UI 모델 변환
                    val uiModels = goals.mapIndexed { index, goal ->
                        goal.toUiModel(currentGoalId)
                    }

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            goals = uiModels
                        )
                    }
                }

                is ApiResultV2.NetworkError -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "네트워크 연결을 확인해주세요."
                        )
                    }
                }

                is ApiResultV2.ServerError -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = result.message
                        )
                    }
                }

                else -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "알 수 없는 오류가 발생했습니다."
                        )
                    }
                }
            }
        }
    }

    private suspend fun loadUserGoal() {
        when (val result = goalRepository.getUserGoal()) {

            is ApiResultV2.Success -> {
                val userGoal = result.data

                _uiState.update { state ->
                    state.copy(
                        currentGoalId = userGoal.goalId
                    )
                }
            }

            else -> {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = result.uiMessage
                    )
                }
            }
        }
    }

    // GoalCard 클릭 시 (즉시 변경 ❌ → 오버레이 표시)
    fun onGoalClick(goalId: Int) {
        _uiState.update {
            it.copy(
                showChangeGoalOverlay = true,
                pendingGoalId = goalId
            )
        }
    }

    // “다시 고르기” 클릭
    fun onCancelChangeGoal() {
        _uiState.update {
            it.copy(
                showChangeGoalOverlay = false,
                pendingGoalId = null
            )
        }
    }

    fun onConfirmChangeGoal() {
        val pendingGoalId = _uiState.value.pendingGoalId ?: return

        // 🔹 기존 목표 정보 찾기
        val targetGoal = _uiState.value.goals
            .firstOrNull { it.goalId == pendingGoalId }
            ?: return

        viewModelScope.launch {

            when(val result = goalRepository.updateGoal(
                pendingGoalId.toLong(),
                UpdateGoalRequest(
                    studyPeriod = targetGoal.weekText,
                    difficulty = targetGoal.difficulty,
                    prompt = targetGoal.description.ifBlank { null }
                ))
            ){
                is ApiResultV2.Success -> {
                    // ✅ 1️⃣ 오버레이 즉시 닫기
                    _uiState.update {
                        it.copy(
                            showChangeGoalOverlay = false,
                            pendingGoalId = null
                        )
                    }

                    // ✅ 2️⃣ 서버 기준 최신 상태 재조회 (순서 중요)
                    loadUserGoal()
                    loadGoals()
                }
                else -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = result.uiMessage
                        )
                    }
                }
            }


        }

    }



}
