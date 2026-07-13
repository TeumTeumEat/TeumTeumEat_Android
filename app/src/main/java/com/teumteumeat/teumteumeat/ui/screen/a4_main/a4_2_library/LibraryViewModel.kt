package com.teumteumeat.teumteumeat.ui.screen.a4_main.a4_2_library

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teumteumeat.teumteumeat.data.network.model.ApiResultV2
import com.teumteumeat.teumteumeat.data.network.model.uiMessage
import com.teumteumeat.teumteumeat.domain.repository.history.HistoryRepository
import com.teumteumeat.teumteumeat.domain.usecase.SessionManager
import com.teumteumeat.teumteumeat.ui.screen.a4_main.component.LibraryTabType
import com.teumteumeat.teumteumeat.utils.firebase.TeumAnalyticsLogger
import dagger.hilt.android.lifecycle.HiltViewModel
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
    private val analyticsLogger: TeumAnalyticsLogger,
    val sessionManager: SessionManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiStateLibrary>(UiStateLibrary())
    val uiState = _uiState.asStateFlow()

    private var allCategoryHistories: List<com.teumteumeat.teumteumeat.domain.model.history.CategoryHistoryUiModel> =
        emptyList()

    /**
     * LIB-001 발화 여부 플래그.
     * 이 ViewModel은 Activity 스코프라 config change(화면 회전 등)에도 유지되므로,
     * Activity 재생성으로 컴포지션이 재구성되어도 중복 발화를 막는다.
     * 탭을 실제로 떠날 때만 [onCalendarViewExited]로 리셋된다.
     */
    private var hasLoggedCalendarView = false

    /** LIB-001 — 진입 시점에 캘린더 데이터 로드가 미완료면 로드 완료 후 발화하도록 예약하는 플래그 */
    private var pendingCalendarViewLog = false

    /** 첫 캘린더 데이터 로드 시도 완료 여부 (성공/실패 무관) */
    private var isCalendarDataLoadCompleted = false

    /** 마지막 캘린더 데이터 로드 성공 여부 — false면 스탬프 파라미터를 unknown/-1로 발화 */
    private var isCalendarDataLoadSucceeded = false

    /**
     * LIB-001 — 히스토리 탭 진입 시 호출. 최초 1회로 제한하지 않고
     * [LibraryScreen]의 DisposableEffect(Unit)에서 매 진입마다 호출된다.
     * 단, 화면 회전 등 Activity 재생성으로 인한 재진입 시에는 발화하지 않는다.
     *
     * 첫 진입처럼 캘린더 데이터 로드가 끝나지 않은 상태면 즉시 발화하지 않고
     * [loadCalendarHistory] 완료 시점으로 발화를 미룬다 — 스탬프 파라미터
     * (month_stamp_count / has_month_stamp / total_stamps)에 정확한 값을 싣기 위함.
     */
    fun onCalendarViewEntered() {
        if (hasLoggedCalendarView) return
        hasLoggedCalendarView = true

        if (isCalendarDataLoadCompleted) {
            logCalendarViewNow()
        } else {
            pendingCalendarViewLog = true
        }
    }

    /**
     * LIB-001 실제 발화. 로드 성공 상태면 표시 월 기준 실값, 실패 상태면 unknown/-1을 싣는다.
     * month는 재방문 시 마지막으로 보던 월이 표시되므로 `calendarUiState.currentMonth`를 사용해
     * month_stamp_count와 월 기준을 일치시킨다.
     */
    private fun logCalendarViewNow() {
        val state = _uiState.value
        val loaded = isCalendarDataLoadSucceeded

        analyticsLogger.logCalendarView(
            month = state.calendarUiState.currentMonth.toString(), // ISO 포맷이 이미 "yyyy-MM"과 일치
            date = LocalDate.now().toString(),                     // ISO 포맷이 이미 "yyyy-MM-dd"와 일치
            monthStampCount = if (loaded) state.monthStampCount.toLong() else -1L,
            hasMonthStamp = if (loaded) (state.monthStampCount > 0).toString() else "unknown",
            totalStamps = if (loaded) state.stampCount.toLong() else -1L,
        )
    }

    /** 로드 완료 시점에 예약된 LIB-001 발화를 수행한다. */
    private fun flushPendingCalendarViewLog() {
        if (!pendingCalendarViewLog) return
        pendingCalendarViewLog = false
        logCalendarViewNow()
    }

    /**
     * LIB-001 — 유저가 히스토리 탭을 실제로 떠날 때 호출 (config change로 인한 dispose 제외).
     * 플래그를 리셋해 탭 재방문 시 다시 발화되도록 한다.
     */
    fun onCalendarViewExited() {
        hasLoggedCalendarView = false
    }


    init {
        viewModelScope.launch {
            // 1. 월별 데이터 로드가 끝날 때까지 여기서 '일시 정지'합니다.
            loadCalendarHistory(YearMonth.now())

            // 2. 위 함수가 완료된 후 최신 상태값을 확인합니다.
            if (_uiState.value.isSolvedToday) {
                onCalendarDateSelected(LocalDate.now())
            }
        }
    }

    /** 📅 월별 퀴즈 히스토리 로드 */
    // 함수 앞에 suspend를 붙여 비동기 작업임을 명시합니다.
    suspend fun loadCalendarHistory(yearMonth: YearMonth) {
        // ⚠️ 내부의 viewModelScope.launch를 제거합니다.
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
                            solvedDates = solvedDates,
                        ),

                        motivationUiState = state.motivationUiState.copy(
                            streakCount = data.currentStreak
                        )
                    )
                }

                isCalendarDataLoadCompleted = true
                isCalendarDataLoadSucceeded = true
                flushPendingCalendarViewLog()
            }

            is ApiResultV2.SessionExpired -> {
                // ⚠️ LIB-001 pending 발화를 수행하지 않음 — 로그인 화면으로 이탈하는 경로
                sessionManager.expireSession()
            }

            else -> {
                // 👉 에러 메시지는 ViewModel 확장함수에서 처리된다고 가정
                Log.e("LibraryViewModel", "❌ 캘린더 히스토리 로드 실패: ${result.uiMessage}")

                // LIB-001: 로드 실패도 '완료'로 취급해 unknown/-1로 발화 (탭 이용률 모수 보존)
                isCalendarDataLoadCompleted = true
                isCalendarDataLoadSucceeded = false
                flushPendingCalendarViewLog()
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

    /**
     * 📆 LIB-002 — 캘린더 날짜 셀 유저 탭 전용 진입점.
     *
     * 스탬프 유무와 무관하게 `calendar_date_tap` 이벤트를 발화하고,
     * 화면 동작(날짜 선택/일별 상세 조회)은 기존과 동일하게 스탬프 날짜에만 수행한다.
     * has_stamp는 스탬프 렌더링과 동일한 소스(`calendarUiState.solvedDates`)로 판단한다.
     *
     * 진입 시 오늘 날짜 자동 선택(`init`의 [onCalendarDateSelected] 직접 호출)은
     * 유저 탭이 아니므로 이 함수를 거치지 않아 이벤트가 발화되지 않는다.
     */
    fun onCalendarDateTapped(date: LocalDate) {
        val hasStamp = _uiState.value.calendarUiState.solvedDates.contains(date)
        analyticsLogger.logCalendarDateTap(
            date = date.toString(),         // ISO 포맷이 이미 "yyyy-MM-dd"와 일치
            hasStamp = hasStamp.toString(), // "true" | "false"
        )

        if (hasStamp) {
            onCalendarDateSelected(date)
        }
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
                    allCategoryHistories = result.data
                    _uiState.update { state ->
                        state.copy(
                            categoryHistories = if (state.showOnlyInProgress) {
                                result.data.filter { category -> category.histories.none { it.isCompleted } }
                            } else {
                                result.data
                            }
                        )
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