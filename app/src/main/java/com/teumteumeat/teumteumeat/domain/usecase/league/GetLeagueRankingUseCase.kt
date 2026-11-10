package com.teumteumeat.teumteumeat.domain.usecase.league

import com.teumteumeat.teumteumeat.data.network.model.ApiResultV2
import com.teumteumeat.teumteumeat.domain.repository.league.LeagueRepository
import com.teumteumeat.teumteumeat.domain.model.league.LeagueRanking
import javax.inject.Inject

/** 이번 주 주간 리그 랭킹(상위 10명 + 내 순위 + 리셋까지 남은 시간)을 조회한다. */
class GetLeagueRankingUseCase @Inject constructor(
    private val leagueRepository: LeagueRepository,
) {
    suspend operator fun invoke(): ApiResultV2<LeagueRanking> = leagueRepository.getLeagueRanking()
}
