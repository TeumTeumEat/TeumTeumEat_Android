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
import com.teumteumeat.teumteumeat.domain.usecase.SessionManager
import com.teumteumeat.teumteumeat.ui.screen.a2_on_boarding.UiStateOnboardingScreenState
import com.teumteumeat.teumteumeat.ui.screen.common_screen.UiScreenState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GoalListViewModel @Inject constructor(
    private val getGoalListUseCase: GetGoalListUseCase,
    private val goalRepository: GoalRepository,
    val sessionManager: SessionManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow(UiStateGoalList())
    val uiState = _uiState.asStateFlow()

    private val _screenState = MutableStateFlow<UiScreenState>(UiScreenState.Loading)
    val screenState = _screenState.asStateFlow()

    init {
        loadMyPageData()
    }

    fun loadMyPageData() {
        viewModelScope.launch {
            loadUserGoal()
            loadGoals()
        }
    }

    private fun loadGoals() {
        viewModelScope.launch {

            // 1️⃣ 로딩 시작
            _uiState.update {
                _screenState.value = UiScreenState.Loading
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
                    _screenState.value = UiScreenState.Success
                }

                else -> {
                    moveToError(result)
                }
            }
        }
    }

    private suspend fun moveToError(result: ApiResultV2<*>) {
        when (result) {
            is ApiResultV2.SessionExpired -> {
                sessionManager.expireSession()
            }

            else -> {
                _uiState.update {
                    _screenState.value = UiScreenState.Error(message = result.uiMessage)
                    it.copy(
                        isLoading = false,
                        errorMessage = result.uiMessage
                    )
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
                moveToError(result)
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
            // ✅ 로딩 시작
            _uiState.update { it.copy(isLoading = true) }

            when(val result = goalRepository.updateGoal(
                pendingGoalId.toLong(),
            )){

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

                    _uiState.update {
                        it.copy(
                            isChanged = true
                        )
                    }
                    // ✅ 2️⃣ 전역 시그널 방출
                    // Repository 내부의 MutableSharedFlow에 신호를 보냅니다.
                    // 이 신호는 MainActivity 등에서 감지하여 데이터를 새로고침하게 됩니다.
                    goalRepository.emitRefreshSignal()
                }
                else -> {
                    moveToError(result)
                }
            }


        }

    }



}
