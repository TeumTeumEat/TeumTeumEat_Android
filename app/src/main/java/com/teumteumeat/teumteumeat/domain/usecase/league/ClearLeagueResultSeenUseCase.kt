package com.teumteumeat.teumteumeat.domain.usecase.league

import com.teumteumeat.teumteumeat.data.network.model.ApiResultV2
import com.teumteumeat.teumteumeat.domain.repository.league.LeagueRepository
import javax.inject.Inject

/** [dev/local 전용] 현재 계정의 리그 결과 '확인함' 기록을 초기화해 같은 주 결과 모달을 다시 볼 수 있게 한다. */
class ClearLeagueResultSeenUseCase @Inject constructor(
    private val leagueRepository: LeagueRepository,
) {
    suspend operator fun invoke(): ApiResultV2<Unit> = leagueRepository.clearResultSeen()
}
