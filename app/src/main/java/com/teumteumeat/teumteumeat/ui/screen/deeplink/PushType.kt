package com.teumteumeat.teumteumeat.ui.screen.deeplink

import com.teumteumeat.teumteumeat.utils.firebase.TeumAnalyticsEvent

/**
 * 서버가 FCM `data.type` 에 담아 보내는 푸시 알림 종류.
 * Android 프레임워크에 의존하지 않는 순수 Kotlin 이라 단위 테스트가 가능하다.
 *
 * | type             | 알림                    | 발송 시점 (KST)                       |
 * |------------------|------------------------|--------------------------------------|
 * | DAILY_QUIZ       | 학습 알림               | 유저가 설정한 시간 (오늘 퀴즈를 푼 유저 제외) |
 * | LEAGUE_DEADLINE  | 주간 간식 리그 마감 임박  | 일 20:00                              |
 * | LEAGUE_RESULT    | 지난주 주간 간식 리그 결과 | 월 08:00 (지난주 순위에 든 유저만)       |
 *
 * @property value 서버가 보내는 대문자 문자열
 * @property opensLeague true 면 알림 탭 시 홈 → 리그 순으로 이동한다. false 면 홈으로 진입한다.
 */
enum class PushType(val value: String, val opensLeague: Boolean) {
    DAILY_QUIZ("DAILY_QUIZ", opensLeague = false),
    LEAGUE_DEADLINE("LEAGUE_DEADLINE", opensLeague = true),
    LEAGUE_RESULT("LEAGUE_RESULT", opensLeague = true);

    companion object {
        /** 서버 문자열을 [PushType] 으로 변환한다. 알 수 없는 값이거나 null 이면 null (호출 측에서 홈으로 처리). */
        fun fromValue(value: String?): PushType? =
            entries.firstOrNull { it.value == value?.trim() }

        /** GA 파라미터 값 최대 길이 (Firebase Analytics 제한) */
        private const val ANALYTICS_VALUE_MAX_LENGTH = 100

        /**
         * GA `push_type` 파라미터 값. 서버 값을 변환하지 않고 그대로 쓴다 — 서버가 새 type 을 추가해도
         * 앱 배포 없이 GA 에 잡히도록 [PushType] 에 없는 값도 그대로 보낸다 (iOS 와 동일 규칙).
         * 값이 없거나 비어 있을 때만 [TeumAnalyticsEvent.PushOpen.PUSH_TYPE_UNKNOWN].
         */
        fun toAnalyticsValue(raw: String?): String =
            raw?.takeIf { it.isNotBlank() }?.take(ANALYTICS_VALUE_MAX_LENGTH)
                ?: TeumAnalyticsEvent.PushOpen.PUSH_TYPE_UNKNOWN
    }
}
