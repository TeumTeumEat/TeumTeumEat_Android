package com.teumteumeat.teumteumeat.ui.screen.d0_league.component

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LeagueResultLottiePlacementTest {

    private val delta = 0.001f

    @Test
    fun `받침 하단이 영역 맨 아래에 닿는다`() {
        listOf(
            120f to 140f,
            280f to 190f,
            300f to 400f,
            240f to 90f,
        ).forEach { (width, height) ->
            val placement = trophyLottiePlacement(width, height)
            val scale = placement.sizeDp / TrophyArt.CANVAS_SIZE

            assertEquals(
                "area=${width}x$height",
                height,
                placement.offsetYDp + TrophyArt.BASE_BOTTOM * scale,
                delta,
            )
        }
    }

    @Test
    fun `후광 가로폭이 영역 폭을 넘지 않는다`() {
        val placement = trophyLottiePlacement(areaWidthDp = 200f, areaHeightDp = 600f)
        val scale = placement.sizeDp / TrophyArt.CANVAS_SIZE

        assertTrue(TrophyArt.ART_WIDTH * scale <= 200f + delta)
    }

    @Test
    fun `반짝임 위쪽 끝이 영역 위로 나가지 않는다`() {
        val placement = trophyLottiePlacement(areaWidthDp = 600f, areaHeightDp = 150f)
        val scale = placement.sizeDp / TrophyArt.CANVAS_SIZE

        assertTrue(placement.offsetYDp + TrophyArt.ART_TOP * scale >= -delta)
    }

    @Test
    fun `가로가 넉넉하면 높이에 맞춰 키운다`() {
        val placement = trophyLottiePlacement(areaWidthDp = 600f, areaHeightDp = 188.4f)

        // 받침 하단(203.4) - 위쪽 끝(15) = 188.4 가 영역 높이와 같을 때 배율 1.0
        assertEquals(TrophyArt.CANVAS_SIZE, placement.sizeDp, delta)
    }
}
