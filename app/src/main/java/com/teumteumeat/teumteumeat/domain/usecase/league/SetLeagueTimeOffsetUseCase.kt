package com.teumteumeat.teumteumeat.domain.usecase.league

import com.teumteumeat.teumteumeat.domain.repository.league.LeagueTimeOffsetRepository
import javax.inject.Inject

/**
 * [dev/local 전용] 리그 시각 오프셋(초)을 설정한다. 이후 리그 API 요청부터 `X-League-Time-Offset` 헤더로 전달된다.
 * [seconds] 가 null 이면 오프셋을 해제한다.
 */
class SetLeagueTimeOffsetUseCase @Inject constructor(
    private val leagueTimeOffsetRepository: LeagueTimeOffsetRepository,
) {
    operator fun invoke(seconds: Long?) = leagueTimeOffsetRepository.setOffsetSeconds(seconds)
}
