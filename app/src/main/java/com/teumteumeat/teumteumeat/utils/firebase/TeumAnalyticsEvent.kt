package com.teumteumeat.teumteumeat.utils.firebase

/**
 * 틈틈잇 Firebase Analytics 이벤트 상수
 *
 * 이벤트 이름과 파라미터 키를 한 곳에서 관리하여
 * 오타·불일치를 컴파일 타임에 방지합니다.
 *
 * 규칙:
 * - 이벤트 이름: snake_case, 최대 40자
 * - 파라미터 키: snake_case, 최대 40자
 * - 파라미터 값: 최대 100자
 */
object TeumAnalyticsEvent {

    /**
     * 소셜 로그인 성공
     *
     * | 파라미터         | 타입   | 예시   | 목적                         |
     * |----------------|--------|--------|------------------------------|
     * | method         | String | kakao  | 로그인 방식별 전환율          |
     * | is_first_login | String | true   | 앱 설치 후 첫 번째 로그인 여부 |
     *
     * - `is_first_login`은 "true" | "false" 문자열로 저장 (Firebase Analytics Boolean 미지원)
     * - 재설치·데이터 삭제 시 플래그 초기화 → "true" 재발생
     */
    object LoginComplete {
        const val NAME = "login_complete"
        const val PARAM_METHOD = "method"           // "kakao" | "google"
        const val PARAM_IS_FIRST_LOGIN = "is_first_login" // "true" | "false"
    }

    /**
     * 소셜 로그인 실패
     *
     * | 파라미터         | 타입   | 예시                            | 목적                         |
     * |-----------------|--------|---------------------------------|------------------------------|
     * | method          | String | kakao                           | 실패 방식 구분               |
     * | error_code      | String | AUTH-007                        | 실패 원인 분류               |
     * | error_message   | String | 존재하지 않는 유저입니다.        | 서버/예외 메시지 상세 확인   |
     * | throwable_class | String | UnknownHostException            | UnknownError 예외 클래스 추적|
     *
     * - `error_message`, `throwable_class`는 ServerError·UnknownError 시에만 기록
     * - Firebase Analytics 파라미터 값 최대 100자 제한 → 초과 시 잘라냄
     */
    object LoginFail {
        const val NAME = "login_fail"
        const val PARAM_METHOD = "method"               // "kakao" | "google"
        const val PARAM_ERROR_CODE =
            "error_code"       // 서버 코드 or "NETWORK_ERROR" / "UNKNOWN_ERROR"
        const val PARAM_ERROR_MESSAGE = "error_message" // 서버 메시지 or 예외 메시지 (max 100자)
        const val PARAM_THROWABLE_CLASS = "throwable_class" // UnknownError 전용: 예외 클래스명

        /** Firebase Analytics 파라미터 값 최대 길이 */
        const val MAX_PARAM_LENGTH = 100
    }

    /**
     * ONB-001 · 온보딩 1단계 — 약관 전체 동의 완료
     *
     * 파라미터 없음 — 동의 자체가 이벤트
     *
     * ## 측정 목적
     * - 약관 동의 전환율: `terms_agree_complete / login_complete (is_first_login=true)`
     *
     * ## 발생 시점
     * - [LoginViewModel.agreeTermsAndRegister] 호출 직전
     *   (BottomSheet 확인 버튼 → allRequiredAgreed 보장됨)
     */
    object TermsAgreeComplete {
        const val NAME = "terms_agree_complete"
    }

    /**
     * ONB-002 · 온보딩 2단계 — 온보딩 첫 화면 진입
     *
     * 파라미터 없음 — 진입 자체가 이벤트
     *
     * ## 측정 목적
     * - 온보딩 진입률: `onboarding_start / terms_agree_complete`
     *
     * ## 발생 시점
     * - [OnBoardingViewModel] init — Process Death 복원이 아닌 최초 생성 시에만 전송
     *   (복원 여부: SavedStateHandle에 KEY_SCREEN 존재 여부로 판별)
     */
    object OnboardingStart {
        const val NAME = "onboarding_start"
    }

    /**
     * ONB-003 · 온보딩 3단계 — 하루 퀴즈 수 설정 완료
     *
     * | 파라미터   | 타입   | 예시 | 목적                        |
     * |-----------|--------|------|----------------------------|
     * | quiz_count | String | "5"  | 선택한 하루 퀴즈 수         |
     *
     * ## 측정 목적
     * - 전체 사용자의 선호 퀴즈 수 분포도 파악
     * - User Property로도 등록하여 세그먼트별 리텐션 분석에 활용
     *
     * ## 발생 시점
     * - [OnBoardingViewModel.onSetRoutineCompleted] 호출 시
     *   (SetRoutineScreen "다음으로" 버튼 → 퀴즈 수 + 알림 시간 모두 설정 완료 후)
     */
    object QuizCountSet {
        const val NAME = "quiz_count_set"
        const val PARAM_QUIZ_COUNT = "quiz_count"  // "3" | "5" | "7" | "10"
    }

    /**
     * ONB-004 · 온보딩 4단계 — 출퇴근 시간 설정 완료
     *
     * | 파라미터              | 타입   | 예시    | 목적                          |
     * |--------------------|--------|---------|-------------------------------|
     * | commute_time_first  | String | "08:00" | 집에서 나오는 시간 (출근)      |
     * | commute_time_second | String | "18:00" | 집에 돌아가는 시간 (퇴근)      |
     *
     * ## 측정 목적
     * - 이용자 출퇴근 시간 분포도 파악 (GA Explore → User Property 세그먼트 분석)
     *
     * ## 발생 시점
     * - [OnBoardingViewModel.onSetRoutineCompleted] 호출 시
     *   (SetRoutineScreen "다음으로" 버튼 → 두 알림 시간 모두 설정 완료 후)
     *
     * ## 포맷
     * - "HH:mm" 24시간 형식 (toServerTime() 에서 초(:ss) 제거)
     */
    object CommuteTimeSet {
        const val NAME = "commute_time_set"
        const val PARAM_COMMUTE_TIME_FIRST = "commute_time_first"    // "08:00"
        const val PARAM_COMMUTE_TIME_SECOND = "commute_time_second"  // "18:00"
    }

    /**
     * ONB-005 · 온보딩 5단계 — 디바이스 알림 권한 허용 후 다음 버튼 클릭
     *
     * 파라미터 없음 — 권한 허용 + 진행 자체가 이벤트
     *
     * ## 측정 목적
     * - 알림 권한 허용 후 온보딩을 계속 진행하는 사용자 비율 파악
     *
     * ## 발생 시점
     * - [OnBoardingViewModel.onSetRoutineCompleted] 호출 시
     *   isNotificationGranted == true 인 경우에만 전송
     */
    object EnableNotifyPermission {
        const val NAME = "enable_notify_permission"
    }

    /**
     * ONB-006 · 온보딩 6단계 — 학습 방식 선택 완료
     *
     * | 파라미터      | 타입   | 예시       | 목적                        |
     * |-------------|--------|------------|----------------------------|
     * | learning_type | String | "category" | 선택한 학습 방식             |
     *
     * ## 측정 목적
     * - 카테고리 vs PDF 학습 방식 선호도 분포 파악
     * - User Property로도 등록하여 세그먼트별 리텐션 분석에 활용
     *
     * ## 발생 시점
     * - [OnBoardingViewModel.onLearningMethodNextClicked] 호출 시
     *   (SelectLearningMethodScreen "다음으로" 버튼, NONE 제외)
     */
    object LearningTypeSelect {
        const val NAME = "learning_type_select"
        const val PARAM_LEARNING_TYPE = "learning_type"  // "category" | "pdf"
    }

    /**
     * ONB-007 · 온보딩 카테고리 1단계 — 카테고리 3뎁스 선택 완료 후 다음 버튼 클릭
     *
     * | 파라미터 | 타입   | 예시        | 목적                      |
     * |--------|--------|-------------|--------------------------|
     * | depth1 | String | "앱개발자"   | 1뎁스 카테고리명           |
     * | depth2 | String | "React Native" | 2뎁스 카테고리명        |
     * | depth3 | String | "SwiftUI"   | 3뎁스 카테고리명           |
     *
     * ## 측정 목적
     * - 인기 카테고리 분포 파악 및 콘텐츠 수요 데이터 확보
     * - User Property로도 등록하여 카테고리별 리텐션 세그먼트 분석에 활용
     *
     * ## 발생 시점
     * - [OnBoardingViewModel.onCategoryNextClicked] 호출 시
     *   (CategorySelectScreen "다음으로" 버튼, depth1·depth2·depth3 모두 선택 완료 후)
     */
    object CategorySelect {
        const val NAME = "category_select"
        const val PARAM_DEPTH1 = "depth1"  // 1뎁스 카테고리명
        const val PARAM_DEPTH2 = "depth2"  // 2뎁스 카테고리명
        const val PARAM_DEPTH3 = "depth3"  // 3뎁스 카테고리명
    }

    /**
     * ONB-010 · 온보딩 7단계 — 난이도 선택 완료
     *
     * | 파라미터   | 타입   | 예시   | 목적                        |
     * |-----------|--------|--------|----------------------------|
     * | difficulty | String | "high" | 선택한 난이도               |
     *
     * ## 측정 목적
     * - 전체 사용자의 난이도 선호도 분포 파악
     * - User Property로도 등록하여 세그먼트별 리텐션 분석에 활용
     *
     * ## 발생 시점
     * - [OnBoardingViewModel.onDifficultyNextClicked] 호출 시
     *   (OptimizeDataScreen "다음으로" 버튼, NONE 제외)
     */
    object DifficultySelect {
        const val NAME = "difficulty_select"
        const val PARAM_DIFFICULTY = "difficulty"  // "high" | "mid" | "low"
    }

    /**
     * ONB-011 · 온보딩 최종 완료 이벤트
     *
     * 파라미터 없음 — 완료 자체가 이벤트
     *
     * ## 측정 목적
     * - 온보딩 완료율: `onboarding_complete / onboarding_start`
     * - 핵심 설정값 분포 분석 (User Property 세그먼트 활용)
     *
     * ## 발생 시점
     * - [OnBoardingViewModel.submitOnBoarding] 내 모든 API 호출 성공 후
     *   [UiStateOnboardingScreenState.Success] 전환 직전 (로딩 완료)
     */
    object OnboardingComplete {
        const val NAME = "onboarding_complete"
    }

    /**
     * ONB-PDF-1 · PDF 업로드 시작 이벤트
     *
     * | 파라미터      | 타입 | 예시   | 목적                      |
     * |-------------|------|--------|--------------------------|
     * | file_size_kb | Long | 2048   | 업로드 파일 크기 (KB)      |
     * | page_count   | Int  | 30     | PDF 페이지 수             |
     *
     * ## 측정 목적
     * - PDF 업로드 시도 횟수 누적 (User Property로도 등록)
     * - 업로드 파일 크기·페이지 수 분포 파악
     *
     * ## 발생 시점
     * - [OnBoardingViewModel.uploadDocumentInternal] / [AddGoalViewModel.uploadDocumentInternal]
     *   에서 [UploadDocumentUseCase] 호출 직전 (서버 presigned URL 요청 트리거 시점)
     */
    object PdfUploadStart {
        const val NAME = "pdf_upload_start"
        const val PARAM_FILE_SIZE_KB = "file_size_kb"  // KB 단위 정수
        const val PARAM_PAGE_COUNT = "page_count"      // PDF 페이지 수
    }

    /**
     * Firebase Analytics User Property 키 목록
     *
     * User Property는 최대 25개, 키 최대 24자, 값 최대 36자 제한.
     */
    object UserProperties {
        /** 선호 하루 퀴즈 수 — 온보딩 SetRoutineScreen에서 설정 */
        const val QUIZ_COUNT = "quiz_count"  // "3" | "5" | "7" | "10"

        /** 집에서 나오는 시간 — 온보딩 SetRoutineScreen에서 설정, 포맷: "HH:mm" */
        const val COMMUTE_TIME_FIRST = "commute_time_first"   // "08:00"

        /** 집에 돌아가는 시간 — 온보딩 SetRoutineScreen에서 설정, 포맷: "HH:mm" */
        const val COMMUTE_TIME_SECOND = "commute_time_second" // "18:00"

        /**
         * OS 종류 — 수동·자동 로그인 성공 시 설정
         *
         * 수동: [LoginViewModel.handleLoginResult] 성공 시
         * 자동: [SplashViewModel.tryAutoLogin] 성공 시
         */
        const val OS_TYPE = "os_type"  // "Android" | "iOS"

        /** 알림 활성 상태 — 온보딩 알림 권한 허용 후 설정 */
        const val NOTIFY_ENABLED = "notify_enabled"  // "true" | "false"

        /** 학습 방식 선호도 — 온보딩 SelectLearningMethodScreen에서 설정 */
        const val LEARNING_TYPE = "learning_type"  // "category" | "pdf"

        /** 선택한 1뎁스 카테고리명 — 온보딩 CategorySelectScreen에서 설정 */
        const val CATEGORY_DEPTH1 = "depth1"

        /** 선택한 2뎁스 카테고리명 — 온보딩 CategorySelectScreen에서 설정 */
        const val CATEGORY_DEPTH2 = "depth2"

        /** 선택한 3뎁스 카테고리명 — 온보딩 CategorySelectScreen에서 설정 */
        const val CATEGORY_DEPTH3 = "depth3"

        /** 선택한 난이도 — 온보딩 OptimizeDataScreen에서 설정 */
        const val DIFFICULTY = "difficulty"  // "high" | "mid" | "low"

        /** PDF 업로드 시도 횟수 — [TeumAnalyticsLogger.logPdfUploadStart] 호출 시 누적 증가 */
        const val PDF_UPLOAD_ATTEMPT_COUNT = "pdf_upload_attempt_count"  // "1", "2", ...

        /** 온보딩 완료 여부 — 온보딩 최종 완료 시 "complete"로 설정 */
        const val ONBOARDING_COMPLETE = "onboarding_complete"  // "not_yet" | "complete"

        /**
         * 요약본 최초 열람 여부 — [SummaryViewStart] 이벤트 첫 발화 시 "true"로 설정
         *
         * GA4 잠재고객 빌더에서 has_viewed_summary ≠ "true" 세그먼트로
         * "온보딩 완료 후 학습 미진입 이탈 위험군" 식별 → FCM 리마인더 타겟팅에 직접 활용
         */
        const val HAS_VIEWED_SUMMARY = "has_viewed_summary"  // "true"

        /**
         * 소셜 로그인 방식 — 수동/자동 로그인 성공 시 설정
         *
         * 수동: [LoginViewModel.handleLoginResult] 성공 시
         * 자동: [SplashViewModel.tryAutoLogin] 성공 시 ([TokenLocalDataSource.getProvider] 참조)
         */
        const val LOGIN_METHOD = "login_method"  // "kakao" | "google"
    }

    /**
     * SUM-001 · 요약본 첫 페이지 노출 이벤트
     *
     * | 파라미터   | 타입   | 예시        | 목적                              |
     * |-----------|--------|-------------|----------------------------------|
     * | session_id | String | "42"        | 현재 학습 목표(goalId) 식별자     |
     * | content_id | String | "17"        | 노출된 요약 콘텐츠 ID             |
     * | topic      | String | "SwiftUI"   | 요약 콘텐츠 제목 (최대 100자)     |
     *
     * ## 측정 목적
     * - `onboarding_complete → summary_view_start` 전환율로 온보딩 학습 유도 효과 판단
     * - topic 세분화로 인기 콘텐츠 분포 파악 (GA4 맞춤 측정기준 등록 필요)
     * - User Property [UserProperties.HAS_VIEWED_SUMMARY]를 "true"로 설정하여
     *   학습 미진입 이탈 위험군 세그먼트 식별 (FCM 리마인더 타겟팅 활용)
     *
     * ## 발생 시점
     * - [SummaryViewModel.loadCategorySummary] / [SummaryViewModel.loadDocumentSummary]
     *   API 성공 후 최초 1회 (ViewModel 인스턴스당 1회 보장)
     */
    object SummaryViewStart {
        const val NAME = "summary_view_start"
        const val PARAM_SESSION_ID = "session_id"   // goalId.toString()
        const val PARAM_CONTENT_ID = "content_id"   // categoryDocumentId 또는 documentId
        const val PARAM_TOPIC = "topic"              // 콘텐츠 제목 (max 100자)
    }

    /**
     * SUM-002 · 요약본 완독 이벤트
     *
     * | 파라미터   | 타입   | 예시        | 목적                                    |
     * |-----------|--------|-------------|----------------------------------------|
     * | session_id | String | "42"        | 현재 학습 목표(goalId) 식별자           |
     * | content_id | String | "17"        | 요약 콘텐츠 ID — summary_view_start JOIN 키 |
     * | topic      | String | "SwiftUI"   | 요약 콘텐츠 제목 (최대 100자)           |
     *
     * ## 발화 조건 (AND)
     * 1. API 응답 완전 수신 완료 (`UiScreenState.Success`)
     * 2. 스크롤 최하단 도달
     *
     * ## 오탐 방지
     * 콘텐츠 로딩 완료 전 최하단 스크롤은 이벤트를 발화하지 않는다.
     * ViewModel 인스턴스당 최초 1회만 발송 (`hasSummaryViewCompleteLogged` 플래그).
     *
     * ## 측정 목적
     * - 완독률 퍼널: `summary_view_start → summary_view_complete`
     * - content_id 기준 JOIN으로 콘텐츠별 완독률 세분화
     * - topic 기준으로 주제별 완독률 비교 및 이탈 구간 개선 판단
     */
    object SummaryViewComplete {
        const val NAME = "summary_view_complete"
        const val PARAM_SESSION_ID = "session_id"   // goalId.toString()
        const val PARAM_CONTENT_ID = "content_id"   // categoryDocumentId 또는 documentId
        const val PARAM_TOPIC = "topic"              // 콘텐츠 제목 (max 100자)
    }

    /**
     * QUIZ-001 · 퀴즈 화면 진입 이벤트
     *
     * | 파라미터   | 타입   | 예시      | 목적                                    |
     * |-----------|--------|-----------|-----------------------------------------|
     * | content_id | String | "17"      | summary_view_start/complete 와 JOIN 키  |
     * | topic      | String | "SwiftUI" | 요약 콘텐츠 제목 (최대 100자)           |
     * | quiz_count | Long   | 5         | 총 문제 수                              |
     * | difficulty | String | "high"    | 사용자 난이도 설정                      |
     * | entry_type | String | "first"   | 진입 유형 — 아래 값 정의 참조           |
     *
     * ## entry_type 값 정의
     * - `"first"`  : 최초 진입
     * - `"resume"` : 미완료 재진입 (complete-set API 미호출 상태에서 재진입)
     * - `"retry"`  : 완료 후 재진입 (complete-set API 성공 이력 있음)
     *
     * 판단 로직은 [com.teumteumeat.teumteumeat.data.datastore.QuizTrackingDataStore.resolveEntryType] 참조.
     * 앱 재설치 시 로컬 이력이 초기화되어 "first"로 재발생하는 것은 의도된 동작.
     *
     * ## 측정 목적
     * - `entry_type = "first"` 필터링 → 전체 유저 중 퀴즈를 한 번이라도 시작한 유저 비율
     * - `entry_type = "retry"` 카운트 → 완료 후 재도전 횟수로 학습 참여 깊이 측정
     * - `entry_type = "resume"` 카운트 → 미완료 이탈 후 복귀율 파악
     * - content_id 기준 summary_view_complete 와 JOIN하여 완독 → 퀴즈 진입 전환율 산출
     *
     * ## 발생 시점
     * - [com.teumteumeat.teumteumeat.ui.screen.b2_quiz.QuizViewModel.loadQuizzes] API 성공 후
     *   ViewModel 인스턴스당 최초 1회 (재시도로 재성공해도 중복 발송 방지)
     */
    object QuizStart {
        const val NAME = "quiz_start"
        const val PARAM_CONTENT_ID = "content_id"    // documentId.toString()
        const val PARAM_TOPIC = "topic"               // 콘텐츠 제목 (max 100자)
        const val PARAM_QUIZ_COUNT = "quiz_count"     // 총 문제 수
        const val PARAM_DIFFICULTY = "difficulty"     // "high" | "mid" | "low"
        const val PARAM_ENTRY_TYPE = "entry_type"     // "first" | "resume" | "retry"
    }

    /**
     * APP-001 · 앱 설치 또는 업데이트 후 첫 시작 이벤트
     *
     * | 파라미터      | 타입   | 예시    | 목적                          |
     * |--------------|--------|---------|-------------------------------|
     * | version_code | String | "17"    | 설치/업데이트된 버전 코드 식별 |
     * | version_name | String | "1.0.17"| 사람이 읽기 쉬운 릴리즈 식별자 |
     *
     * ## 발생 조건
     * SharedPreferences에 저장된 마지막 발송 versionCode와 현재 versionCode가
     * 다를 때만 1회 발송합니다.
     * - 최초 설치 후 첫 실행 → 이벤트 발송
     * - 앱 업데이트 후 첫 실행 → 이벤트 발송
     * - 동일 버전 재시작 → 발송 안 함
     * - 재설치·데이터 초기화 → 플래그 리셋으로 재발송
     *
     * ## 발생 시점
     * [TeumAnalyticsLogger] 초기화 시 [TeumAnalyticsLogger.logAppInstallOrUpdateIfNeeded] 호출
     */
    object AppInstallOrUpdate {
        const val NAME = "app_install_or_update"
        const val PARAM_VERSION_CODE = "version_code"  // 예: "17"
        const val PARAM_VERSION_NAME = "version_name"  // 예: "1.0.17"
    }
}
