package com.teumteumeat.teumteumeat.ui.screen.a4_main.a4_2_library

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teumteumeat.teumteumeat.data.network.model.ApiResultV2
import com.teumteumeat.teumteumeat.data.network.model.uiMessage
import com.teumteumeat.teumteumeat.domain.repository.history.HistoryRepository
import com.teumteumeat.teumteumeat.domain.usecase.SessionManager
import com.teumteumeat.teumteumeat.ui.screen.a4_main.component.LibraryTabType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject


@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val historyRepository: HistoryRepository,
    val sessionManager: SessionManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiStateLibrary>(UiStateLibrary())
    val uiState = _uiState.asStateFlow()

    private var allCategoryHistories: List<com.teumteumeat.teumteumeat.domain.model.history.CategoryHistoryUiModel> =
        emptyList()


    init {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            // 1. 월별 데이터 로드가 끝날 때까지 여기서 '일시 정지'합니다.
            loadCalendarHistory(YearMonth.now())

            _uiState.update { it.copy(isLoading = false) }

            // 2. 위 함수가 완료된 후 최신 상태값을 확인합니다.
            if (_uiState.value.isSolvedToday) {
                onCalendarDateSelected(LocalDate.now())
            }
        }
    }

    /** 📅 월별 퀴즈 히스토리 로드 */
    // 함수 앞에 suspend를 붙여 비동기 작업임을 명시합니다.
    suspend fun loadCalendarHistory(yearMonth: YearMonth) = coroutineScope {
        // ✅ 캘린더 첫째/마지막 주에 표시되는 인접 달 날짜의 학습 여부도 필요하므로
        //    현재 월 + 이전 월 + 다음 월을 병렬 로드한다
        val currentDeferred = async {
            historyRepository.getCalendarHistory(
                year = yearMonth.year,
                month = yearMonth.monthValue
            )
        }
        val prevMonth = yearMonth.minusMonths(1)
        val prevDeferred = async {
            historyRepository.getCalendarHistory(
                year = prevMonth.year,
                month = prevMonth.monthValue
            )
        }
        val nextMonth = yearMonth.plusMonths(1)
        val nextDeferred = async {
            historyRepository.getCalendarHistory(
                year = nextMonth.year,
                month = nextMonth.monthValue
            )
        }

        // ✅ 인접 월은 stampedDates만 사용 — 실패해도 무시 (원 표시만 생략됨)
        val adjacentSolvedDates = listOf(prevDeferred, nextDeferred)
            .map { it.await() }
            .flatMap { result ->
                when (result) {
                    is ApiResultV2.Success -> result.data.stampedDates
                    else -> {
                        Log.e("LibraryViewModel", "⚠️ 인접 월 히스토리 로드 실패: ${result.uiMessage}")
                        emptyList()
                    }
                }
            }
            .map { LocalDate.parse(it) }
            .toSet()

        when (val result = currentDeferred.await()) {
            is ApiResultV2.Success -> {
                val data = result.data

                val solvedDates = data.stampedDates
                    .map { LocalDate.parse(it) }
                    .toSet() + adjacentSolvedDates

                // 현재 날짜 포함 여부 확인
                val isTodayIncluded = solvedDates.contains(LocalDate.now())

                // UiState 업데이트 (copy를 사용하여 특정 값만 변경)
                _uiState.update { currentState ->
                    currentState.copy(isSolvedToday = isTodayIncluded)
                }

                _uiState.update { state ->
                    state.copy(
                        currentStreak = data.currentStreak,
                        stampCount = data.totalStamps,
                        monthStampCount = data.monthlyStamps,

                        calendarUiState = state.calendarUiState.copy(
                            currentMonth = yearMonth,
                            // ✅ 기존 값과 union — 월 스와이프 왕복 시 이미 로드한 데이터 유지
                            solvedDates = state.calendarUiState.solvedDates + solvedDates,
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

    /** 📅 월 변경 */
    fun onCalendarMonthChanged(yearMonth: YearMonth) {
        // ✅ 현재 뷰모델이 가진 월과 같다면 (탭 이동으로 인한 초기 방출이라면) 무시
        if (_uiState.value.calendarUiState.currentMonth == yearMonth) {
            return
        }

        Log.d("Calendar", "📅 월 변경: ${yearMonth.year}-${yearMonth.monthValue}")
        viewModelScope.launch {
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
    }

    /** 📆 날짜 선택 */
    fun onCalendarDateSelected(date: LocalDate) {
        // ✅ 이미 선택된 날짜를 다시 클릭하면 재로딩하지 않음
        //    단, 직전 로드가 실패한 경우(dailyErrorMessage != null)는 재시도 허용
        val calendarState = _uiState.value.calendarUiState
        if (calendarState.selectedDate == date && calendarState.dailyErrorMessage == null) {
            return
        }

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
            _uiState.update { it.copy(isCategoryLoading = true) }

            when (val result = historyRepository.getCategoryHistories()) {

                is ApiResultV2.Success -> {
                    allCategoryHistories = result.data
                    _uiState.update { state ->
                        state.copy(
                            isCategoryLoading = false,
                            categoryHistories = if (state.showOnlyInProgress) {
                                result.data.filter { category -> category.histories.none { it.isCompleted } }
                            } else {
                                result.data
                            }
                        )
                    }
                }

                is ApiResultV2.SessionExpired -> {
                    _uiState.update { it.copy(isCategoryLoading = false) }
                    sessionManager.expireSession()
                }

                is ApiResultV2.ServerError,
                is ApiResultV2.NetworkError,
                is ApiResultV2.UnknownError -> {
                    // 👉 공통 에러 메시지 처리
                    _uiState.update {
                        it.copy(isCategoryLoading = false, errorMessage = result.uiMessage)
                    }
                }
            }
        }
    }


    fun onToggleInProgressFilter() {
        val newValue = !_uiState.value.showOnlyInProgress
        _uiState.update { state ->
            state.copy(
                showOnlyInProgress = newValue,
                categoryHistories = if (newValue) {
                    allCategoryHistories.filter { category -> category.histories.none { it.isCompleted } }
                } else {
                    allCategoryHistories
                },
                selectedCategoryName = null,
            )
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