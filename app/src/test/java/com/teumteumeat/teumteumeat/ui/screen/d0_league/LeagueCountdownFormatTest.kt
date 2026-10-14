package com.teumteumeat.teumteumeat.ui.screen.d0_league

import org.junit.Assert.assertEquals
import org.junit.Test

class LeagueCountdownFormatTest {

    @Test
    fun `1일 이상 남으면 n일 HH_mm 으로 표시하고 초는 생략한다`() {
        assertEquals("1일 10:31", formatCountdown(86_400L + 10 * 3600L + 31 * 60L + 59L))
    }

    @Test
    fun `1일 이상이고 시간이 0이면 시와 분을 두 자리로 맞춘다`() {
        assertEquals("1일 00:01", formatCountdown(86_400L + 60L))
    }

    @Test
    fun `정확히 1일 남으면 1일 00_00 으로 표시한다`() {
        assertEquals("1일 00:00", formatCountdown(86_400L))
    }

    @Test
    fun `1일 미만이면 HH_mm_ss 로 표시한다`() {
        assertEquals("10:31:01", formatCountdown(10 * 3600L + 31 * 60L + 1L))
        assertEquals("23:59:59", formatCountdown(86_399L))
    }

    @Test
    fun `0초면 00_00_00 으로 표시한다`() {
        assertEquals("00:00:00", formatCountdown(0L))
    }
}
