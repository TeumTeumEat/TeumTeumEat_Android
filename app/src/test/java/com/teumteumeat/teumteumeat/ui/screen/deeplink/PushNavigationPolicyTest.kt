package com.teumteumeat.teumteumeat.ui.screen.deeplink

import org.junit.Assert.assertEquals
import org.junit.Test

/** 푸시 알림 탭의 이동 정책(iOS 와 동일)을 검증한다. */
class PushNavigationPolicyTest {

    private val leagueLink = "https://teumteumeat.co.kr/league"

    @Test
    fun `리그 알림은 리그로 이동한다`() {
        assertEquals(PushTarget.LEAGUE, PushNavigationPolicy.resolve("LEAGUE_DEADLINE", null, false))
        assertEquals(PushTarget.LEAGUE, PushNavigationPolicy.resolve("LEAGUE_RESULT", null, false))
    }

    @Test
    fun `학습 알림은 홈으로 이동한다`() {
        assertEquals(PushTarget.HOME, PushNavigationPolicy.resolve("DAILY_QUIZ", null, false))
    }

    @Test
    fun `모르는 type 이거나 type 이 없으면 이동하지 않는다`() {
        assertEquals(PushTarget.NONE, PushNavigationPolicy.resolve("NEW_SERVER_TYPE", null, false))
        assertEquals(PushTarget.NONE, PushNavigationPolicy.resolve("", null, false))
        assertEquals(PushTarget.NONE, PushNavigationPolicy.resolve(null, null, false))
    }

    @Test
    fun `퀴즈 주제 추가 새 목표 설정 중에는 type 과 무관하게 이동하지 않는다`() {
        assertEquals(PushTarget.NONE, PushNavigationPolicy.resolve("LEAGUE_RESULT", null, true))
        assertEquals(PushTarget.NONE, PushNavigationPolicy.resolve("LEAGUE_DEADLINE", null, true))
        assertEquals(PushTarget.NONE, PushNavigationPolicy.resolve("DAILY_QUIZ", null, true))
        assertEquals(PushTarget.NONE, PushNavigationPolicy.resolve("NEW_SERVER_TYPE", leagueLink, true))
    }

    @Test
    fun `link 가 리그 딥링크면 type 이 없어도 리그로 이동한다`() {
        assertEquals(PushTarget.LEAGUE, PushNavigationPolicy.resolve(null, leagueLink, false))
    }
}
