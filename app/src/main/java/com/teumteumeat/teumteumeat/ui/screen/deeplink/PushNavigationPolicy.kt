package com.teumteumeat.teumteumeat.ui.screen.deeplink

/** 푸시 알림 탭 후 앱이 이동해야 할 곳. */
enum class PushTarget {
    /** 홈으로 이동 (학습 알림) */
    HOME,

    /** 홈 → 리그 순으로 이동 (리그 알림) */
    LEAGUE,

    /** 화면을 옮기지 않는다 (진행 중인 화면 유지). GA `push_open` 의 destination 은 "none" */
    NONE,
}

/**
 * 푸시 알림 탭의 이동 정책 (iOS 와 동일). Android 프레임워크에 의존하지 않는 순수 Kotlin 이라 단위 테스트가 가능하다.
 */
object PushNavigationPolicy {

    /**
     * @param type 서버 FCM `data.type` (없으면 null)
     * @param link FCM `data.link` (서버는 현재 보내지 않음)
     * @param isNoInterruptScreenActive 퀴즈 / 주제 추가 / 새 목표 설정처럼 진행을 끊으면 안 되는 화면이 떠 있는지
     *
     * - 끊으면 안 되는 화면이 떠 있으면 type 과 무관하게 [PushTarget.NONE]
     * - 리그 알림(또는 리그 링크)은 [PushTarget.LEAGUE], 학습 알림은 [PushTarget.HOME]
     * - 모르는 type 이거나 type 이 없으면 [PushTarget.NONE]
     */
    fun resolve(type: String?, link: String?, isNoInterruptScreenActive: Boolean): PushTarget = when {
        isNoInterruptScreenActive -> PushTarget.NONE
        DeepLinkRouter.isLeaguePush(link, type) -> PushTarget.LEAGUE
        PushType.fromValue(type) != null -> PushTarget.HOME
        else -> PushTarget.NONE
    }
}
