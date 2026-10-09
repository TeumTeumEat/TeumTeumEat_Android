package com.teumteumeat.teumteumeat.domain.model.league

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.TemporalAdjusters

// 리그는 매주 월요일 00:00에 초기화된다.
// 리셋까지 남은 시간은 서버(GET /api/v1/league 의 remainingSeconds)를 따르므로 여기서 계산하지 않는다.
// 아래 함수들은 지난주 결과 모달(Fake)이 쓰는 로컬 판정이다.
// TODO: GET /api/v1/league/results/latest 연동 시 서버 응답 기준으로 교체

/**
 * [today] 기준으로 가장 최근에 '종료된' 리그 주의 시작일(월요일).
 * 일요일 23:59:59까지는 진행 중인 주의 직전 주, 월요일 00:00부터는 방금 끝난 주를 가리킨다.
 */
fun lastFinishedLeagueWeekStart(today: LocalDate): LocalDate =
    today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).minusWeeks(1)

/**
 * [weekStart]로 시작하는 리그 주가 [now] 시점에 이미 종료(= 다음 월요일 00:00 초기화 완료)됐는지.
 * 아직 진행 중인 주의 결과는 결과 안내 모달 대상이 아니다.
 */
fun isLeagueWeekFinished(weekStart: LocalDate, now: LocalDateTime): Boolean =
    !now.isBefore(weekStart.plusWeeks(1).atStartOfDay())
