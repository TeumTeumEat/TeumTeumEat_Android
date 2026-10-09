package com.teumteumeat.teumteumeat.ui.screen.d0_league

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teumteumeat.teumteumeat.data.network.model.ApiResultV2
import com.teumteumeat.teumteumeat.data.network.model.uiMessage
import com.teumteumeat.teumteumeat.domain.model.league.LeagueRanker
import com.teumteumeat.teumteumeat.domain.model.league.LeagueRanking
import com.teumteumeat.teumteumeat.domain.model.league.isLeagueWeekFinished
import com.teumteumeat.teumteumeat.domain.model.league.toLeagueWeekLabel
import com.teumteumeat.teumteumeat.domain.usecase.SessionManager
import com.teumteumeat.teumteumeat.domain.usecase.league.GetLeagueRankingUseCase
import com.teumteumeat.teumteumeat.domain.usecase.league.GetPendingLeagueResultUseCase
import com.teumteumeat.teumteumeat.domain.usecase.league.MarkLeagueResultSeenUseCase
import com.teumteumeat.teumteumeat.ui.screen.common_screen.UiScreenState
import com.teumteumeat.teumteumeat.utils.DeepLinkConstants
import com.teumteumeat.teumteumeat.utils.ElapsedTimeSource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Clock
import java.time.LocalDate
import java.time.LocalDateTime
import javax.inject.Inject

// 리셋 직후 재조회 — 서버에 새 주 랭킹이 반영되기 전이면 잠시 뒤 다시 시도하고, 끝내 실패하면 로컬 계산으로 폴백한다.
private const val RESET_REFRESH_MAX_ATTEMPTS = 3
private const val RESET_REFRESH_RETRY_DELAY_MILLIS = 5_000L

// 리그 리셋 주기(매주). 서버가 새 주를 내려주지 않을 때 마감 시점을 이만큼 뒤로 민다.
private const val LEAGUE_CYCLE_MILLIS = 7L * 24 * 60 * 60 * 1_000

@HiltViewModel
class LeagueViewModel @Inject constructor(
    val sessionManager: SessionManager,
    private val getLeagueRanking: GetLeagueRankingUseCase,
    private val getPendingLeagueResult: GetPendingLeagueResultUseCase,
    private val markLeagueResultSeen: MarkLeagueResultSeenUseCase,
    private val clock: Clock,
    private val elapsedTimeSource: ElapsedTimeSource,
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

    // 서버가 내려준 남은 시간(remainingSeconds)을 받은 시점에 '마감 시점(부팅 후 경과 ms)'으로 바꿔 저장한다.
    // 카운트다운은 값을 깎지 않고 매 tick 마다 (마감 시점 - 현재 경과 시간)으로 계산하므로 오차·백그라운드 지연이 누적되지 않고,
    // 기기 시각(벽시계)·시간대를 바꿔도 서버가 준 남은 시간 그대로 줄어든다.
    private var resetDeadlineElapsedMillis: Long = 0L

    // 현재 화면에 표시 중인 리그 주의 시작일 — 리셋 후 재조회 결과가 새 주인지 판별하는 데 쓴다.
    private var currentWeekStart: LocalDate? = null

    // 이번 화면 세션에서 닫은 결과 주차 — '확인함' 기록이 실패해도 같은 세션에서 다시 띄우지 않는다.
    private val dismissedResultWeeks = mutableSetOf<LocalDate>()

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

        viewModelScope.launch {
            when (val result = fetchRanking()) {
                is ApiResultV2.Success -> {
                    applyRanking(result.data)
                    _screenState.value = UiScreenState.Success

                    startCountdown()
                    loadPendingResult()
                }

                is ApiResultV2.SessionExpired -> Unit // 로그인 화면으로 이동 중 ([fetchRanking]에서 처리)

                else -> _screenState.value = UiScreenState.Error(result.uiMessage)
            }
        }
    }

    /**
     * 주간 랭킹을 조회한다. 세션 만료는 여기서 한 번만 처리(로그인 화면 이동)하고 결과는 그대로 돌려주므로,
     * 호출부는 성공/실패에 따른 화면 정책만 다루면 된다.
     */
    private suspend fun fetchRanking(): ApiResultV2<LeagueRanking> {
        val result = getLeagueRanking()
        if (result is ApiResultV2.SessionExpired) sessionManager.expireSession()
        return result
    }

    /** 서버 랭킹 응답을 화면 상태에 반영하고, 리셋 마감 시각을 새로 잡는다. */
    private fun applyRanking(ranking: LeagueRanking) {
        currentWeekStart = ranking.weekStart
        resetDeadlineElapsedMillis = elapsedTimeSource.elapsedRealtimeMillis() + ranking.remainingSeconds * 1_000

        val me = ranking.me
        _uiState.update {
            it.copy(
                isLoading = false,
                rankers = ranking.rankers.map { ranker -> ranker.toUiModel() },
                myNickname = me?.nickname.orEmpty(),
                // 이번 주 스낵이 0개면 서버가 rank 를 null 로 내려준다 → 아직 랭킹 미등록
                myRanker = me?.rank?.let { rank ->
                    LeagueRankerUiModel(
                        rank = rank,
                        nickname = me.nickname,
                        snackCount = me.weeklySnackCount,
                        isMe = true,
                    )
                },
                myTodaySnackCount = me?.todaySnackCount ?: 0,
                resetRemainingSeconds = ranking.remainingSeconds,
            )
        }
    }

    // 닉네임은 서버에서 이미 마스킹돼 내려온다 — 여기서는 그대로 전달하고, 포디움 카드만 표시 직전에 같은 규칙을 적용한다.
    private fun LeagueRanker.toUiModel() = LeagueRankerUiModel(
        rank = rank,
        nickname = nickname,
        snackCount = snackCount,
        isMe = isMe,
    )

    /**
     * 이미 떠 있는 리그 화면으로 다시 진입했을 때(singleTask 재진입 → onNewIntent) 최신 상태로 갱신한다.
     * - 정상 상태: 로딩 화면 없이 조용히 재조회하고, 실패하면 기존 화면을 유지한다.
     * - 에러 상태: 처음부터 다시 불러온다.
     * - 로딩 중: 이미 불러오는 중이므로 아무것도 하지 않는다. (중복 요청 방지)
     */
    fun onReenter() {
        when (_screenState.value) {
            UiScreenState.Success -> viewModelScope.launch {
                val result = fetchRanking()
                if (result is ApiResultV2.Success) {
                    applyRanking(result.data)
                    loadPendingResult()
                } // 실패해도 기존 화면 유지
            }

            is UiScreenState.Error -> loadLeague()

            UiScreenState.Idle, UiScreenState.Loading -> Unit
        }
    }

    /**
     * 리그 종료 후 첫 진입 시 보여줄 결과를 조회한다.
     * 결과가 없거나 조회에 실패하면 모달 없이 기존 화면을 그대로 유지한다.
     */
    private fun loadPendingResult() {
        viewModelScope.launch {
            when (val result = getPendingLeagueResult()) {
                is ApiResultV2.Success -> {
                    val pending = result.data ?: return@launch
                    if (pending.weekStart in dismissedResultWeeks || _uiState.value.resultModal != null) return@launch
                    // 리그가 아직 초기화되지 않은(진행 중인) 주의 결과는 모달을 띄우지 않는다.
                    if (!isLeagueWeekFinished(pending.weekStart, LocalDateTime.now(clock))) return@launch
                    _uiState.update {
                        it.copy(
                            resultModal = LeagueResultModalUi(
                                tier = pending.tier,
                                rank = pending.rank,
                                snackCount = pending.snackCount,
                                // 제목은 결과가 속한 주가 아니라 '현재 일자' 기준 월/주차를 표시한다 (9월 → 9월 5주차, 10월 → 10월 1주차)
                                weekLabel = LocalDate.now(clock).toLeagueWeekLabel(),
                                weekStart = pending.weekStart,
                            )
                        )
                    }
                }

                else -> Unit // 실패 시 모달을 띄우지 않고 기존 화면 유지
            }
        }
    }

    /** 헤더의 안내(i) 버튼 클릭 — 리그 소개 모달을 연다. */
    fun onGuideClick() {
        _uiState.update { it.copy(isGuideVisible = true) }
    }

    /** 리그 소개 모달을 닫는다. 저장하는 상태는 없다. */
    fun onGuideDismiss() {
        _uiState.update { it.copy(isGuideVisible = false) }
    }

    /** 결과 모달을 닫는다. 이후 같은 결과가 다시 노출되지 않도록 '확인함'을 기록한다. */
    fun onResultModalDismiss() {
        val modal = _uiState.value.resultModal ?: return
        _uiState.update { it.copy(resultModal = null) }
        dismissedResultWeeks += modal.weekStart
        viewModelScope.launch {
            markLeagueResultSeen(modal.weekStart) // 실패해도 이번 세션에서는 재노출하지 않는다
        }
    }

    private fun startCountdown() {
        countdownJob?.cancel()
        countdownJob = viewModelScope.launch {
            while (true) {
                delay(1_000)
                val remaining = remainingSecondsUntilReset()
                _uiState.update { it.copy(resetRemainingSeconds = remaining) }
                // 0에 도달하면 새 주 랭킹을 조용히(로딩 화면 없이) 다시 받아온다.
                if (remaining == 0L) refreshRankingAfterReset()
            }
        }
    }

    /** 마감 시각까지 남은 초(올림). 올림이라 tick 지터가 있어도 표시 숫자가 건너뛰지 않는다. */
    private fun remainingSecondsUntilReset(): Long {
        val remainingMillis = resetDeadlineElapsedMillis - elapsedTimeSource.elapsedRealtimeMillis()
        return if (remainingMillis <= 0) 0L else (remainingMillis + 999) / 1_000
    }

    /**
     * 리그 리셋 직후 새 주 랭킹을 재조회한다.
     * 서버에 아직 새 주가 반영되지 않았거나(같은 주 / 남은 시간 0 이하) 실패하면 잠시 뒤 다시 시도하고,
     * 끝내 받지 못하면 기존 목록을 유지한 채 마감 시점을 리셋 주기(7일)만큼 뒤로 밀어 카운트다운을 이어간다.
     * (기기 달력이 아니라 서버가 내려준 마감 시점을 기준으로 하므로 기기 시각·시간대에 영향받지 않는다)
     */
    private suspend fun refreshRankingAfterReset() {
        repeat(RESET_REFRESH_MAX_ATTEMPTS) { attempt ->
            when (val result = fetchRanking()) {
                is ApiResultV2.Success -> {
                    val ranking = result.data
                    if (ranking.weekStart != currentWeekStart && ranking.remainingSeconds > 0) {
                        applyRanking(ranking)
                        return
                    }
                }

                is ApiResultV2.SessionExpired -> return // 로그인 화면으로 이동 중 — 재시도하지 않는다

                else -> Unit
            }
            if (attempt < RESET_REFRESH_MAX_ATTEMPTS - 1) delay(RESET_REFRESH_RETRY_DELAY_MILLIS)
        }

        val now = elapsedTimeSource.elapsedRealtimeMillis()
        while (resetDeadlineElapsedMillis <= now) resetDeadlineElapsedMillis += LEAGUE_CYCLE_MILLIS
        _uiState.update { it.copy(resetRemainingSeconds = remainingSecondsUntilReset()) }
    }

    override fun onCleared() {
        super.onCleared()
        countdownJob?.cancel()
    }
}

sealed class LeagueUiEvent {
    data class ShareLeague(val shareText: String = DeepLinkConstants.LEAGUE_SHARE_MESSAGE) : LeagueUiEvent()
}
