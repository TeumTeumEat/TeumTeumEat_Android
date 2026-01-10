package com.teumteumeat.teumteumeat.ui.screen.a4_main

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teumteumeat.teumteumeat.data.network.model.ApiResultV2
import com.teumteumeat.teumteumeat.data.network.model.uiMessage
import com.teumteumeat.teumteumeat.data.repository.history.HistoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val historyRepository: HistoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiStateMain>(UiStateMain())
    val uiState = _uiState.asStateFlow()

    // Flow 값으로 currentPage 읽기
    private val currentPage get() = uiState.value.currentPage
    private val totalPage get() = uiState.value.totalPage

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

                is ApiResultV2.ServerError,
                is ApiResultV2.NetworkError,
                is ApiResultV2.UnknownError,
                is ApiResultV2.SessionExpired -> {
                    // 👉 에러 메시지는 ViewModel 확장함수에서 처리된다고 가정
                    Log.e("LibraryViewModel", "❌ 캘린더 히스토리 로드 실패: ${result.uiMessage}")
                }
            }
        }
    }


    fun nextPage() {
        if (currentPage < totalPage) {
            Log.d("1증가 전", "증가함, ${currentPage}/${totalPage}")
            viewModelScope.launch {
                _uiState.update { currentState ->
                    currentState.copy(
                        currentPage = currentPage + 1
                    )
                }
            }
            Log.d("1증가 후", "증가함, ${currentPage}/${totalPage}")
        }
    }

    fun prevPage() {
        if (currentPage > 0) {
            Log.d("1감소 전", "감소함, ${currentPage}/${totalPage}")
            viewModelScope.launch {
                _uiState.update { currentState ->
                    currentState.copy(
                        currentPage = currentPage - 1
                    )
                }
            }
            Log.d("1감소 후", "감소함, ${currentPage}/${totalPage}")
        }
    }
}