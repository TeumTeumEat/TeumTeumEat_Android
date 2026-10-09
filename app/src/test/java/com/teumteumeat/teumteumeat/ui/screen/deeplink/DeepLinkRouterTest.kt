package com.teumteumeat.teumteumeat.ui.screen.deeplink

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** FCM `link` 값이 리그 딥링크로 인정되는 조건을 검증한다. (스플래시의 리그 진입 분기 기준) */
class DeepLinkRouterTest {

    @Test
    fun `리그 공유 링크는 리그 딥링크로 인정된다`() {
        assertTrue(DeepLinkRouter.isLeagueLink("https://teum-teum-eat-lab.vercel.app/league"))
        assertTrue(DeepLinkRouter.isLeagueLink("  https://teum-teum-eat-lab.vercel.app/league  "))
    }

    @Test
    fun `쿼리 문자열이 붙어도 경로가 같으면 인정된다`() {
        assertTrue(DeepLinkRouter.isLeagueLink("https://teum-teum-eat-lab.vercel.app/league?from=push"))
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
        assertFalse(DeepLinkRouter.isLeagueLink("http://teum-teum-eat-lab.vercel.app/league"))
        assertFalse(DeepLinkRouter.isLeagueLink("https://evil.example.com/league"))
        assertFalse(DeepLinkRouter.isLeagueLink("https://teum-teum-eat-lab.vercel.app/leagues"))
        assertFalse(DeepLinkRouter.isLeagueLink("https://teum-teum-eat-lab.vercel.app/"))
    }
}
