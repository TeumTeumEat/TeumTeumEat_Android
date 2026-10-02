package com.teumteumeat.teumteumeat.domain.usecase.league

import com.teumteumeat.teumteumeat.data.network.model.ApiResultV2
import com.teumteumeat.teumteumeat.data.repository.league.LeagueRepository
import com.teumteumeat.teumteumeat.domain.model.league.LeagueResult
import javax.inject.Inject

/** 아직 확인하지 않은 종료 리그 결과(없으면 null)를 조회한다. */
class GetPendingLeagueResultUseCase @Inject constructor(
    private val leagueRepository: LeagueRepository,
) {
    suspend operator fun invoke(): ApiResultV2<LeagueResult?> = leagueRepository.getPendingResult()
}
