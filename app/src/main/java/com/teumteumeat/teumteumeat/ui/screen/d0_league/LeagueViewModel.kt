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
import javax.inject.Inject

// TODO: 실제 리그 API가 준비되면 LeagueRepository/UseCase를 경유하도록 교체
private const val MOCK_RESET_REMAINING_SECONDS = 23 * 3600L + 20 * 60L + 10L

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
        val myRanker = MOCK_RANKERS.first { it.isMe }

        _uiState.update {
            it.copy(
                isLoading = false,
                rankers = MOCK_RANKERS,
                myRanker = myRanker,
                myTodaySnackCount = 1,
                resetRemainingSeconds = MOCK_RESET_REMAINING_SECONDS,
            )
        }
        _screenState.value = UiScreenState.Success

        startCountdown()
    }

    private fun startCountdown() {
        countdownJob?.cancel()
        countdownJob = viewModelScope.launch {
            while (_uiState.value.resetRemainingSeconds > 0) {
                delay(1_000)
                _uiState.update {
                    it.copy(resetRemainingSeconds = (it.resetRemainingSeconds - 1).coerceAtLeast(0))
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        countdownJob?.cancel()
    }
}
