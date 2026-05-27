package com.teumteumeat.teumteumeat.utils.firebase

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import com.google.firebase.analytics.FirebaseAnalytics
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Firebase Analytics 전용 로거
 *
 * - 이벤트 이름/파라미터 키는 [TeumAnalyticsEvent]에서 중앙 관리
 * - ViewModel이 FirebaseAnalytics를 직접 참조하지 않도록 래핑
 * - Android 프레임워크 의존성을 이 계층에서 격리하여 ViewModel 테스트 용이성 확보
 * - 생성 시점에 앱 버전 정보를 User Property로 자동 등록
 *   → GA4 Audience / Exploration에서 버전코드 기준 필터링 가능
 *
 * @param analytics Firebase Analytics 인스턴스 (Hilt를 통해 주입)
 * @param context   앱 버전 정보 조회용 ApplicationContext
 */
@Singleton
class TeumAnalyticsLogger @Inject constructor(
    private val analytics: FirebaseAnalytics,
    @ApplicationContext private val context: Context,
) {

    init {
        setAppVersionProperties()
    }

    companion object {
        private const val TAG = "TeumAnalytics"
    }

    /**
     * 앱 버전 정보를 Firebase Analytics User Property로 등록합니다.
     *
     * | User Property       | 예시    | 목적                            |
     * |---------------------|---------|----------------------------------|
     * | app_version_code    | "17"    | 버전코드 기준 Audience 필터링   |
     * | app_version_name    | "1.0.17"| 릴리즈 식별 (사람이 읽기 쉬운 값)|
     *
     * - [TeumAnalyticsEvent.UserProperty] 상수로 키 관리
     * - PackageManager 예외 발생 시 조용히 무시 (Analytics 실패가 앱 크래시 유발 방지)
     * - API 28+ 에서는 [android.content.pm.PackageInfo.getLongVersionCode] 사용
     */
    private fun setAppVersionProperties() {
        try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)

            @Suppress("DEPRECATION")
            val versionCode: Long = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageInfo.longVersionCode
            } else {
                packageInfo.versionCode.toLong()
            }

            analytics.setUserProperty(
                TeumAnalyticsEvent.UserProperty.APP_VERSION_CODE,
                versionCode.toString(),
            )
            analytics.setUserProperty(
                TeumAnalyticsEvent.UserProperty.APP_VERSION_NAME,
                packageInfo.versionName,
            )

        } catch (e: Exception) {
            Log.e(TAG, "❌ setAppVersionProperties 예외 발생 — ${e::class.simpleName}: ${e.message}", e)
        }
    }

    /**
     * ONB-001 — 약관 전체 동의 완료 이벤트 로깅 ([TeumAnalyticsEvent.TermsAgreeComplete])
     *
     * 파라미터 없음. [LoginViewModel.agreeTermsAndRegister] 호출 직전에 전송합니다.
     */
    fun logTermsAgreeComplete() {
        analytics.logEvent(TeumAnalyticsEvent.TermsAgreeComplete.NAME, null)
    }

    /**
     * ONB-002 — 온보딩 첫 화면 진입 이벤트 로깅 ([TeumAnalyticsEvent.OnboardingStart])
     *
     * 파라미터 없음. [OnBoardingViewModel] init 중 Process Death 복원이 아닌 경우에만 전송합니다.
     */
    fun logOnboardingStart() {
        analytics.logEvent(TeumAnalyticsEvent.OnboardingStart.NAME, null)
    }

    /**
     * 소셜 로그인 성공 이벤트 로깅 ([TeumAnalyticsEvent.LoginComplete])
     *
     * @param method       로그인 방식 — "kakao" 또는 "google"
     * @param isFirstLogin 앱 설치 후 첫 번째 로그인 여부
     *   - `true`  : 최초 로그인 (재설치·데이터 삭제 후 포함)
     *   - `false` : 재로그인
     *   Firebase Analytics는 Boolean 타입을 지원하지 않으므로 "true"/"false" 문자열로 저장합니다.
     */
    fun logLoginComplete(method: String, isFirstLogin: Boolean) {
        val params = Bundle().apply {
            putString(TeumAnalyticsEvent.LoginComplete.PARAM_METHOD, method)
            putString(
                TeumAnalyticsEvent.LoginComplete.PARAM_IS_FIRST_LOGIN,
                isFirstLogin.toString()
            )
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