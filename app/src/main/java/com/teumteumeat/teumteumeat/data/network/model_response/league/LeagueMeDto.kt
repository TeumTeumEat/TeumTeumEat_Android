package com.teumteumeat.teumteumeat.data.network.model_response.league

/**
 * 내 주간 현황. GET /api/v1/league 응답의 `me` 와 GET /api/v1/league/me 응답 `data` 가 같은 모양이라 함께 쓴다.
 */
data class LeagueMeDto(
    val name: String,
    val weeklySnackCount: Int,
    val todaySnackCount: Int,
    val rank: Int?, // 이번 주 스낵이 0개면 null
)
