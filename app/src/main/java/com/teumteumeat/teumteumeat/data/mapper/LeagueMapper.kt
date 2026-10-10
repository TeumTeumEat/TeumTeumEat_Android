package com.teumteumeat.teumteumeat.data.mapper

import com.teumteumeat.teumteumeat.data.network.model_response.league.LeagueLatestResultResponse
import com.teumteumeat.teumteumeat.data.network.model_response.league.LeagueMeDto
import com.teumteumeat.teumteumeat.data.network.model_response.league.LeagueRankerDto
import com.teumteumeat.teumteumeat.data.network.model_response.league.LeagueRankingResponse
import com.teumteumeat.teumteumeat.domain.model.league.LeagueMe
import com.teumteumeat.teumteumeat.domain.model.league.LeagueRanker
import com.teumteumeat.teumteumeat.domain.model.league.LeagueRanking
import com.teumteumeat.teumteumeat.domain.model.league.LeagueResult
import java.time.LocalDate

fun LeagueRankingResponse.toDomain(): LeagueRanking = LeagueRanking(
    weekStart = LocalDate.parse(weekStartDate),
    remainingSeconds = remainingSeconds.coerceAtLeast(0L),
    rankers = rankers.orEmpty().map { it.toDomain() },
    me = me?.toDomain(),
)

private fun LeagueRankerDto.toDomain() = LeagueRanker(
    rank = rank,
    nickname = name,
    snackCount = weeklySnackCount,
    isMe = isMe,
)

/** 주간 랭킹의 `me` 와 GET /league/me 응답이 함께 쓰는 변환. */
fun LeagueMeDto.toDomain() = LeagueMe(
    nickname = name,
    weeklySnackCount = weeklySnackCount,
    todaySnackCount = todaySnackCount,
    rank = rank,
)

/**
 * 직전 주 결과 → 도메인. 직전 주 스낵이 0개라 `rank` 가 null 이면(한 주를 쉬고 진입한 경우 포함) 보여줄 결과가 없으므로 null 을 돌려준다.
 */
fun LeagueLatestResultResponse.toDomain(): LeagueResult? = rank?.let {
    LeagueResult(
        weekStart = LocalDate.parse(weekStartDate),
        rank = it,
        snackCount = weeklySnackCount,
    )
}
