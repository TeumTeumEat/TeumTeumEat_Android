package com.teumteumeat.teumteumeat.util

import com.teumteumeat.teumteumeat.data.network.model.ApiResultV2
import com.teumteumeat.teumteumeat.domain.model.league.LeagueRanking
import com.teumteumeat.teumteumeat.domain.model.league.LeagueResult
import com.teumteumeat.teumteumeat.domain.repository.league.LeagueRepository
import java.time.Clock
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

/** [today] 기준 직전 주(서버 GET /league/results/latest 의 대상 주)의 시작일(월요일). */
fun previousLeagueWeekStart(today: LocalDate): LocalDate =
    today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).minusWeeks(1)

/**
 * 서버처럼 '시각이 지나면 직전 주가 바뀌는' 지난주 결과 Repository 테스트 대역. 랭킹 조회는 지원하지 않는다.
 * [myRank] 가 null 이면 지난주 스낵 0개(결과 없음), 확인한 주는 메모리에 기록해 주당 1회만 내려준다.
 */
class FakeLeagueRepository(
    private val clock: Clock,
    private val myRank: Int?,
) : LeagueRepository {

    private val seenWeeks = mutableSetOf<LocalDate>()

    override suspend fun getLeagueRanking(): ApiResultV2<LeagueRanking> =
        ApiResultV2.UnknownError(message = "FakeLeagueRepository 는 주간 랭킹 조회를 지원하지 않습니다.")

    override suspend fun getPendingResult(): ApiResultV2<LeagueResult?> {
        val weekStart = previousLeagueWeekStart(LocalDate.now(clock))
        val result = myRank
            ?.let { LeagueResult(weekStart, rank = it, snackCount = (13 - it).coerceAtLeast(1)) }
            ?.takeIf { it.weekStart !in seenWeeks }
        return ApiResultV2.Success(message = null, data = result)
    }

    override suspend fun markResultSeen(weekStart: LocalDate): ApiResultV2<Unit> {
        seenWeeks += weekStart
        return ApiResultV2.Success(message = null, data = Unit)
    }
}
