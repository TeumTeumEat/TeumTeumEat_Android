package com.teumteumeat.teumteumeat.util

import com.teumteumeat.teumteumeat.domain.repository.league.LeagueTimeOffsetRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/** 오프셋을 메모리에 보관하는 테스트용 [LeagueTimeOffsetRepository]. */
class FakeLeagueTimeOffsetRepository(initial: Long? = null) : LeagueTimeOffsetRepository {
    private val _offsetSeconds = MutableStateFlow(initial)
    override val offsetSeconds: StateFlow<Long?> = _offsetSeconds

    override fun setOffsetSeconds(seconds: Long?) {
        _offsetSeconds.value = seconds
    }
}
