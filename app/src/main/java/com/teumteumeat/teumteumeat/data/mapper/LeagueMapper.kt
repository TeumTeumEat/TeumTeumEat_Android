package com.teumteumeat.teumteumeat.data.mapper

import com.teumteumeat.teumteumeat.data.network.model_response.league.LeagueMeDto
import com.teumteumeat.teumteumeat.data.network.model_response.league.LeagueRankerDto
import com.teumteumeat.teumteumeat.data.network.model_response.league.LeagueRankingResponse
import com.teumteumeat.teumteumeat.domain.model.league.LeagueMe
import com.teumteumeat.teumteumeat.domain.model.league.LeagueRanker
import com.teumteumeat.teumteumeat.domain.model.league.LeagueRanking
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

private fun LeagueMeDto.toDomain() = LeagueMe(
    nickname = name,
    weeklySnackCount = weeklySnackCount,
    todaySnackCount = todaySnackCount,
    rank = rank,
)
