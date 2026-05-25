package com.teumteumeat.teumteumeat.utils.firebase

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Firebase Analytics 전용 로거
 *
 * - 이벤트 이름/파라미터 키는 [TeumAnalyticsEvent]에서 중앙 관리
 * - ViewModel이 FirebaseAnalytics를 직접 참조하지 않도록 래핑
 * - Android 프레임워크 의존성을 이 계층에서 격리하여 ViewModel 테스트 용이성 확보
 *
 * @param analytics Firebase Analytics 인스턴스 (Hilt를 통해 주입)
 */
@Singleton
class TeumAnalyticsLogger @Inject constructor(
    private val analytics: FirebaseAnalytics
) {

    /**
     * 소셜 로그인 성공 이벤트 로깅 ([TeumAnalyticsEvent.LoginComplete])
     *
     * @param method 로그인 방식 — "kakao" 또는 "google"
     */
    fun logLoginComplete(method: String) {
        val params = Bundle().apply {
            putString(TeumAnalyticsEvent.LoginComplete.PARAM_METHOD, method)
        }
        analytics.logEvent(TeumAnalyticsEvent.LoginComplete.NAME, params)
    }

    /**
     * 소셜 로그인 실패 이벤트 로깅 ([TeumAnalyticsEvent.LoginFail])
     *
     * @param method         로그인 방식 — "kakao" 또는 "google"
     * @param errorCode      실패 원인 코드
     *   - 서버 오류: 서버가 내려준 code 값 그대로 (예: "AUTH-007")
     *   - 네트워크 오류: "NETWORK_ERROR"
     *   - 세션 만료: "SESSION_EXPIRED"
     *   - 알 수 없는 오류: "UNKNOWN_ERROR"
     * @param errorMessage   서버 메시지 또는 예외 메시지 — ServerError·UnknownError 시 전달
     *   Firebase Analytics 파라미터 값 최대 100자를 초과하면 잘라냅니다.
     * @param throwableClass UnknownError 전용 — 발생한 예외 클래스 단순명 (예: "UnknownHostException")
     */
    fun logLoginFail(
        method: String,
        errorCode: String,
        errorMessage: String? = null,
        throwableClass: String? = null,
    ) {
        val maxLen = TeumAnalyticsEvent.LoginFail.MAX_PARAM_LENGTH
        val params = Bundle().apply {
            putString(TeumAnalyticsEvent.LoginFail.PARAM_METHOD, method)
            putString(TeumAnalyticsEvent.LoginFail.PARAM_ERROR_CODE, errorCode)
            errorMessage?.take(maxLen)?.let {
                putString(TeumAnalyticsEvent.LoginFail.PARAM_ERROR_MESSAGE, it)
            }
            throwableClass?.take(maxLen)?.let {
                putString(TeumAnalyticsEvent.LoginFail.PARAM_THROWABLE_CLASS, it)
            }
        }
        analytics.logEvent(TeumAnalyticsEvent.LoginFail.NAME, params)
    }
}