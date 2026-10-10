package com.teumteumeat.teumteumeat.domain.usecase.league

import com.teumteumeat.teumteumeat.data.network.model.ApiResultV2
import com.teumteumeat.teumteumeat.domain.repository.league.LeagueRepository
import com.teumteumeat.teumteumeat.domain.model.league.LeagueResult
import javax.inject.Inject

/** 직전 주 리그에 참여했고 아직 확인하지 않은 결과(없으면 null)를 조회한다. 직전 주보다 이전 주의 결과는 조회할 수 없다. */
class GetPendingLeagueResultUseCase @Inject constructor(
    private val leagueRepository: LeagueRepository,
) {
    suspend operator fun invoke(): ApiResultV2<LeagueResult?> = leagueRepository.getPendingResult()
}
