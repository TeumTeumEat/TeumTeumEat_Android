package com.teumteumeat.teumteumeat.ui.screen.a4_main

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import androidx.compose.ui.geometry.Offset
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teumteumeat.teumteumeat.BuildConfig
import com.teumteumeat.teumteumeat.data.network.model.ApiResultV2
import com.teumteumeat.teumteumeat.data.network.model.uiMessage
import com.teumteumeat.teumteumeat.data.repository.history.HistoryRepository
import com.teumteumeat.teumteumeat.domain.usecase.SessionManager
import com.teumteumeat.teumteumeat.ui.screen.common_screen.UiScreenState
import com.teumteumeat.teumteumeat.utils.date_change_reciver.DateChangeReceiver
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
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
    private val dateChangeReceiver: DateChangeReceiver, // Singleton 리시버 주입
    @ApplicationContext private val context: Context, // 등록/해제를 위한 컨텍스트
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiStateMain>(UiStateMain())
    val uiState = _uiState.asStateFlow()

    private val _screenState =
        MutableStateFlow<UiScreenState>(UiScreenState.Idle)
    val screenState = _screenState.asStateFlow()

    val retryEvent = MutableSharedFlow<Unit>()
    fun triggerRetry() { viewModelScope.launch { retryEvent.emit(Unit) } }

    init {

        // 날짜 변경 시에 viewModel.loadCalendarHistory(YearMonth.now()) 호출
        setupDateChangeReceiver()
        loadCalendarHistory(YearMonth.now())
    }



    internal fun setupDateChangeReceiver() {

        dateChangeReceiver.setOnDateChangedListener {
            loadCalendarHistory(YearMonth.now()) // 날짜 변경 시 실행할 비즈니스 로직
        }

        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_DATE_CHANGED)

            // 디버그 모드일 때만 테스트용 커스텀 액션 추가
            if (BuildConfig.DEBUG) {
                addAction("com.teumteumeat.test.ACTION_DATE_CHANGED")
            }
        }

        // 모드에 따른 보안 플래그 설정
        val flags = if (BuildConfig.DEBUG) {
            ContextCompat.RECEIVER_EXPORTED // 디버그: ADB 등 외부 신호 허용
        } else {
            ContextCompat.RECEIVER_NOT_EXPORTED // 릴리즈: 외부 앱/ADB 차단 (보안 강화)
        }

        // ContextCompat을 사용하여 등록
        ContextCompat.registerReceiver(
            context,
            dateChangeReceiver,
            filter,
            flags
        )

        if (BuildConfig.DEBUG) {
            Log.d("HomeViewModel", "리시버 등록 완료 (디버그 모드 - 외부 노출 허용)")
        }
    }

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

    override fun onCleared() {
        super.onCleared()
        // 4. 중요: ViewModel이 소멸될 때 리시버 등록을 해제하여 메모리 누수 방지
        try {
            context.unregisterReceiver(dateChangeReceiver)
        } catch (e: IllegalArgumentException) {
            // 이미 해제되었거나 등록되지 않은 경우 예외 처리
            Log.e("MainViewModel", "Receiver 해제 실패", e)
        }
    }

}