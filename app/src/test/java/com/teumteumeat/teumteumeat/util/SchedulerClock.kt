package com.teumteumeat.teumteumeat.util

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineScheduler
import java.time.Clock
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset

/**
 * 코루틴 테스트 스케줄러의 가상 시간과 함께 흐르는 [Clock].
 * [start] 시각에서 출발해 `advanceTimeBy` 로 가상 시간을 보내면 같은 만큼 시계도 앞으로 간다.
 * (예: 일요일 23:59:00 에서 시작해 60초 진행하면 월요일 00:00:00)
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SchedulerClock(
    start: LocalDateTime,
    private val scheduler: TestCoroutineScheduler,
    private val zone: ZoneId = ZoneOffset.UTC,
) : Clock() {

    private val startInstant: Instant = start.atZone(zone).toInstant()

    override fun getZone(): ZoneId = zone

    override fun withZone(zone: ZoneId): Clock = SchedulerClock(LocalDateTime.ofInstant(startInstant, zone), scheduler, zone)

    override fun instant(): Instant = startInstant.plusMillis(scheduler.currentTime)
}
