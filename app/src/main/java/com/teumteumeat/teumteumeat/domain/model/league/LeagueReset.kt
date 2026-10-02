package com.teumteumeat.teumteumeat.domain.model.league

import java.time.DayOfWeek
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.TemporalAdjusters

// 리그는 매주 월요일 00:00에 초기화된다.
// TODO: 실제 리그 API가 준비되면 서버가 내려주는 초기화 시각/카운트를 사용하도록 교체

/** [now] 기준 다음 리그 초기화(다음 월요일 00:00)까지 남은 초. 월요일 00:00:00 정각이면 7일(604800초)이다. */
fun secondsUntilNextLeagueReset(now: LocalDateTime): Long {
    val nextReset = now.toLocalDate()
        .with(TemporalAdjusters.next(DayOfWeek.MONDAY))
        .atStartOfDay()
    return Duration.between(now, nextReset).seconds
}

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
