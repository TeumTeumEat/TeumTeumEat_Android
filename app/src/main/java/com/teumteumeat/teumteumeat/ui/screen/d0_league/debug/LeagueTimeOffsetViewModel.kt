package com.teumteumeat.teumteumeat.ui.screen.d0_league.debug

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teumteumeat.teumteumeat.data.network.model.ApiResultV2
import com.teumteumeat.teumteumeat.domain.usecase.league.ClearLeagueResultSeenUseCase
import com.teumteumeat.teumteumeat.domain.usecase.league.ObserveLeagueTimeOffsetUseCase
import com.teumteumeat.teumteumeat.domain.usecase.league.SetLeagueTimeOffsetUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/** '리셋 N초 전' 프리셋의 기본 N. */
const val DEFAULT_SECONDS_BEFORE_RESET = 3L

/** [dev/local 전용] 리그 시각 오프셋 입력 패널의 표시 상태. */
data class UiStateLeagueTimeOffset(
    // 현재 `X-League-Time-Offset` 으로 보내는 값(초). null 이면 보내지 않는다.
    val appliedOffsetSeconds: Long? = null,
    // 직접 입력란의 텍스트.
    val input: String = "",
) {
    // 숫자로 해석할 수 있는 입력일 때만 '적용'을 허용한다.
    val canApplyInput: Boolean
        get() = input.trim().toLongOrNull() != null
}

/**
 * [dev/local 전용] 리그 시각 오프셋을 직접 입력하거나 '리셋 N초 전' 프리셋으로 설정하는 패널의 ViewModel.
 * 값을 바꾼 뒤 리그 화면 재조회는 호출부(Activity)가 맡는다.
 */
@HiltViewModel
class LeagueTimeOffsetViewModel @Inject constructor(
    observeLeagueTimeOffset: ObserveLeagueTimeOffsetUseCase,
    private val setLeagueTimeOffset: SetLeagueTimeOffsetUseCase,
    private val clearLeagueResultSeen: ClearLeagueResultSeenUseCase,
) : ViewModel() {

    private val offsetSeconds = observeLeagueTimeOffset()
    private val input = MutableStateFlow("")

    val uiState: StateFlow<UiStateLeagueTimeOffset> =
        combine(offsetSeconds, input) { applied, text ->
            UiStateLeagueTimeOffset(appliedOffsetSeconds = applied, input = text)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UiStateLeagueTimeOffset())

    fun onInputChange(text: String) {
        input.value = text
    }

    /** 입력란의 값을 오프셋으로 적용한다. 숫자가 아니면 아무것도 하지 않는다. @return 적용했으면 true */
    fun applyInput(): Boolean {
        val seconds = input.value.trim().toLongOrNull() ?: return false
        setLeagueTimeOffset(seconds)
        return true
    }

    /**
     * 리셋 [secondsBefore]초 전이 되도록 오프셋을 맞춘다.
     * [remainingSeconds] 는 화면에 표시 중인 리셋까지 남은 시간이며, 이미 적용 중인 오프셋이 반영된 값이다.
     * 따라서 새 오프셋 = 현재 오프셋 + ([remainingSeconds] - [secondsBefore]) 로 누적해야 한다.
     * (예: 남은 시간 100초, 적용 중인 오프셋 없음 → 97초)
     */
    fun applyPresetBeforeReset(remainingSeconds: Long, secondsBefore: Long = DEFAULT_SECONDS_BEFORE_RESET) {
        val newOffset = (offsetSeconds.value ?: 0L) + remainingSeconds - secondsBefore
        input.value = newOffset.toString()
        setLeagueTimeOffset(newOffset)
    }

    /** 오프셋을 해제해 헤더를 보내지 않는다. */
    fun clearOffset() {
        input.value = ""
        setLeagueTimeOffset(null)
    }

    /**
     * 현재 계정의 '결과 확인함' 기록을 지워 같은 주 결과 모달을 다시 볼 수 있게 한다.
     * 오프셋으로 미래 시점을 만든 채 모달을 닫으면 미래 주차가 확인한 주로 남아 실제 결과가 뜨지 않으므로, 그때 초기화한다.
     * @param onResult 초기화에 성공하면 true. 화면 재조회와 토스트는 호출부가 맡는다.
     */
    fun clearResultSeenRecord(onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            onResult(clearLeagueResultSeen() is ApiResultV2.Success)
        }
    }
}
