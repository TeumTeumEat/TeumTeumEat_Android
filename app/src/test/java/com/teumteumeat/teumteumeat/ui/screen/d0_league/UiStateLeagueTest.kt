package com.teumteumeat.teumteumeat.ui.screen.d0_league

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** [UiStateLeague.hasFirstPlace] — 주간 랭킹 초기화 직후 1위 미등록 상태 판단 기준 검증. */
class UiStateLeagueTest {

    private fun ranker(rank: Int) = LeagueRankerUiModel(rank = rank, nickname = "이*민", snackCount = 10)

    @Test
    fun `랭킹이 비어 있으면 1위가 없다`() {
        assertFalse(UiStateLeague(rankers = emptyList()).hasFirstPlace)
    }

    @Test
    fun `rank 1이 있으면 1위가 있다`() {
        assertTrue(UiStateLeague(rankers = listOf(ranker(1), ranker(2))).hasFirstPlace)
    }

    @Test
    fun `rank 1 없이 2위 이하만 있으면 1위가 없다`() {
        assertFalse(UiStateLeague(rankers = listOf(ranker(2), ranker(3))).hasFirstPlace)
    }

    @Test
    fun `공동 1위도 1위가 있는 것으로 본다`() {
        assertTrue(UiStateLeague(rankers = listOf(ranker(1), ranker(1), ranker(3))).hasFirstPlace)
    }

    @Test
    fun `랭킹이 비어 있으면 4~10위 빈 행을 채운다`() {
        val rows = UiStateLeague(rankers = emptyList()).restRowsWithPlaceholder
        assertEquals((4..10).toList(), rows.map { it.rank })
        assertTrue(rows.all { it.isEmpty && it.snackCountText == "-" })
    }

    @Test
    fun `등록된 유저가 부족하면 다음 순위부터 빈 행으로 채운다`() {
        val rankers = listOf(1, 2, 3, 4, 5, 5, 7).map(::ranker)
        val rows = UiStateLeague(rankers = rankers).restRowsWithPlaceholder
        assertEquals(listOf(4, 5, 5, 7, 8, 9, 10), rows.map { it.rank })
        assertEquals(listOf(false, false, false, false, true, true, true), rows.map { it.isEmpty })
    }
}
