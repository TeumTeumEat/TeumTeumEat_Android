package com.teumteumeat.teumteumeat.ui.screen.d0_league

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teumteumeat.teumteumeat.domain.usecase.SessionManager
import com.teumteumeat.teumteumeat.ui.screen.common_screen.UiScreenState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.Duration
import java.time.LocalDateTime
import java.time.temporal.TemporalAdjusters
import javax.inject.Inject

// 리그는 매주 월요일 00:00에 초기화된다. 다음 초기화 시점까지 남은 초를 매번 현재 시각 기준으로 계산한다.
// TODO: 실제 리그 API가 준비되면 서버가 내려주는 초기화 시각/카운트를 사용하도록 교체
private fun secondsUntilNextWeeklyReset(): Long {
    val now = LocalDateTime.now()
    val nextReset = now.toLocalDate()
        .with(TemporalAdjusters.next(DayOfWeek.MONDAY))
        .atStartOfDay()
    return Duration.between(now, nextReset).seconds
}

// 이번 주 랭킹에 아직 등록되지 않았어도(=MOCK_RANKERS에 isMe 항목이 없어도) 내 랭킹 카드에 표시할 닉네임
private const val MOCK_MY_NICKNAME = "이*민"

// 스크롤 영역이 실제로 동작하는지 확인할 수 있도록 20등까지 채운 목업 데이터
private val MOCK_RANKERS = listOf(
    LeagueRankerUiModel(rank = 1, nickname = "특*잇", snackCount = 12),
    LeagueRankerUiModel(rank = 2, nickname = "김*민", snackCount = 10),
    LeagueRankerUiModel(rank = 3, nickname = "이*재", snackCount = 9),
    LeagueRankerUiModel(rank = 4, nickname = "김*영", snackCount = 8),
    LeagueRankerUiModel(rank = 5, nickname = "임*현", snackCount = 7),
    LeagueRankerUiModel(rank = 5, nickname = "강*수", snackCount = 7),
    LeagueRankerUiModel(rank = 7, nickname = "이*", snackCount = 6),
    LeagueRankerUiModel(rank = 8, nickname = "이*민", snackCount = 5, isMe = true),
    LeagueRankerUiModel(rank = 9, nickname = "김*주", snackCount = 4),
    LeagueRankerUiModel(rank = 10, nickname = "박*훈", snackCount = 4),
    LeagueRankerUiModel(rank = 11, nickname = "최*아", snackCount = 3),
    LeagueRankerUiModel(rank = 12, nickname = "정*우", snackCount = 3),
    LeagueRankerUiModel(rank = 13, nickname = "한*빈", snackCount = 3),
    LeagueRankerUiModel(rank = 14, nickname = "오*진", snackCount = 3),
    LeagueRankerUiModel(rank = 15, nickname = "서*연", snackCount = 2),
    LeagueRankerUiModel(rank = 16, nickname = "황*서", snackCount = 2),
    LeagueRankerUiModel(rank = 17, nickname = "윤*호", snackCount = 2),
    LeagueRankerUiModel(rank = 18, nickname = "장*희", snackCount = 2),
    LeagueRankerUiModel(rank = 19, nickname = "신*아", snackCount = 1),
    LeagueRankerUiModel(rank = 20, nickname = "권*수", snackCount = 1),
)

@HiltViewModel
class LeagueViewModel @Inject constructor(
    val sessionManager: SessionManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow(UiStateLeague())
    val uiState = _uiState.asStateFlow()

    private val _screenState = MutableStateFlow<UiScreenState>(UiScreenState.Idle)
    val screenState = _screenState.asStateFlow()

    private var countdownJob: Job? = null

    init {
        loadLeague()
    }

    fun loadLeague() {
        _screenState.value = UiScreenState.Loading

        // TODO: 실제 API 연동 시 Repository 조회 결과로 교체
        val myRanker = MOCK_RANKERS.firstOrNull { it.isMe }

        _uiState.update {
            it.copy(
                isLoading = false,
                rankers = MOCK_RANKERS,
                myNickname = MOCK_MY_NICKNAME,
                myRanker = myRanker,
                myTodaySnackCount = if (myRanker != null) 1 else 0,
                resetRemainingSeconds = secondsUntilNextWeeklyReset(),
            )
        }
        _screenState.value = UiScreenState.Success

        startCountdown()
    }

    private fun startCountdown() {
        countdownJob?.cancel()
        countdownJob = viewModelScope.launch {
            while (true) {
                delay(1_000)
                val remaining = _uiState.value.resetRemainingSeconds - 1
                // 0에 도달하면 다음 주 월요일 00:00까지 남은 초를 다시 계산해 매주 반복되게 한다.
                _uiState.update {
                    it.copy(
                        resetRemainingSeconds = if (remaining > 0) remaining else secondsUntilNextWeeklyReset(),
                    )
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        countdownJob?.cancel()
    }
}
