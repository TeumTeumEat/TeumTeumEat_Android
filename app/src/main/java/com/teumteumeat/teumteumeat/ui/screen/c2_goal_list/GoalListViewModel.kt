package com.teumteumeat.teumteumeat.ui.screen.c2_goal_list

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teumteumeat.teumteumeat.data.network.model.ApiResultV2
import com.teumteumeat.teumteumeat.data.repository.document.DocumentRepository
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

) : ViewModel() {

    private val _uiState = MutableStateFlow(UiStateGoalList())
    val uiState = _uiState.asStateFlow()

    init {
        loadGoals()
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

                    // 2️⃣ 서버 → UI 모델 변환
                    val uiModels = goals.mapIndexed { index, goal ->
                        goal.toUiModel(
                            isSelected = index == 0   // 첫 번째 목표 선택
                        )
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
}
