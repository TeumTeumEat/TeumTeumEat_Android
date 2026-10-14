package com.teumteumeat.teumteumeat.domain.usecase.league

import com.teumteumeat.teumteumeat.domain.repository.league.LeagueTimeOffsetRepository
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

/** [dev/local 전용] 현재 적용 중인 리그 시각 오프셋(초)을 관찰한다. null 이면 적용 중인 오프셋이 없다. */
class ObserveLeagueTimeOffsetUseCase @Inject constructor(
    private val leagueTimeOffsetRepository: LeagueTimeOffsetRepository,
) {
    operator fun invoke(): StateFlow<Long?> = leagueTimeOffsetRepository.offsetSeconds
}
