package com.teumteumeat.teumteumeat.ui.screen.a4_main

import android.util.Log
import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teumteumeat.teumteumeat.data.network.model.ApiResultV2
import com.teumteumeat.teumteumeat.data.network.model.uiMessage
import com.teumteumeat.teumteumeat.data.repository.history.HistoryRepository
import com.teumteumeat.teumteumeat.domain.usecase.SessionManager
import com.teumteumeat.teumteumeat.ui.screen.common_screen.UiScreenState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val historyRepository: HistoryRepository,
    val sessionManager: SessionManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiStateMain>(UiStateMain())
    val uiState = _uiState.asStateFlow()

    private val _screenState =
        MutableStateFlow<UiScreenState>(UiScreenState.Idle)
    val screenState = _screenState.asStateFlow()


    fun updatePlusButtonOffset(offset: Offset) {
        _uiState.update { state ->
            if (state.plusBtnOffset == null) {
                state.copy(plusBtnOffset = offset)
            } else {
                state
            }
        }
    }

    fun toggleBottomNavPlus() {
        _uiState.update { state ->
            if (state.plusBtnOffset == null) {
                // 아직 위치 측정 안 됨 → 무시
                state
            } else {
                state.copy(
                    isExpandedBottomNavItemPlus = !state.isExpandedBottomNavItemPlus
                )
            }
        }
    }

    fun closeBottomNavPlus() {
        _uiState.update {
            it.copy(isExpandedBottomNavItemPlus = false)
        }
    }

    fun onScreenChanged(
        screenType: MainScreenType,
        from: String = "UNKNOWN"
    ) {
        Log.d("탭 변경 추적", "onScreenChanged($screenType) from=$from")
        _uiState.update {
            it.copy(
                currentScreenType = screenType,
                hasHandledExternalNavigation = true
            )
        }
    }

    /** 📅 월별 퀴즈 히스토리 로드 */
    fun loadCalendarHistory(yearMonth: YearMonth) {
        viewModelScope.launch {
            when (
                val result = historyRepository.getCalendarHistory(
                    year = yearMonth.year,
                    month = yearMonth.monthValue
                )
            ) {
                is ApiResultV2.Success -> {
                    val data = result.data

                    val solvedDates = data.stampedDates
                        .map { LocalDate.parse(it) }
                        .toSet()

                    _uiState.update { state ->
                        state.copy(
                            currentStreak = data.currentStreak,
                            stampCount = data.totalStamps,
                            monthStampCount = data.monthlyStamps,
                        )
                    }
                }

                is ApiResultV2.SessionExpired -> {
                    sessionManager.expireSession()
                }
                is ApiResultV2.ServerError,
                is ApiResultV2.NetworkError,
                is ApiResultV2.UnknownError -> {
                    // 👉 에러 메시지는 ViewModel 확장함수에서 처리된다고 가정
                    Log.e("LibraryViewModel", "❌ 캘린더 히스토리 로드 실패: ${result.uiMessage}")
                }
            }
        }
    }

}