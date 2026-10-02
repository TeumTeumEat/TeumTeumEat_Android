package com.teumteumeat.teumteumeat.domain.usecase.league

import com.teumteumeat.teumteumeat.data.network.model.ApiResultV2
import com.teumteumeat.teumteumeat.data.repository.league.LeagueRepository
import java.time.LocalDate
import javax.inject.Inject

/** 결과 안내 모달을 닫은 주차를 '확인함'으로 기록한다. */
class MarkLeagueResultSeenUseCase @Inject constructor(
    private val leagueRepository: LeagueRepository,
) {
    suspend operator fun invoke(weekStart: LocalDate): ApiResultV2<Unit> =
        leagueRepository.markResultSeen(weekStart)
}
