package com.teumteumeat.teumteumeat.ui.screen.d0_league

import com.teumteumeat.teumteumeat.domain.model.league.LeagueResultTier
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** 리그 공유 문구와 링크 결합 규칙을 검증한다. */
class LeagueShareMessagesTest {

    private val link = "https://teumteumeat.co.kr/league"

    @Test
    fun `우상단 공유 버튼은 안내 문구 다음 줄에 링크를 붙인다`() {
        assertEquals(
            "퀴즈로 공부도 하고 친구랑 경쟁도 할래? 지금 바로 틈틈잇을 사용해 봐!\n$link",
            LeagueShareMessages.general(),
        )
    }

    @Test
    fun `1등 결과 문구`() {
        assertEquals(
            "이번 주 틈틈잇 간식 리그 1등 했어! 👑 나 이길 자신 있으면 지금 바로 도전해 봐!\n$link",
            LeagueShareMessages.forResult(LeagueResultTier.FIRST),
        )
    }

    @Test
    fun `2~10등 결과 문구는 TOP 10 문구다`() {
        assertEquals(
            "이번 주 틈틈잇 간식 리그 TOP 10 들었어! 🍿 다음 주엔 같이 순위 올려 볼래?\n$link",
            LeagueShareMessages.forResult(LeagueResultTier.SECOND),
        )
    }

    @Test
    fun `순위권 밖 결과 문구는 이모지 앞에 공백을 두지 않는다`() {
        assertEquals(
            "틈틈이 퀴즈 풀면서 간식 모으는 중🍿 다음 주 리그는 나랑 같이 달려 볼래?\n$link",
            LeagueShareMessages.forResult(LeagueResultTier.OUT_OF_RANK),
        )
    }

    @Test
    fun `모든 문구는 링크가 맨 마지막 줄이다`() {
        val messages = listOf(LeagueShareMessages.general()) +
            LeagueResultTier.entries.map { LeagueShareMessages.forResult(it) }

        messages.forEach { assertEquals(link, it.lines().last()) }
        assertTrue(messages.all { it.lines().size == 2 })
    }
}
