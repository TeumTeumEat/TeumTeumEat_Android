package com.teumteumeat.teumteumeat.domain.model.league

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate

class LeagueResultTest {

    @Test
    fun `1위는 금트로피 FIRST 다`() {
        assertEquals(LeagueResultTier.FIRST, LeagueResultTier.from(1))
    }

    @Test
    fun `2위부터 10위까지는 은트로피 SECOND 다`() {
        (2..LEAGUE_SILVER_TROPHY_MAX_RANK).forEach { rank ->
            assertEquals("rank=$rank", LeagueResultTier.SECOND, LeagueResultTier.from(rank))
        }
    }

    @Test
    fun `10위 밖(11위~)은 순위권 밖 연출이다`() {
        listOf(11, 12, 20).forEach { rank ->
            assertEquals("rank=$rank", LeagueResultTier.OUT_OF_RANK, LeagueResultTier.from(rank))
        }
    }

    @Test
    fun `LeagueResult tier 는 순위에서 계산된다`() {
        val result = LeagueResult(weekStart = LocalDate.of(2026, 9, 28), rank = 5, snackCount = 10)
        assertEquals(LeagueResultTier.SECOND, result.tier)
    }

    @Test
    fun `9월 마지막 주는 9월 5주차다`() {
        assertEquals("9월 5주차", LocalDate.of(2026, 9, 28).toLeagueWeekLabel())
        assertEquals("9월 5주차", LocalDate.of(2026, 9, 30).toLeagueWeekLabel())
    }

    @Test
    fun `10월 1일이 든 주는 10월 1주차다`() {
        assertEquals("10월 1주차", LocalDate.of(2026, 10, 1).toLeagueWeekLabel())
    }

    @Test
    fun `현재 일자 예시 - 9월 말은 9월 5주차 10월 초는 10월 1주차다`() {
        assertEquals("9월 5주차", LocalDate.of(2026, 9, 28).toLeagueWeekLabel())
        assertEquals("9월 5주차", LocalDate.of(2026, 9, 30).toLeagueWeekLabel())
        assertEquals("10월 1주차", LocalDate.of(2026, 10, 1).toLeagueWeekLabel())
        assertEquals("10월 1주차", LocalDate.of(2026, 10, 2).toLeagueWeekLabel())
    }

    @Test
    fun `월요일에 새 주차가 시작된다`() {
        assertEquals("10월 1주차", LocalDate.of(2026, 10, 4).toLeagueWeekLabel()) // 일요일
        assertEquals("10월 2주차", LocalDate.of(2026, 10, 5).toLeagueWeekLabel()) // 월요일
    }

    // ---- 월별 주차 표시 검증 (어느 달이든 같은 규칙이 성립하는지) ----

    private fun weekOf(date: LocalDate): Int = date.toLeagueWeekLabel().substringAfter("월 ").removeSuffix("주차").toInt()

    /** 독립적으로 계산한 기대값: (그 달 1일의 요일 오프셋 + 일 - 1) / 7 + 1, 월요일 시작. */
    private fun expectedWeek(date: LocalDate): Int {
        val offset = date.withDayOfMonth(1).dayOfWeek.value - 1 // 월=0 … 일=6
        return (date.dayOfMonth - 1 + offset) / 7 + 1
    }

    @Test
    fun `2026년부터 2035년까지 모든 날짜의 라벨이 월과 주차 규칙을 지킨다`() {
        var date = LocalDate.of(2026, 1, 1)
        val end = LocalDate.of(2035, 12, 31)
        var previous: LocalDate? = null
        while (!date.isAfter(end)) {
            val label = date.toLeagueWeekLabel()
            val week = weekOf(date)

            assertEquals("$date 의 월", "${date.monthValue}월", label.substringBefore(" "))
            assertEquals("$date 의 주차", expectedWeek(date), week)
            assertTrue("$date 주차 범위(1~6): $week", week in 1..6)
            if (date.dayOfMonth == 1) assertEquals("$date 1일은 1주차", 1, week)

            previous?.let { prev ->
                if (prev.month == date.month) {
                    // 같은 달 안에서는 월요일에만 주차가 1 증가하고, 그 외에는 유지된다.
                    val expectedDelta = if (date.dayOfWeek == DayOfWeek.MONDAY) 1 else 0
                    assertEquals("$prev → $date", expectedDelta, week - weekOf(prev))
                } else {
                    // 달이 바뀌면 항상 1주차로 시작한다.
                    assertEquals("$prev → $date", 1, week)
                }
            }
            previous = date
            date = date.plusDays(1)
        }
    }

    @Test
    fun `연말과 연초 경계도 월 기준으로 이어진다`() {
        assertEquals("12월 5주차", LocalDate.of(2026, 12, 31).toLeagueWeekLabel())
        assertEquals("1월 1주차", LocalDate.of(2027, 1, 1).toLeagueWeekLabel())
        assertEquals("1월 2주차", LocalDate.of(2027, 1, 4).toLeagueWeekLabel())
    }

    @Test
    fun `1일이 일요일이면 1주차는 하루뿐이고 6주차가 생기는 달이 있다`() {
        // 현재 규칙(월 기준)의 알려진 특성 — 변경한다면 이 테스트가 깨져서 알려준다.
        assertEquals("11월 1주차", LocalDate.of(2026, 11, 1).toLeagueWeekLabel()) // 일요일
        assertEquals("11월 2주차", LocalDate.of(2026, 11, 2).toLeagueWeekLabel())
        assertEquals("11월 6주차", LocalDate.of(2026, 11, 30).toLeagueWeekLabel())
    }
}
