package com.teumteumeat.teumteumeat.domain.model.league

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.WeekFields

// 한 주의 시작은 월요일, 그 달 1일이 포함된 주를 1주차로 센다.
// (예: 2026-09-28 → 9월 5주차, 2026-10-01 → 10월 1주차)
private val LEAGUE_WEEK_FIELDS: WeekFields = WeekFields.of(DayOfWeek.MONDAY, 1)

/** "9월 5주차" 형태의 리그 주차 라벨. */
fun LocalDate.toLeagueWeekLabel(): String =
    "${monthValue}월 ${get(LEAGUE_WEEK_FIELDS.weekOfMonth())}주차"
