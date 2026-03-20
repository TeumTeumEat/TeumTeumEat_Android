package com.teumteumeat.teumteumeat.ui.screen.a4_main.a4_2_library

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.common.api.Api
import com.teumteumeat.teumteumeat.data.network.model.ApiResultV2
import com.teumteumeat.teumteumeat.data.network.model.uiMessage
import com.teumteumeat.teumteumeat.data.repository.history.HistoryRepository
import com.teumteumeat.teumteumeat.domain.usecase.SessionManager
import com.teumteumeat.teumteumeat.ui.screen.a4_main.component.LibraryTabType
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

sealed interface LibraryUiEvent {
    data class NavigateToHistoryDetail(val historyId: Long) : LibraryUiEvent
}

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val historyRepository: HistoryRepository,
    val sessionManager: SessionManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiStateLibrary>(UiStateLibrary())
    val uiState = _uiState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<LibraryUiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    // Flow 값으로 currentPage 읽기
    private val currentPage get() = uiState.value.currentPage
    private val totalPage get() = uiState.value.totalPage

    init {
        // 최초 진입 시 현재 월 히스토리 로드
        loadCalendarHistory(YearMonth.now())
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

                            calendarUiState = state.calendarUiState.copy(
                                currentMonth = yearMonth,
                                solvedDates = solvedDates
                            ),

                            motivationUiState = state.motivationUiState.copy(
                                streakCount = data.currentStreak
                            )
                        )
                    }
                }

                is ApiResultV2.SessionExpired -> {
                    sessionManager.expireSession()
                }

                else -> {
                    // 👉 에러 메시지는 ViewModel 확장함수에서 처리된다고 가정
                    Log.e("LibraryViewModel", "❌ 캘린더 히스토리 로드 실패: ${result.uiMessage}")
                }
            }

        }

    }

    /** 📅 월 변경 */
    fun onCalendarMonthChanged(yearMonth: YearMonth) {
        Log.d("Calendar", "📅 월 변경: ${yearMonth.year}-${yearMonth.monthValue}")

        _uiState.update { state ->
            state.copy(
                calendarUiState = state.calendarUiState.copy(
                    currentMonth = yearMonth,

                    // ✅ 핵심: 날짜 선택 해제
                    selectedDate = null,

                    // ✅ 하단 일별 상태 초기화
                    dailyLearningList = emptyList(),
                    isDailyLoading = false,
                    dailyErrorMessage = null
                )
            )
        }

        // ✅ 월 변경 시 해당 월 이력 다시 로드
        loadCalendarHistory(yearMonth)
    }

    /** 📆 날짜 선택 */
    fun onCalendarDateSelected(date: LocalDate) {
        Log.d("Calendar", "📆 날짜 선택: $date")

        // 1️⃣ 날짜 선택 상태 갱신
        _uiState.update { state ->
            state.copy(
                calendarUiState = state.calendarUiState.copy(
                    selectedDate = date,
                    isDailyLoading = true,
                    dailyErrorMessage = null
                )
            )
        }

        // 2️⃣ 선택 날짜 기준 학습 내역 로드
        loadDailyLearningHistory(date)
    }

    /** 📥 선택 날짜의 학습 내역 조회 */
    private fun loadDailyLearningHistory(date: LocalDate) {
        viewModelScope.launch {
            when (val result = historyRepository.getCalendarDailyHistory(date.toString())
            ) {
                is ApiResultV2.Success -> {
                    _uiState.update { state ->
                        state.copy(
                            calendarUiState = state.calendarUiState.copy(
                                dailyLearningList = result.data,
                                isDailyLoading = false,
                                dailyErrorMessage = null
                            )
                        )
                    }
                }
                is ApiResultV2.SessionExpired -> {
                    sessionManager.expireSession()
                }
                else -> {
                    _uiState.update { state ->
                        state.copy(
                            calendarUiState = state.calendarUiState.copy(
                                dailyLearningList = emptyList(),
                                isDailyLoading = false,
                                dailyErrorMessage = result.uiMessage
                            )
                        )
                    }
                }
            }
        }
    }

    fun selectLibraryTab(tab: LibraryTabType) {
        _uiState.update {
            it.copy(selectedLibraryTab = tab)
        }

        if (tab == LibraryTabType.TOPIC) {
            fetchCategoryHistories()
        }
    }

    private fun fetchCategoryHistories() {
        viewModelScope.launch {
            when (val result = historyRepository.getCategoryHistories()) {

                is ApiResultV2.Success -> {
                    _uiState.update {
                        it.copy(categoryHistories = result.data)
                    }
                }

                is ApiResultV2.SessionExpired -> {
                    sessionManager.expireSession()
                }

                is ApiResultV2.ServerError,
                is ApiResultV2.NetworkError,
                is ApiResultV2.UnknownError -> {
                    // 👉 공통 에러 메시지 처리
                    _uiState.update {
                        it.copy(errorMessage = result.uiMessage)
                    }
                }
            }
        }
    }


    fun onClickCategory(categoryName: String) {
        _uiState.update { state ->
            state.copy(
                selectedCategoryName =
                    if (state.selectedCategoryName == categoryName) {
                        null // 이미 선택 → 해제
                    } else {
                        categoryName // 새로 선택
                    }
            )
        }
    }

}