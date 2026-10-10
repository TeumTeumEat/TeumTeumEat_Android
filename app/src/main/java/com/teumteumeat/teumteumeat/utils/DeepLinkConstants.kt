package com.teumteumeat.teumteumeat.utils

/**
 * 딥링크(App Links) 관련 상수.
 *
 * LEAGUE_HOST는 AndroidManifest.xml의 DeepLinkRouterActivity intent-filter `android:host` 값과
 * 반드시 동일해야 한다 — 매니페스트는 XML이라 이 상수를 직접 참조하지 못하므로 수동으로 동기화한다.
 * `.well-known/assetlinks.json`이 해당 도메인에 배포되어 있지 않으면 App Link 검증이 되지 않아
 * 링크가 항상 브라우저로 열린다(설치 여부와 무관하게 앱으로 진입하지 못함).
 */
object DeepLinkConstants {
    const val LEAGUE_HOST = "teum-teum-eat-lab.vercel.app"
    const val LEAGUE_PATH = "/league"
    const val LEAGUE_SHARE_URL = "https://$LEAGUE_HOST$LEAGUE_PATH"

    // 카카오톡/인스타 DM 공유 시 링크와 함께 전달되는 홍보 문구
    const val LEAGUE_SHARE_MESSAGE =
        "이번 주 틈틈잇 리그 OPEN! 🏆\n틈틈이 푼 퀴즈로 스낵 모으고 리그 순위 올려봐요!\n$LEAGUE_SHARE_URL"

    // FCM data payload 에서 이동할 딥링크를 담는 키 (예: {"link": "https://teum-teum-eat-lab.vercel.app/league"}).
    // 서버(푸시 발송)와 합의된 키여야 한다. 값은 [DeepLinkRouterActivity]가 판정하므로 허용되지 않는 링크는 홈으로 간다.
    const val PUSH_DATA_KEY_LINK = "link"

    // 리그 공유 딥링크로 진입했지만 로그인/온보딩을 마쳐야 하는 유저를 위한 "로그인 후 리그로 이동" 플래그.
    // LoginActivity, OnBoardingActivity 양쪽에서 공통으로 참조하는 Intent extra 키.
    const val EXTRA_PENDING_LEAGUE_REDIRECT = "extra_pending_league_redirect"
}
