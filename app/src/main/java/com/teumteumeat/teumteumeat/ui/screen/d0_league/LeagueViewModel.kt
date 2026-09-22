package com.teumteumeat.teumteumeat.ui.screen.d0_league

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teumteumeat.teumteumeat.domain.usecase.SessionManager
import com.teumteumeat.teumteumeat.ui.screen.common_screen.UiScreenState
import com.teumteumeat.teumteumeat.utils.DeepLinkConstants
import com.teumteumeat.teumteumeat.utils.toMaskedNickname
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
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

// 이번 주 랭킹에 아직 등록되지 않았어도(=MOCK_RANKERS에 isMe 항목이 없어도) 내 랭킹 카드(풋터)에
// 표시할 닉네임 — 풋터는 본인의 닉네임이므로 마스킹 없이 원본 그대로 노출한다.
private const val MOCK_MY_NICKNAME = "이영여민"

// 랭킹 리스트(포디움 + 4위 이하)는 공개 영역이므로 원본 닉네임을 [toMaskedNickname]으로 마스킹해
// 노출하되, 본인(isMe) 행은 마스킹하지 않고 원본 그대로 노출한다.
// 스크롤 영역이 실제로 동작하는지 확인할 수 있도록 20등까지 채운 목업 데이터.
private val MOCK_RANKERS = listOf(
    LeagueRankerUiModel(rank = 1, nickname = "틈틈틈틈틈잇틈잇".toMaskedNickname(), snackCount = 12),
    LeagueRankerUiModel(rank = 2, nickname = "김로로로롤로민".toMaskedNickname(), snackCount = 10),
    LeagueRankerUiModel(rank = 3, nickname = "이한하하나하나한재".toMaskedNickname(), snackCount = 9),
    // 닉네임 최대 길이(10자, OnBoardingViewModel.MAX_LENGTH)를 넘지 않는 대표 케이스로 5자 초과 캡 동작을 검증한다.
    LeagueRankerUiModel(rank = 4, nickname = "김수영하나영수민아라".toMaskedNickname(), snackCount = 8),
    LeagueRankerUiModel(rank = 5, nickname = "임도현".toMaskedNickname(), snackCount = 7),
    LeagueRankerUiModel(rank = 5, nickname = "강민수".toMaskedNickname(), snackCount = 7),
    LeagueRankerUiModel(rank = 7, nickname = "이빈".toMaskedNickname(), snackCount = 6),
    LeagueRankerUiModel(rank = 8, nickname = MOCK_MY_NICKNAME, snackCount = 5, isMe = true),
    LeagueRankerUiModel(rank = 9, nickname = "김하주".toMaskedNickname(), snackCount = 4),
    LeagueRankerUiModel(rank = 10, nickname = "박지훈".toMaskedNickname(), snackCount = 4),
    LeagueRankerUiModel(rank = 11, nickname = "최서아".toMaskedNickname(), snackCount = 3),
    LeagueRankerUiModel(rank = 12, nickname = "정민우".toMaskedNickname(), snackCount = 3),
    LeagueRankerUiModel(rank = 13, nickname = "한소빈".toMaskedNickname(), snackCount = 3),
    LeagueRankerUiModel(rank = 14, nickname = "오태진".toMaskedNickname(), snackCount = 3),
    LeagueRankerUiModel(rank = 15, nickname = "서지연".toMaskedNickname(), snackCount = 2),
    LeagueRankerUiModel(rank = 16, nickname = "황민서".toMaskedNickname(), snackCount = 2),
    LeagueRankerUiModel(rank = 17, nickname = "윤성호".toMaskedNickname(), snackCount = 2),
    LeagueRankerUiModel(rank = 18, nickname = "장은희".toMaskedNickname(), snackCount = 2),
    LeagueRankerUiModel(rank = 19, nickname = "신유아".toMaskedNickname(), snackCount = 1),
    LeagueRankerUiModel(rank = 20, nickname = "권도수".toMaskedNickname(), snackCount = 1),
)

@HiltViewModel
class LeagueViewModel @Inject constructor(
    val sessionManager: SessionManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow(UiStateLeague())
    val uiState = _uiState.asStateFlow()

    private val _screenState = MutableStateFlow<UiScreenState>(UiScreenState.Idle)
    val screenState = _screenState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<LeagueUiEvent>(
        replay = 0,              // 재전달 ❌
        extraBufferCapacity = 1  // 순간 이벤트 유실 방지
    )
    val uiEvent = _uiEvent.asSharedFlow()

    private var countdownJob: Job? = null

    init {
        loadLeague()
    }

    fun requestShare() {
        viewModelScope.launch {
            _uiEvent.emit(LeagueUiEvent.ShareLeague())
        }
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

sealed class LeagueUiEvent {
    data class ShareLeague(val shareText: String = DeepLinkConstants.LEAGUE_SHARE_MESSAGE) : LeagueUiEvent()
}
