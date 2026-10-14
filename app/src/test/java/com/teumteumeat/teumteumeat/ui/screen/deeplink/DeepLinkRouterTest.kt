package com.teumteumeat.teumteumeat.ui.screen.deeplink

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** FCM `link` 값이 리그 딥링크로 인정되는 조건을 검증한다. (스플래시의 리그 진입 분기 기준) */
class DeepLinkRouterTest {

    @Test
    fun `리그 공유 링크는 리그 딥링크로 인정된다`() {
        assertTrue(DeepLinkRouter.isLeagueLink("https://teumteumeat.co.kr/league"))
        assertTrue(DeepLinkRouter.isLeagueLink("  https://teumteumeat.co.kr/league  "))
    }

    @Test
    fun `쿼리 문자열이 붙어도 경로가 같으면 인정된다`() {
        assertTrue(DeepLinkRouter.isLeagueLink("https://teumteumeat.co.kr/league?from=push"))
    }

    @Test
    fun `null 빈 문자열 형식이 잘못된 값은 인정하지 않는다`() {
        assertFalse(DeepLinkRouter.isLeagueLink(null))
        assertFalse(DeepLinkRouter.isLeagueLink(""))
        assertFalse(DeepLinkRouter.isLeagueLink("   "))
        assertFalse(DeepLinkRouter.isLeagueLink("not a url ::"))
    }

    @Test
    fun `다른 호스트 스킴 경로는 인정하지 않는다`() {
        assertFalse(DeepLinkRouter.isLeagueLink("http://teumteumeat.co.kr/league"))
        assertFalse(DeepLinkRouter.isLeagueLink("https://evil.example.com/league"))
        assertFalse(DeepLinkRouter.isLeagueLink("https://teumteumeat.co.kr/leagues"))
        assertFalse(DeepLinkRouter.isLeagueLink("https://teumteumeat.co.kr/"))
    }

    @Test
    fun `리그 알림 type 은 link 없이도 리그 진입으로 판정한다`() {
        assertTrue(DeepLinkRouter.isLeaguePush(link = null, type = "LEAGUE_DEADLINE"))
        assertTrue(DeepLinkRouter.isLeaguePush(link = null, type = "LEAGUE_RESULT"))
        assertTrue(DeepLinkRouter.isLeaguePush(link = null, type = " LEAGUE_RESULT "))
    }

    @Test
    fun `학습 알림과 알 수 없는 type 은 리그 진입이 아니다`() {
        assertFalse(DeepLinkRouter.isLeaguePush(link = null, type = "DAILY_QUIZ"))
        assertFalse(DeepLinkRouter.isLeaguePush(link = null, type = "UNKNOWN_TYPE"))
        assertFalse(DeepLinkRouter.isLeaguePush(link = null, type = "league_result"))
        assertFalse(DeepLinkRouter.isLeaguePush(link = null, type = null))
        assertFalse(DeepLinkRouter.isLeaguePush(link = "", type = ""))
    }

    @Test
    fun `type 이 학습 알림이어도 link 가 리그 딥링크면 리그 진입이다`() {
        assertTrue(
            DeepLinkRouter.isLeaguePush(
                link = "https://teumteumeat.co.kr/league",
                type = "DAILY_QUIZ",
            )
        )
    }

    @Test
    fun `PushType 은 서버 문자열을 해석한다`() {
        assertTrue(PushType.fromValue("DAILY_QUIZ") == PushType.DAILY_QUIZ)
        assertTrue(PushType.fromValue("LEAGUE_DEADLINE") == PushType.LEAGUE_DEADLINE)
        assertTrue(PushType.fromValue("LEAGUE_RESULT") == PushType.LEAGUE_RESULT)
        assertTrue(PushType.fromValue("nope") == null)
    }

    @Test
    fun `GA push_type 은 서버 값을 변환하지 않고 그대로 쓴다`() {
        assertEquals("DAILY_QUIZ", PushType.toAnalyticsValue("DAILY_QUIZ"))
        assertEquals("LEAGUE_DEADLINE", PushType.toAnalyticsValue("LEAGUE_DEADLINE"))
        assertEquals("LEAGUE_RESULT", PushType.toAnalyticsValue("LEAGUE_RESULT"))
        // 서버가 새 type 을 추가해도 앱 배포 없이 GA 에 잡히도록 모르는 값도 그대로 보낸다
        assertEquals("NEW_SERVER_TYPE", PushType.toAnalyticsValue("NEW_SERVER_TYPE"))
        assertEquals("league_result", PushType.toAnalyticsValue("league_result"))
    }

    @Test
    fun `GA push_type 은 type 이 없거나 비어 있을 때만 unknown 이다`() {
        assertEquals("unknown", PushType.toAnalyticsValue(null))
        assertEquals("unknown", PushType.toAnalyticsValue(""))
        assertEquals("unknown", PushType.toAnalyticsValue("   "))
    }

    @Test
    fun `GA push_type 은 100자를 넘으면 잘라서 보낸다`() {
        assertEquals(100, PushType.toAnalyticsValue("A".repeat(150)).length)
    }
}
