package com.teumteumeat.teumteumeat.domain.model.league

import java.time.LocalDate

/** 금트로피 연출을 쓰는 최하위 순위(2~10위, 리그 화면에 노출되는 순위권). 이보다 낮은 11위부터는 순위권 밖 연출을 쓴다. */
const val LEAGUE_GOLD_TROPHY_MAX_RANK = 10

/**
 * 직전 주 주간 리그에서 내가 거둔 결과.
 * [weekStart]는 해당 리그 주의 시작일(월요일)이며, 결과 안내 모달을 한 번만 보여주기 위한 식별자로도 쓴다.
 */
data class LeagueResult(
    val weekStart: LocalDate,
    val rank: Int,
    val snackCount: Int,
) {
    val tier: LeagueResultTier
        get() = LeagueResultTier.from(rank)
}

/**
 * 결과 안내 모달의 연출·문구를 가르는 순위 구간.
 * 1위([FIRST])와 2~10위([SECOND])는 금트로피(문구만 다름), 10위 밖(11위~)은 순위권 밖 연출([OUT_OF_RANK]).
 */
enum class LeagueResultTier {
    FIRST,
    SECOND,
    OUT_OF_RANK;

    companion object {
        fun from(rank: Int): LeagueResultTier = when {
            rank <= 1 -> FIRST
            rank <= LEAGUE_GOLD_TROPHY_MAX_RANK -> SECOND
            else -> OUT_OF_RANK
        }
    }
}
