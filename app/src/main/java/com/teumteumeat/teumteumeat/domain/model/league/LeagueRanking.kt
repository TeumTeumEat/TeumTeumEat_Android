package com.teumteumeat.teumteumeat.domain.model.league

import java.time.LocalDate

/**
 * 주간 리그에서 한 유저가 모을 수 있는 최대 스낵 수 (기획서 기준).
 *
 * 산식: 하루 최대 11회(기본 1회 + 쿠폰 10회) × 7일 = 77. 퀴즈 세트 1회 완료 = 스낵 1개.
 * 쿠폰 한도(10회)는 `CupponModal` 의 "하루 최대 10번 사용 가능" 과 같은 기획 규칙이므로, 쿠폰 한도가 바뀌면 이 값도 함께 확인한다.
 *
 * 서버 GET /api/v1/league 응답에는 내려오지 않으므로 클라이언트 상수로 두며, 기획이 바뀌면 이 값만 고친다.
 * 리그 화면 상단 문구("매주 최대 N개의 스낵을 모을 수 있어요")에 사용한다.
 */
const val LEAGUE_MAX_WEEKLY_SNACK_COUNT = 77

/**
 * 이번 주 주간 리그 랭킹.
 * [remainingSeconds]는 응답을 받은 시점 기준으로 리그 리셋(다음 월요일 00:00)까지 남은 초다.
 */
data class LeagueRanking(
    val weekStart: LocalDate,
    val remainingSeconds: Long,
    val rankers: List<LeagueRanker>,
    val me: LeagueMe?,
)

/** 랭킹에 노출되는 한 유저. [nickname]은 서버에서 이미 마스킹돼 내려온다. */
data class LeagueRanker(
    val rank: Int,
    val nickname: String,
    val snackCount: Int,
    val isMe: Boolean,
)

/**
 * 내 주간 현황. 이번 주 스낵이 0개면 [rank]는 null(=랭킹 미등록)이다.
 * 주간 랭킹(GET /league)의 `me` 와 내 순위 조회(GET /league/me) 응답에 함께 쓴다. [nickname]은 서버에서 마스킹돼 내려온다.
 */
data class LeagueMe(
    val nickname: String,
    val weeklySnackCount: Int,
    val todaySnackCount: Int,
    val rank: Int?,
)
