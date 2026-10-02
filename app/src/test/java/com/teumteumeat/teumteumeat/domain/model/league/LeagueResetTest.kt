package com.teumteumeat.teumteumeat.domain.model.league

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * 리그 초기화(매주 월요일 00:00) 경계 테스트.
 * 2026-10-04 는 일요일, 2026-10-05 는 월요일이다.
 */
class LeagueResetTest {

    private val sunday = LocalDate.of(2026, 10, 4)
    private val monday = LocalDate.of(2026, 10, 5)

    @Test
    fun `일요일 23시 59분 00초에는 초기화까지 60초가 남는다`() {
        assertEquals(60L, secondsUntilNextLeagueReset(sunday.atTime(23, 59, 0)))
    }

    @Test
    fun `일요일 23시 59분 59초에는 1초가 남는다`() {
        assertEquals(1L, secondsUntilNextLeagueReset(sunday.atTime(23, 59, 59)))
    }

    @Test
    fun `월요일 00시 00분 00초에는 다음 초기화까지 7일이 남는다`() {
        assertEquals(7 * 24 * 3600L, secondsUntilNextLeagueReset(monday.atStartOfDay()))
    }

    @Test
    fun `월요일 00시 00분 01초에는 7일에서 1초 줄어든다`() {
        assertEquals(7 * 24 * 3600L - 1, secondsUntilNextLeagueReset(LocalDateTime.of(2026, 10, 5, 0, 0, 1)))
    }

    @Test
    fun `초기화 직전(일요일)에는 직전 주가 종료된 가장 최근 주다`() {
        val weekStart = lastFinishedLeagueWeekStart(sunday)

        assertEquals(LocalDate.of(2026, 9, 21), weekStart)
        assertEquals("9월 4주차", weekStart.toLeagueWeekLabel())
    }

    @Test
    fun `초기화 직후(월요일)에는 방금 끝난 주로 바뀐다`() {
        val weekStart = lastFinishedLeagueWeekStart(monday)

        assertEquals(LocalDate.of(2026, 9, 28), weekStart)
        assertEquals("9월 5주차", weekStart.toLeagueWeekLabel())
    }

    @Test
    fun `리그 주는 다음 월요일 00시 00분 00초에 종료된 것으로 본다`() {
        val weekStart = LocalDate.of(2026, 9, 28) // 월요일 — 이 주는 10/4(일) 23:59:59 까지

        assertFalse(isLeagueWeekFinished(weekStart, sunday.atTime(23, 59, 59)))
        assertTrue(isLeagueWeekFinished(weekStart, monday.atStartOfDay()))
        assertTrue(isLeagueWeekFinished(weekStart, monday.atTime(0, 0, 1)))
    }

    @Test
    fun `진행 중인 주는 종료되지 않았다`() {
        assertFalse(isLeagueWeekFinished(LocalDate.of(2026, 9, 28), LocalDateTime.of(2026, 9, 28, 0, 0, 0)))
        assertFalse(isLeagueWeekFinished(LocalDate.of(2026, 9, 28), LocalDateTime.of(2026, 10, 2, 12, 0, 0)))
    }
}
