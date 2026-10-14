package com.teumteumeat.teumteumeat.data.repository.league

import com.teumteumeat.teumteumeat.domain.repository.league.LeagueTimeOffsetRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/** 리그 시각 오프셋을 프로세스 메모리에 보관하는 구현. 요청 헤더에는 [com.teumteumeat.teumteumeat.data.network.interceptor.LeagueTimeOffsetInterceptor] 가 싣는다. */
@Singleton
class LeagueTimeOffsetRepositoryImpl @Inject constructor() : LeagueTimeOffsetRepository {

    private val _offsetSeconds = MutableStateFlow<Long?>(null)
    override val offsetSeconds: StateFlow<Long?> = _offsetSeconds.asStateFlow()

    override fun setOffsetSeconds(seconds: Long?) {
        _offsetSeconds.value = seconds
    }
}
