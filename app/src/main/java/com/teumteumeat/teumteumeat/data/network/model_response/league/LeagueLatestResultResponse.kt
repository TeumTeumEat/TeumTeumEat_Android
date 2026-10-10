package com.teumteumeat.teumteumeat.data.network.model_response.league

/**
 * GET /api/v1/league/results/latest 응답 — 직전 주(지난주 월 00:00 ~ 이번 주 월 00:00, KST) 리그의 내 최종 순위.
 * 직전 주만 조회할 수 있어서, 그 이전에 활동했더라도 직전 주를 쉬었다면 [rank] 는 null 이다.
 * Gson 은 Kotlin 의 non-null·기본값을 보장하지 않으므로 서버가 누락할 수 있는 필드는 nullable 로 받는다.
 */
data class LeagueLatestResultResponse(
    val weekStartDate: String, // "yyyy-MM-dd" — 결과 대상 주의 시작일(월요일). 모달 노출(주당 1회) 판단 키
    val rank: Int?,            // 최종 순위. 직전 주 스낵이 0개(한 주를 쉰 경우 포함)면 null
    val weeklySnackCount: Int, // 해당 주에 모은 스낵 수
)
