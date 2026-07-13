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
        val value = difficulty.toAnalyticsValue() ?: return
        analytics.setUserProperty(TeumAnalyticsEvent.UserProperties.DIFFICULTY, value)
        val params = Bundle().apply {
            putString(TeumAnalyticsEvent.DifficultySelect.PARAM_DIFFICULTY, value)
        }
        analytics.logEvent(TeumAnalyticsEvent.DifficultySelect.NAME, params)
    }

    /** [Difficulty] → Firebase Analytics 파라미터 값 매핑 ("high" | "mid" | "low") */
    private fun Difficulty.toAnalyticsValue(): String? = when (this) {
        Difficulty.HARD -> "high"
        Difficulty.MEDIUM -> "mid"
        Difficulty.EASY -> "low"
        Difficulty.NONE -> null
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
     * QUIZ-001 — 퀴즈 화면 진입 이벤트 로깅 ([TeumAnalyticsEvent.QuizStart])
     *
     * [QuizViewModel] 내에서 ViewModel 인스턴스당 최초 1회만 호출됩니다.
     * `difficulty`가 [Difficulty.NONE]이면 조기 반환하여 이벤트를 발송하지 않습니다.
     *
     * @param contentId 퀴즈 콘텐츠 ID (documentId.toString()) — summary 이벤트와 JOIN 키
     * @param topic     콘텐츠 제목 — 100자 초과 시 잘라냄
     * @param quizCount 총 문제 수
     * @param difficulty 사용자 난이도 설정 — [Difficulty.HARD] | [Difficulty.MEDIUM] | [Difficulty.EASY]
     * @param entryType 진입 유형 — "first" | "resume" | "retry"
     */
    fun logQuizStart(
        contentId: String,
        topic: String,
        quizCount: Long,
        difficulty: Difficulty,
        entryType: String,
    ) {
        val difficultyValue = difficulty.toAnalyticsValue() ?: return
        val params = Bundle().apply {
            putString(TeumAnalyticsEvent.QuizStart.PARAM_CONTENT_ID, contentId)
            putString(TeumAnalyticsEvent.QuizStart.PARAM_TOPIC, topic.take(100))
            putLong(TeumAnalyticsEvent.QuizStart.PARAM_QUIZ_COUNT, quizCount)
            putString(TeumAnalyticsEvent.QuizStart.PARAM_DIFFICULTY, difficultyValue)
            putString(TeumAnalyticsEvent.QuizStart.PARAM_ENTRY_TYPE, entryType)
        }
        analytics.logEvent(TeumAnalyticsEvent.QuizStart.NAME, params)
    }

    /**
     * QUIZ-002 — 개별 문항 제출 이벤트 로깅 ([TeumAnalyticsEvent.QuizAnswerSubmit])
     *
     * 이벤트와 함께 User Property([TeumAnalyticsEvent.UserProperties.TOTAL_QUESTIONS_ANSWERED])를
     * questionNo로 갱신합니다. [QuizViewModel.submitAnswer]에서 서버 응답 수신 시마다 호출됩니다.
     *
     * @param contentId  퀴즈 콘텐츠 ID (documentId.toString())
     * @param questionNo 전역 누적 문항 응답 수 (재도전해도 초기화되지 않음)
     * @param answerType 문항 유형 — "ox" | "mcq"
     * @param isCorrect  정답 여부
     */
    fun logQuizAnswerSubmit(
        contentId: String,
        questionNo: Long,
        answerType: String,
        isCorrect: Boolean,
    ) {
        analytics.setUserProperty(
            TeumAnalyticsEvent.UserProperties.TOTAL_QUESTIONS_ANSWERED,
            questionNo.toString()
        )
        val params = Bundle().apply {
            putString(TeumAnalyticsEvent.QuizAnswerSubmit.PARAM_CONTENT_ID, contentId)
            putLong(TeumAnalyticsEvent.QuizAnswerSubmit.PARAM_QUESTION_NO, questionNo)
            putString(TeumAnalyticsEvent.QuizAnswerSubmit.PARAM_ANSWER_TYPE, answerType)
            putString(TeumAnalyticsEvent.QuizAnswerSubmit.PARAM_IS_CORRECT, isCorrect.toString())
        }
        analytics.logEvent(TeumAnalyticsEvent.QuizAnswerSubmit.NAME, params)
    }

    /**
     * QUIZ-003 — 퀴즈 풀이 중 이탈 이벤트 로깅 ([TeumAnalyticsEvent.QuizAbandoned])
     *
     * [QuizViewModel.onCleared]에서 세션 내 제출 수가 전체 문항 수보다 적을 때만 호출됩니다.
     *
     * @param contentId      퀴즈 콘텐츠 ID (documentId.toString())
     * @param lastQuestionNo 세션 내 마지막 제출 문항 번호 (0 = 한 문항도 안 풀고 이탈)
     * @param quizCount      이 세션의 전체 문항 수
     * @param entryType      진입 유형 — "first" | "resume" | "retry"
     */
    fun logQuizAbandoned(
        contentId: String,
        lastQuestionNo: Long,
        quizCount: Long,
        entryType: String,
    ) {
        val params = Bundle().apply {
            putString(TeumAnalyticsEvent.QuizAbandoned.PARAM_CONTENT_ID, contentId)
            putLong(TeumAnalyticsEvent.QuizAbandoned.PARAM_LAST_QUESTION_NO, lastQuestionNo)
            putLong(TeumAnalyticsEvent.QuizAbandoned.PARAM_QUIZ_COUNT, quizCount)
            putString(TeumAnalyticsEvent.QuizAbandoned.PARAM_ENTRY_TYPE, entryType)
        }
        analytics.logEvent(TeumAnalyticsEvent.QuizAbandoned.NAME, params)
    }

    /**
     * QUIZ-004 — 퀴즈 세트 완료 이벤트 로깅 ([TeumAnalyticsEvent.QuizComplete])
     *
     * [QuizViewModel.completeCurrentQuizSet]에서 complete-set API 성공 시,
     * [com.teumteumeat.teumteumeat.data.datastore.QuizTrackingDataStore.markQuizCompleted]
     * 호출 직후 발송됩니다.
     * `difficulty`가 [Difficulty.NONE]이면 조기 반환하여 이벤트를 발송하지 않습니다
     * (quiz_start와 동일한 방어 — JOIN 키 일관성 유지).
     *
     * @param contentId    퀴즈 콘텐츠 ID (documentId.toString()) — quiz_start 와 JOIN 키
     * @param topic        콘텐츠 제목 — 100자 초과 시 잘라냄
     * @param difficulty   사용자 난이도 설정 — [Difficulty.HARD] | [Difficulty.MEDIUM] | [Difficulty.EASY]
     * @param entryType    진입 유형 — "first" | "resume" | "retry"
     * @param quizCount    전체 문항 수
     * @param correctCount 정답 문항 수
     * @param scoreRate    정답률(%) 문자열 — 호출부에서 0으로 나누기 방어 후 전달 (예: "60.0")
     */
    fun logQuizComplete(
        contentId: String,
        topic: String,
        difficulty: Difficulty,
        entryType: String,
        quizCount: Long,
        correctCount: Long,
        scoreRate: String,
    ) {
        val difficultyValue = difficulty.toAnalyticsValue() ?: return
        val params = Bundle().apply {
            putString(TeumAnalyticsEvent.QuizComplete.PARAM_CONTENT_ID, contentId)
            putString(TeumAnalyticsEvent.QuizComplete.PARAM_TOPIC, topic.take(100))
            putString(TeumAnalyticsEvent.QuizComplete.PARAM_DIFFICULTY, difficultyValue)
            putString(TeumAnalyticsEvent.QuizComplete.PARAM_ENTRY_TYPE, entryType)
            putLong(TeumAnalyticsEvent.QuizComplete.PARAM_QUIZ_COUNT, quizCount)
            putLong(TeumAnalyticsEvent.QuizComplete.PARAM_CORRECT_COUNT, correctCount)
            putString(TeumAnalyticsEvent.QuizComplete.PARAM_SCORE_RATE, scoreRate)
        }
        analytics.logEvent(TeumAnalyticsEvent.QuizComplete.NAME, params)
    }

    /**
     * STAMP-001 — 스탬프 적립 이벤트를 로깅합니다.
     * 오늘 하루 중 첫 퀴즈 완료로 hasSolvedToday가 false → true로 바뀐 경우에만 호출된다.
     */
    fun logStampEarned(
        contentId: String,
        streakCount: Long,
        totalStamps: Long,
        monthlyStamps: Long,
    ) {
        val params = Bundle().apply {
            putString(TeumAnalyticsEvent.StampEarned.PARAM_CONTENT_ID, contentId)
            putLong(TeumAnalyticsEvent.StampEarned.PARAM_STREAK_COUNT, streakCount)
            putLong(TeumAnalyticsEvent.StampEarned.PARAM_TOTAL_STAMPS, totalStamps)
            putLong(TeumAnalyticsEvent.StampEarned.PARAM_MONTHLY_STAMPS, monthlyStamps)
        }
        analytics.logEvent(TeumAnalyticsEvent.StampEarned.NAME, params)
    }

    /**
     * QUIZ-005 — 퀴즈 결과 화면 "글보기" 버튼 탭 이벤트를 로깅합니다.
     *
     * @param contentId 퀴즈 콘텐츠 ID (documentId.toString()) — quiz_complete 와 JOIN 키
     * @param topic     콘텐츠 제목 — 100자 초과 시 잘라냄
     * @param entryType 진입 유형 — quiz_start에서 계산된 값을 그대로 전달 ("first" | "resume" | "retry")
     */
    fun logReviewConceptTap(contentId: String, topic: String, entryType: String) {
        val params = Bundle().apply {
            putString(TeumAnalyticsEvent.ReviewConceptTap.PARAM_CONTENT_ID, contentId)
            putString(TeumAnalyticsEvent.ReviewConceptTap.PARAM_TOPIC, topic.take(100))
            putString(TeumAnalyticsEvent.ReviewConceptTap.PARAM_ENTRY_TYPE, entryType)
        }
        analytics.logEvent(TeumAnalyticsEvent.ReviewConceptTap.NAME, params)
    }

    /**
     * GOAL-001 — 목표 완주 이벤트를 로깅합니다.
     *
     * 이벤트와 함께 User Property([TeumAnalyticsEvent.UserProperties.GOAL_WEEKS])도 등록합니다.
     * `difficulty`/`learning_type`/`quiz_count` User Property는 온보딩 시점에 이미 등록되어
     * 있으므로 이 함수에서 재등록하지 않습니다.
     *
     * @param goalId          완주한 목표 식별 ID
     * @param categoryId      CATEGORY 목표: categoryId / DOCUMENT 목표: 파일명
     * @param learningType    학습 방식 — "category" | "pdf"
     * @param goalWeeks       목표 기간(주) — startDate~endDate 근사 계산값
     * @param totalStamps     완주 시점까지 획득한 전체 누적 스탬프
     * @param isFirstComplete 첫 완주 여부 — "true" | "false"
     */
    fun logCourseComplete(
        goalId: String,
        categoryId: String,
        learningType: String,
        goalWeeks: Long,
        totalStamps: Long,
        isFirstComplete: String,
    ) {
        val params = Bundle().apply {
            putString(TeumAnalyticsEvent.CourseComplete.PARAM_GOAL_ID, goalId)
            putString(TeumAnalyticsEvent.CourseComplete.PARAM_CATEGORY_ID, categoryId)
            putString(TeumAnalyticsEvent.CourseComplete.PARAM_LEARNING_TYPE, learningType)
            putLong(TeumAnalyticsEvent.CourseComplete.PARAM_GOAL_WEEKS, goalWeeks)
            putLong(TeumAnalyticsEvent.CourseComplete.PARAM_TOTAL_STAMPS, totalStamps)
            putString(TeumAnalyticsEvent.CourseComplete.PARAM_IS_FIRST_COMPLETE, isFirstComplete)
        }
        analytics.logEvent(TeumAnalyticsEvent.CourseComplete.NAME, params)

        analytics.setUserProperty(TeumAnalyticsEvent.UserProperties.GOAL_WEEKS, goalWeeks.toString())
    }

    /**
     * GOAL-002 — 완주 후 재학습 시작 이벤트를 로깅합니다.
     *
     * @param prevGoalId       직전에 완주한 목표 식별 ID
     * @param prevCategoryId   직전 완주 목표의 category_id (course_complete와 동일 기준)
     * @param prevLearningType 직전 완주 목표 학습 방식 — "category" | "pdf"
     * @param nextLearningType 새로 선택한 목표 학습 방식 — "category" | "pdf"
     * @param isFirstComplete  직전 완주가 첫 완주였는지 — "true" | "false"
     */
    fun logNextCourseStart(
        prevGoalId: String,
        prevCategoryId: String,
        prevLearningType: String,
        nextLearningType: String,
        isFirstComplete: String,
    ) {
        val params = Bundle().apply {
            putString(TeumAnalyticsEvent.NextCourseStart.PARAM_PREV_GOAL_ID, prevGoalId)
            putString(TeumAnalyticsEvent.NextCourseStart.PARAM_PREV_CATEGORY_ID, prevCategoryId)
            putString(TeumAnalyticsEvent.NextCourseStart.PARAM_PREV_LEARNING_TYPE, prevLearningType)
            putString(TeumAnalyticsEvent.NextCourseStart.PARAM_NEXT_LEARNING_TYPE, nextLearningType)
            putString(TeumAnalyticsEvent.NextCourseStart.PARAM_IS_FIRST_COMPLETE, isFirstComplete)
        }
        analytics.logEvent(TeumAnalyticsEvent.NextCourseStart.NAME, params)
    }

    /**
     * LIB-001 — 히스토리 탭(캘린더) 진입 이벤트를 로깅합니다.
     * 최초 1회로 제한하지 않고 탭에 진입할 때마다 매번 호출됩니다.
     * 첫 진입 시에는 캘린더 데이터 로드 완료 후 호출되어 스탬프 파라미터에 정확한 값을 싣습니다.
     *
     * @param month           진입 시 표시 중인 캘린더 월 — "yyyy-MM" (예: "2025-05")
     * @param date            진입 시점의 날짜 — "yyyy-MM-dd" (예: "2025-05-01")
     * @param monthStampCount 표시 월에 획득한 스탬프 수 — 로드 실패 시 -1
     * @param hasMonthStamp   표시 월 스탬프 보유 여부 — "true" | "false" | "unknown"(로드 실패)
     * @param totalStamps     누적 스탬프 수 — 로드 실패 시 -1
     */
    fun logCalendarView(
        month: String,
        date: String,
        monthStampCount: Long,
        hasMonthStamp: String,
        totalStamps: Long,
    ) {
        val params = Bundle().apply {
            putString(TeumAnalyticsEvent.CalendarView.PARAM_MONTH, month)
            putString(TeumAnalyticsEvent.CalendarView.PARAM_DATE, date)
            putLong(TeumAnalyticsEvent.CalendarView.PARAM_MONTH_STAMP_COUNT, monthStampCount)
            putString(TeumAnalyticsEvent.CalendarView.PARAM_HAS_MONTH_STAMP, hasMonthStamp)
            putLong(TeumAnalyticsEvent.CalendarView.PARAM_TOTAL_STAMPS, totalStamps)
        }
        analytics.logEvent(TeumAnalyticsEvent.CalendarView.NAME, params)
    }

    /**
     * LIB-002 — 캘린더 날짜 탭 이벤트를 로깅합니다.
     * 발화 횟수를 제한하지 않고 날짜 셀을 탭할 때마다 매번 호출됩니다.
     *
     * @param date     탭한 날짜 — "yyyy-MM-dd" (예: "2025-05-19")
     * @param hasStamp 탭한 날짜의 스탬프 존재 여부 — "true" | "false"
     */
    fun logCalendarDateTap(date: String, hasStamp: String) {
        val params = Bundle().apply {
            putString(TeumAnalyticsEvent.CalendarDateTap.PARAM_DATE, date)
            putString(TeumAnalyticsEvent.CalendarDateTap.PARAM_HAS_STAMP, hasStamp)
        }
        analytics.logEvent(TeumAnalyticsEvent.CalendarDateTap.NAME, params)
    }

    /**
     * LIB-003 — 일별 학습 기록 카드 탭 이벤트를 로깅합니다.
     * 발화 횟수를 제한하지 않고 카드를 탭할 때마다 매번 호출됩니다.
     *
     * @param date  조회한 캘린더 선택 날짜 — "yyyy-MM-dd" (예: "2025-05-19")
     * @param topic 주제명 or PDF 파일명 (예: "관세사 관세법")
     */
    fun logLearningRecordByDateTap(date: String, topic: String) {
        val params = Bundle().apply {
            putString(TeumAnalyticsEvent.LearningRecordByDateTap.PARAM_DATE, date)
            putString(TeumAnalyticsEvent.LearningRecordByDateTap.PARAM_TOPIC, topic)
        }
        analytics.logEvent(TeumAnalyticsEvent.LearningRecordByDateTap.NAME, params)
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