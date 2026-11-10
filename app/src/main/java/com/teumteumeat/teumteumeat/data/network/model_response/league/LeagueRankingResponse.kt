package com.teumteumeat.teumteumeat.data.network.model_response.league

/**
 * GET /api/v1/league 응답.
 * Gson 은 Kotlin 의 non-null·기본값을 보장하지 않으므로 서버가 누락할 수 있는 필드는 nullable 로 받는다.
 * 서버가 함께 내려주는 `resetAt`(리셋 시각)은 앱에서 쓰지 않아 선언하지 않는다 — 카운트다운은 [remainingSeconds] 기준.
 */
data class LeagueRankingResponse(
    val weekStartDate: String,     // "yyyy-MM-dd" — 이번 리그 주의 시작일(월요일)
    val remainingSeconds: Long,    // 리셋까지 남은 초
    val rankers: List<LeagueRankerDto>?, // 상위 10명 (닉네임은 서버에서 마스킹)
    val me: LeagueMeDto?,
)

data class LeagueRankerDto(
    val rank: Int,
    val name: String,
    val weeklySnackCount: Int,
    val isMe: Boolean,
)
