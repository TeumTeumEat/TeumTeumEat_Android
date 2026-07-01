package com.teumteumeat.teumteumeat.utils.firebase

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.core.content.edit
import com.google.firebase.analytics.FirebaseAnalytics
import com.teumteumeat.teumteumeat.domain.model.common.GoalTypeUiState
import com.teumteumeat.teumteumeat.domain.model.goal.Difficulty
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Firebase Analytics 전용 로거
 *
 * - 이벤트 이름/파라미터 키는 [TeumAnalyticsEvent]에서 중앙 관리
 * - ViewModel이 FirebaseAnalytics를 직접 참조하지 않도록 래핑
 * - Android 프레임워크 의존성을 이 계층에서 격리하여 ViewModel 테스트 용이성 확보
 * - 생성 시점에 설치/업데이트 여부를 확인하여 최초 1회 이벤트 발송
 *   → 동일 버전 재시작 시에는 이벤트를 발송하지 않음
 *
 * @param analytics Firebase Analytics 인스턴스 (Hilt를 통해 주입)
 * @param context   앱 버전 정보 조회 및 발송 플래그 저장용 ApplicationContext
 */
@Singleton
class TeumAnalyticsLogger @Inject constructor(
    private val analytics: FirebaseAnalytics,
    @ApplicationContext private val context: Context,
) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    init {
        logAppInstallOrUpdateIfNeeded()
    }

    companion object {
        private const val TAG = "TeumAnalytics"
        private const val PREF_NAME = "analytics_prefs"

        /**
         * 마지막으로 [TeumAnalyticsEvent.AppInstallOrUpdate] 이벤트를 발송한 versionCode.
         * 저장된 값과 현재 versionCode가 다를 때만 이벤트를 발송합니다.
         * - 기본값 -1L: 한 번도 발송한 적 없음 (최초 설치)
         */
        private const val KEY_LAST_SENT_VERSION_CODE = "last_sent_version_code"

        /** PDF 업로드 시도 횟수 누적값 — 0부터 시작, [logPdfUploadStart] 호출 시마다 +1 */
        private const val KEY_PDF_UPLOAD_ATTEMPT_COUNT = "pdf_upload_attempt_count"
    }

    /**
     * 앱 설치 또는 업데이트 후 첫 실행 시 [TeumAnalyticsEvent.AppInstallOrUpdate] 이벤트를 1회 로깅합니다.
     *
     * ## 발송 조건
     * SharedPreferences에 저장된 마지막 발송 versionCode와 현재 versionCode가 다를 때만 발송합니다.
     *
     * | 시나리오              | 동작         |
     * |----------------------|--------------|
     * | 최초 설치 후 첫 실행  | 이벤트 발송  |
     * | 업데이트 후 첫 실행   | 이벤트 발송  |
     * | 동일 버전 재실행      | 발송 안 함   |
     * | 재설치·데이터 초기화  | 이벤트 발송  |
     *
     * - PackageManager 예외 발생 시 조용히 무시 (Analytics 실패가 앱 크래시 유발 방지)
     * - API 28+ 에서는 [android.content.pm.PackageInfo.getLongVersionCode] 사용
     */
    private fun logAppInstallOrUpdateIfNeeded() {
        try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)

            @Suppress("DEPRECATION")
            val currentVersionCode: Long = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageInfo.longVersionCode
            } else {
                packageInfo.versionCode.toLong()
            }

            val lastSentVersionCode = prefs.getLong(KEY_LAST_SENT_VERSION_CODE, -1L)
            if (currentVersionCode == lastSentVersionCode) return

            val params = Bundle().apply {
                putString(TeumAnalyticsEvent.AppInstallOrUpdate.PARAM_VERSION_CODE, currentVersionCode.toString())
                putString(TeumAnalyticsEvent.AppInstallOrUpdate.PARAM_VERSION_NAME, packageInfo.versionName)
            }
            analytics.logEvent(TeumAnalyticsEvent.AppInstallOrUpdate.NAME, params)

            prefs.edit { putLong(KEY_LAST_SENT_VERSION_CODE, currentVersionCode) }

        } catch (e: Exception) {
            Log.e(TAG, "❌ logAppInstallOrUpdateIfNeeded 예외 발생 — ${e::class.simpleName}: ${e.message}", e)
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
     * ONB-004 — 출퇴근 시간 설정 완료 이벤트 로깅 ([TeumAnalyticsEvent.CommuteTimeSet])
     *
     * 이벤트와 함께 User Property([TeumAnalyticsEvent.UserProperties.COMMUTE_TIME_FIRST],
     * [TeumAnalyticsEvent.UserProperties.COMMUTE_TIME_SECOND])도 등록합니다.
     *
     * @param firstTime  집에서 나오는 시간 — "HH:mm" 24시간 형식 (예: "08:00")
     * @param secondTime 집에 돌아가는 시간 — "HH:mm" 24시간 형식 (예: "18:00")
     */
    fun logCommuteTimeSet(firstTime: String, secondTime: String) {
        analytics.setUserProperty(TeumAnalyticsEvent.UserProperties.COMMUTE_TIME_FIRST, firstTime)
        analytics.setUserProperty(TeumAnalyticsEvent.UserProperties.COMMUTE_TIME_SECOND, secondTime)
        val params = Bundle().apply {
            putString(TeumAnalyticsEvent.CommuteTimeSet.PARAM_COMMUTE_TIME_FIRST, firstTime)
            putString(TeumAnalyticsEvent.CommuteTimeSet.PARAM_COMMUTE_TIME_SECOND, secondTime)
        }
        analytics.logEvent(TeumAnalyticsEvent.CommuteTimeSet.NAME, params)
    }

    /**
     * ONB-003 — 하루 퀴즈 수 설정 완료 이벤트 로깅 ([TeumAnalyticsEvent.QuizCountSet])
     *
     * 이벤트와 함께 User Property([TeumAnalyticsEvent.UserProperties.QUIZ_COUNT])도 등록합니다.
     * User Property는 세션이 종료되어도 유지되므로, 추후 세그먼트별 리텐션 분석에 활용됩니다.
     *
     * @param quizCount 선택한 하루 퀴즈 수 — 3 | 5 | 7 | 10
     */
    fun logQuizCountSet(quizCount: Int) {
        val quizCountStr = quizCount.toString()
        analytics.setUserProperty(TeumAnalyticsEvent.UserProperties.QUIZ_COUNT, quizCountStr)
        val params = Bundle().apply {
            putString(TeumAnalyticsEvent.QuizCountSet.PARAM_QUIZ_COUNT, quizCountStr)
        }
        analytics.logEvent(TeumAnalyticsEvent.QuizCountSet.NAME, params)
    }

    /**
     * ONB-007 — 카테고리 3뎁스 선택 완료 이벤트 로깅 ([TeumAnalyticsEvent.CategorySelect])
     *
     * 이벤트와 함께 User Property([TeumAnalyticsEvent.UserProperties.CATEGORY_DEPTH1/2/3])도 등록합니다.
     *
     * @param depth1 1뎁스 카테고리명 (예: "앱개발자")
     * @param depth2 2뎁스 카테고리명 (예: "React Native")
     * @param depth3 3뎁스 카테고리명 (예: "SwiftUI")
     */
    fun logCategorySelect(depth1: String, depth2: String, depth3: String) {
        analytics.setUserProperty(TeumAnalyticsEvent.UserProperties.CATEGORY_DEPTH1, depth1)
        analytics.setUserProperty(TeumAnalyticsEvent.UserProperties.CATEGORY_DEPTH2, depth2)
        analytics.setUserProperty(TeumAnalyticsEvent.UserProperties.CATEGORY_DEPTH3, depth3)
        val params = Bundle().apply {
            putString(TeumAnalyticsEvent.CategorySelect.PARAM_DEPTH1, depth1)
            putString(TeumAnalyticsEvent.CategorySelect.PARAM_DEPTH2, depth2)
            putString(TeumAnalyticsEvent.CategorySelect.PARAM_DEPTH3, depth3)
        }
        analytics.logEvent(TeumAnalyticsEvent.CategorySelect.NAME, params)
    }

    /**
     * ONB-006 — 학습 방식 선택 완료 이벤트 로깅 ([TeumAnalyticsEvent.LearningTypeSelect])
     *
     * 이벤트와 함께 User Property([TeumAnalyticsEvent.UserProperties.LEARNING_TYPE])도 등록합니다.
     *
     * @param goalType 선택한 학습 방식 — [GoalTypeUiState.CATEGORY] 또는 [GoalTypeUiState.DOCUMENT]
     */
    fun logLearningTypeSelect(goalType: GoalTypeUiState) {
        val value = when (goalType) {
            GoalTypeUiState.CATEGORY -> "category"
            GoalTypeUiState.DOCUMENT -> "pdf"
            GoalTypeUiState.NONE -> return
        }
        analytics.setUserProperty(TeumAnalyticsEvent.UserProperties.LEARNING_TYPE, value)
        val params = Bundle().apply {
            putString(TeumAnalyticsEvent.LearningTypeSelect.PARAM_LEARNING_TYPE, value)
        }
        analytics.logEvent(TeumAnalyticsEvent.LearningTypeSelect.NAME, params)
    }

    /**
     * ONB-005 — 디바이스 알림 권한 허용 후 다음 버튼 클릭 이벤트 로깅 ([TeumAnalyticsEvent.EnableNotifyPermission])
     *
     * User Property([TeumAnalyticsEvent.UserProperties.NOTIFY_ENABLED])도 함께 등록합니다.
     * 이 함수는 알림 권한을 허용한 경우에만 호출되므로 항상 "true"로 설정됩니다.
     */
    fun logEnableNotifyPermission() {
        analytics.setUserProperty(TeumAnalyticsEvent.UserProperties.NOTIFY_ENABLED, "true")
        analytics.logEvent(TeumAnalyticsEvent.EnableNotifyPermission.NAME, null)
    }

    /**
     * ONB-010 — 난이도 선택 완료 이벤트 로깅 ([TeumAnalyticsEvent.DifficultySelect])
     *
     * 이벤트와 함께 User Property([TeumAnalyticsEvent.UserProperties.DIFFICULTY])도 등록합니다.
     *
     * @param difficulty 선택한 난이도 — [Difficulty.HARD] | [Difficulty.MEDIUM] | [Difficulty.EASY]
     *   [Difficulty.NONE]이 전달되면 조기 반환합니다.
     */
    fun logDifficultySelect(difficulty: Difficulty) {
        val value = when (difficulty) {
            Difficulty.HARD -> "high"
            Difficulty.MEDIUM -> "mid"
            Difficulty.EASY -> "low"
            Difficulty.NONE -> return
        }
        analytics.setUserProperty(TeumAnalyticsEvent.UserProperties.DIFFICULTY, value)
        val params = Bundle().apply {
            putString(TeumAnalyticsEvent.DifficultySelect.PARAM_DIFFICULTY, value)
        }
        analytics.logEvent(TeumAnalyticsEvent.DifficultySelect.NAME, params)
    }

    /**
     * ONB-011 — 온보딩 최종 완료 이벤트 로깅 ([TeumAnalyticsEvent.OnboardingComplete])
     *
     * 파라미터 없음. User Property([TeumAnalyticsEvent.UserProperties.ONBOARDING_COMPLETE])를
     * "complete"로 설정합니다.
     * [OnBoardingViewModel.submitOnBoarding] 내 모든 API 성공 후 호출합니다.
     */
    fun logOnboardingComplete() {
        analytics.setUserProperty(TeumAnalyticsEvent.UserProperties.ONBOARDING_COMPLETE, "complete")
        analytics.logEvent(TeumAnalyticsEvent.OnboardingComplete.NAME, null)
    }

    /**
     * ONB-PDF-1 — PDF 업로드 시작 이벤트 로깅 ([TeumAnalyticsEvent.PdfUploadStart])
     *
     * 호출 시마다 SharedPreferences에 저장된 시도 횟수를 1 증가시키고,
     * 증가된 값을 User Property([TeumAnalyticsEvent.UserProperties.PDF_UPLOAD_ATTEMPT_COUNT])로 등록합니다.
     *
     * @param fileSizeKb  업로드할 파일 크기 (KB 단위)
     * @param pageCount   PDF 페이지 수
     */
    fun logPdfUploadStart(fileSizeKb: Long, pageCount: Int) {
        val newCount = prefs.getInt(KEY_PDF_UPLOAD_ATTEMPT_COUNT, 0) + 1
        prefs.edit { putInt(KEY_PDF_UPLOAD_ATTEMPT_COUNT, newCount) }

        analytics.setUserProperty(
            TeumAnalyticsEvent.UserProperties.PDF_UPLOAD_ATTEMPT_COUNT,
            newCount.toString()
        )

        val params = Bundle().apply {
            putLong(TeumAnalyticsEvent.PdfUploadStart.PARAM_FILE_SIZE_KB, fileSizeKb)
            putInt(TeumAnalyticsEvent.PdfUploadStart.PARAM_PAGE_COUNT, pageCount)
        }
        analytics.logEvent(TeumAnalyticsEvent.PdfUploadStart.NAME, params)
    }

    /**
     * SUM-001 — 요약본 첫 페이지 노출 이벤트 로깅 ([TeumAnalyticsEvent.SummaryViewStart])
     *
     * 이벤트와 함께 User Property([TeumAnalyticsEvent.UserProperties.HAS_VIEWED_SUMMARY])를
     * "true"로 설정합니다. [SummaryViewModel] 내에서 ViewModel 인스턴스당 최초 1회만 호출됩니다.
     *
     * @param sessionId 현재 학습 목표 ID (goalId.toString())
     * @param contentId 노출된 요약 콘텐츠 ID (categoryDocumentId 또는 documentId)
     * @param topic     요약 콘텐츠 제목 — 100자 초과 시 잘라냄
     */
    fun logSummaryViewStart(sessionId: String, contentId: String, topic: String) {
        analytics.setUserProperty(TeumAnalyticsEvent.UserProperties.HAS_VIEWED_SUMMARY, "true")
        val params = Bundle().apply {
            putString(TeumAnalyticsEvent.SummaryViewStart.PARAM_SESSION_ID, sessionId)
            putString(TeumAnalyticsEvent.SummaryViewStart.PARAM_CONTENT_ID, contentId)
            putString(TeumAnalyticsEvent.SummaryViewStart.PARAM_TOPIC, topic.take(100))
        }
        analytics.logEvent(TeumAnalyticsEvent.SummaryViewStart.NAME, params)
    }

    /**
     * SUM-002 — 요약본 완독 이벤트 로깅 ([TeumAnalyticsEvent.SummaryViewComplete])
     *
     * [SummaryViewModel] 내에서 ViewModel 인스턴스당 최초 1회만 호출됩니다.
     * 발화 조건: API 완전 수신 완료(`UiScreenState.Success`) AND 스크롤 최하단 도달.
     *
     * @param sessionId 현재 학습 목표 ID (goalId.toString())
     * @param contentId 요약 콘텐츠 ID — summary_view_start 와 JOIN 키로 사용
     * @param topic     요약 콘텐츠 제목 — 100자 초과 시 잘라냄
     */
    fun logSummaryViewComplete(sessionId: String, contentId: String, topic: String) {
        val params = Bundle().apply {
            putString(TeumAnalyticsEvent.SummaryViewComplete.PARAM_SESSION_ID, sessionId)
            putString(TeumAnalyticsEvent.SummaryViewComplete.PARAM_CONTENT_ID, contentId)
            putString(TeumAnalyticsEvent.SummaryViewComplete.PARAM_TOPIC, topic.take(100))
        }
        analytics.logEvent(TeumAnalyticsEvent.SummaryViewComplete.NAME, params)
    }

    /**
     * 소셜 로그인 방식을 User Property로 등록합니다 ([TeumAnalyticsEvent.UserProperties.LOGIN_METHOD]).
     *
     * 수동 로그인과 자동 로그인 모두 성공 시 호출됩니다.
     *
     * @param method 로그인 방식 — "kakao" 또는 "google"
     */
    fun setLoginMethod(method: String) {
        analytics.setUserProperty(TeumAnalyticsEvent.UserProperties.LOGIN_METHOD, method)
    }

    /**
     * OS 종류를 User Property로 등록합니다 ([TeumAnalyticsEvent.UserProperties.OS_TYPE]).
     *
     * 수동 로그인과 자동 로그인 모두 성공 시 호출됩니다.
     */
    fun setOsType() {
        analytics.setUserProperty(TeumAnalyticsEvent.UserProperties.OS_TYPE, "Android")
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