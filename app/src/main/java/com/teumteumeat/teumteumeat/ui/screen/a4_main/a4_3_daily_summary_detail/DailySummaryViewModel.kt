package com.teumteumeat.teumteumeat.ui.screen.a4_main.a4_3_daily_summary_detail

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teumteumeat.teumteumeat.data.network.model.ApiResultV2
import com.teumteumeat.teumteumeat.data.network.model.uiMessage
import com.teumteumeat.teumteumeat.domain.repository.history.HistoryRepository
import com.teumteumeat.teumteumeat.domain.model.goal.DomainGoalType
import com.teumteumeat.teumteumeat.domain.usecase.SessionManager
import com.teumteumeat.teumteumeat.ui.screen.common_screen.UiScreenState
import com.teumteumeat.teumteumeat.utils.Utils.TypeUtils.formatDate
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class DailySummaryViewModel @Inject constructor(
    private val historyRepository: HistoryRepository,
    val application: Application,
    val sessionManager: SessionManager,
) : ViewModel() {
    private val appContext = application.applicationContext

    private val _uiState = MutableStateFlow(UiStateDailySummary())
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
                dateText = formatDate(date)
            )
        }
    }

    /**
     * 해당 일자의 요약글 조회
     */
    fun loadSummary() {
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
            _screenState.value = UiScreenState.Loading
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            when (
                val result = historyRepository.getLearningHistorySummary(
                    id = id,
                    type = type,
                    date = date.toString()
                )
            ) {
                is ApiResultV2.Success -> {
                    val summary = result.data

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            title = summary.title,
                            summary = summary.summary
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
