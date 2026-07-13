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

        /**
         * 유저 전체 누적 퀴즈 문항 응답 수 — [QuizAnswerSubmit] 발화 시마다 갱신
         *
         * 재도전(retry) 시에도 초기화되지 않고 계속 증가한다 (24자, GA4 키 최대 길이 제한 통과).
         */
        const val TOTAL_QUESTIONS_ANSWERED = "total_questions_answered"  // "1", "2", ...

        /** 가장 최근 완주한 목표의 기간(주 단위) — [CourseComplete] 발화 시 설정 */
        const val GOAL_WEEKS = "goal_weeks"  // "2", "4", ...
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
     * QUIZ-002 · 개별 문항 제출 이벤트
     *
     * | 파라미터    | 타입   | 예시   | 목적                                    |
     * |------------|--------|--------|-----------------------------------------|
     * | content_id  | String | "17"   | quiz_start 와 JOIN 키                   |
     * | question_no | Long   | 12     | 전역 누적 문항 응답 수 (재도전해도 계속 증가) |
     * | answer_type | String | "ox"   | 문항 유형                                |
     * | is_correct  | String | "true" | 정답 여부                                |
     *
     * ## 측정 목적
     * - `quiz_start(entry_type=first) 대비 quiz_answer_submit 발생 유저 비율` → 실제 풀이 전환율
     * - User Property [UserProperties.TOTAL_QUESTIONS_ANSWERED]로 유저별 누적 풀이량 세그먼트 분석
     *
     * ## 발생 시점
     * - [com.teumteumeat.teumteumeat.ui.screen.b2_quiz.QuizViewModel.submitAnswer]
     *   서버 응답 수신(`ApiResultV2.Success`) 시마다
     */
    object QuizAnswerSubmit {
        const val NAME = "quiz_answer_submit"
        const val PARAM_CONTENT_ID = "content_id"       // documentId.toString()
        const val PARAM_QUESTION_NO = "question_no"     // 전역 누적값
        const val PARAM_ANSWER_TYPE = "answer_type"     // "ox" | "mcq"
        const val PARAM_IS_CORRECT = "is_correct"       // "true" | "false"
    }

    /**
     * QUIZ-003 · 퀴즈 풀이 중 이탈 이벤트
     *
     * | 파라미터         | 타입   | 예시    | 목적                                    |
     * |------------------|--------|---------|-----------------------------------------|
     * | content_id       | String | "17"    | quiz_start 와 JOIN 키                   |
     * | last_question_no | Long   | 3       | 세션 내 마지막 제출 문항 번호 (0=미풀이) |
     * | quiz_count       | Long   | 5       | 이 세션의 전체 문항 수                   |
     * | entry_type       | String | "first" | 진입 유형                                |
     *
     * `last_question_no`는 [QuizAnswerSubmit.PARAM_QUESTION_NO](전역 누적값)와 무관한
     * ViewModel 인스턴스 전용 세션 카운터다. 전역 누적값을 재사용하면 재도전 이후
     * 항상 quiz_count보다 커져 이탈이 감지되지 않기 때문에 분리했다.
     *
     * ## 측정 목적
     * - 마지막 제출 문항 번호 기준 이탈 구간 분포 파악
     *
     * ## 발생 시점
     * - [com.teumteumeat.teumteumeat.ui.screen.b2_quiz.QuizViewModel.onCleared]에서
     *   세션 내 제출 수가 quiz_count보다 적을 때만
     */
    object QuizAbandoned {
        const val NAME = "quiz_abandoned"
        const val PARAM_CONTENT_ID = "content_id"
        const val PARAM_LAST_QUESTION_NO = "last_question_no"  // 세션 전용 카운터, 0=미풀이
        const val PARAM_QUIZ_COUNT = "quiz_count"
        const val PARAM_ENTRY_TYPE = "entry_type"               // "first" | "resume" | "retry"
    }

    /**
     * QUIZ-004 · 퀴즈 세트 완료 이벤트
     *
     * | 파라미터      | 타입   | 예시      | 목적                                        |
     * |--------------|--------|-----------|---------------------------------------------|
     * | content_id    | String | "17"      | quiz_start 와 JOIN 키                       |
     * | topic         | String | "SwiftUI" | 콘텐츠 제목 (최대 100자)                     |
     * | difficulty    | String | "high"    | 사용자 난이도 설정                           |
     * | entry_type    | String | "first"   | 진입 유형 — "first" \| "resume" \| "retry"  |
     * | quiz_count    | Long   | 5         | 전체 문항 수                                 |
     * | correct_count | Long   | 3         | 정답 문항 수                                 |
     * | score_rate    | String | "60.0"    | 정답률(%) — Float 계산 후 String 변환        |
     *
     * ## 측정 목적
     * - `quiz_start → quiz_complete` 전환율로 퀴즈 완료율 산출 (entry_type별 분리 가능)
     * - difficulty·entry_type별 score_rate 평균으로 난이도 체감도 및 재도전 성향 분석
     * - content_id 기준 quiz_answer_submit 과 JOIN하여 answer_type별 정답률 세분화 (BigQuery)
     *
     * ## 발생 시점
     * - [com.teumteumeat.teumteumeat.ui.screen.b2_quiz.QuizViewModel.completeCurrentQuizSet]
     *   `POST /api/v1/user-quizzes/complete-set` 성공(`ApiResultV2.Success`) 시,
     *   [com.teumteumeat.teumteumeat.data.datastore.QuizTrackingDataStore.markQuizCompleted]
     *   호출 직후
     *
     * ## 주의
     * - difficulty 조회 실패로 quiz_start 자체가 스킵된 경우 (`resolvedDifficulty`가
     *   [com.teumteumeat.teumteumeat.domain.model.goal.Difficulty.NONE]으로 남음)
     *   quiz_complete도 동일한 가드로 함께 스킵된다 — quiz_start 없는 quiz_complete 단독 발생을
     *   막아 JOIN 분석 일관성을 지키기 위한 의도된 동작이다.
     * - quiz_count가 0이면 score_rate는 "0.0"으로 방어 처리한다.
     */
    object QuizComplete {
        const val NAME = "quiz_complete"
        const val PARAM_CONTENT_ID = "content_id"
        const val PARAM_TOPIC = "topic"
        const val PARAM_DIFFICULTY = "difficulty"       // "high" | "mid" | "low"
        const val PARAM_ENTRY_TYPE = "entry_type"       // "first" | "resume" | "retry"
        const val PARAM_QUIZ_COUNT = "quiz_count"
        const val PARAM_CORRECT_COUNT = "correct_count"
        const val PARAM_SCORE_RATE = "score_rate"       // Float → String, 예: "60.0"
    }

    /**
     * STAMP-001 · 스탬프 적립 이벤트
     *
     * | 파라미터       | 타입   | 예시 | 목적                        |
     * |---------------|--------|------|-----------------------------|
     * | content_id     | String | "17" | quiz_complete 와 JOIN 키    |
     * | streak_count   | Long   | 3    | 연속 학습일수               |
     * | total_stamps   | Long   | 42   | 누적 스탬프 수              |
     * | monthly_stamps | Long   | 5    | 이번 달 스탬프 수           |
     *
     * ## 측정 목적
     * - content_id별 스탬프 적립 횟수로 콘텐츠 학습 관심도 파악
     * - streak_count 분포로 리텐션 패턴 분석
     * - total_stamps/monthly_stamps 추이로 누적 학습량 파악
     *
     * ## 발생 조건 (변경 감지 방식)
     * - 퀴즈 진입 전 `user-quizzes/status`의 hasSolvedToday(before)가 false였고,
     * - `POST /api/v1/user-quizzes/complete-set` 성공 후 재조회한 hasSolvedToday(after)가
     *   true로 바뀐 경우에만 — 즉 "오늘 하루 중 첫 퀴즈 완료"일 때만 발화한다.
     * - 오늘 이미 완료 후 재도전(retry)인 경우, 혹은 재조회/캘린더 조회 API 실패 시에는
     *   발화하지 않는다. 이 경우에도 quiz_complete 발화에는 영향을 주지 않는다.
     *
     * ## 발생 시점
     * - [com.teumteumeat.teumteumeat.ui.screen.b2_quiz.QuizViewModel.completeCurrentQuizSet]
     *   에서 quiz_complete 로깅 직후, 위 조건 충족 시
     */
    object StampEarned {
        const val NAME = "stamp_earned"
        const val PARAM_CONTENT_ID = "content_id"
        const val PARAM_STREAK_COUNT = "streak_count"
        const val PARAM_TOTAL_STAMPS = "total_stamps"
        const val PARAM_MONTHLY_STAMPS = "monthly_stamps"
    }

    /**
     * QUIZ-005 · 퀴즈 결과 화면 "글보기" 버튼 탭 이벤트
     *
     * | 파라미터   | 타입   | 예시      | 목적                                    |
     * |-----------|--------|-----------|-----------------------------------------|
     * | content_id | String | "17"      | quiz_complete 와 JOIN 키                |
     * | topic      | String | "SwiftUI" | 콘텐츠 제목 (최대 100자)                |
     * | entry_type | String | "first"   | 진입 유형 — "first" \| "resume" \| "retry" |
     *
     * `entry_type`은 [QuizStart]에서 계산된 값을 그대로 재사용한다.
     * 결과 화면 시점에는 이미 `complete-set` API가 성공해 로컬 이력에
     * documentId가 기록된 상태라, [com.teumteumeat.teumteumeat.data.datastore.QuizTrackingDataStore.resolveEntryType]을
     * 다시 호출하면 항상 "retry"가 반환되어 재계산할 수 없다.
     *
     * ## 측정 목적
     * - quiz_complete를 분모, review_concept_tap을 분자로 하여 콘텐츠별·entry_type별 복습 이용률 산출
     *
     * ## 발생 시점
     * - [com.teumteumeat.teumteumeat.ui.screen.b3_quiz_result.QuizResultViewModel.onReviewConceptTap]
     *   퀴즈 결과 화면에서 "글보기" 버튼 탭 직후 (quiz_complete 이후 결과 화면에서만 노출되는 버튼이므로
     *   quiz_abandoned 상태에서는 발생하지 않는다)
     */
    object ReviewConceptTap {
        const val NAME = "review_concept_tap"
        const val PARAM_CONTENT_ID = "content_id"    // documentId.toString()
        const val PARAM_TOPIC = "topic"               // 콘텐츠 제목 (max 100자)
        const val PARAM_ENTRY_TYPE = "entry_type"     // "first" | "resume" | "retry"
    }

    /**
     * GOAL-001 · 목표 완주 이벤트
     *
     * | 파라미터           | 타입   | 예시      | 목적                                       |
     * |-------------------|--------|-----------|---------------------------------------------|
     * | goal_id            | String | "42"      | 완주한 목표 식별 ID                          |
     * | category_id        | String | "17"      | CATEGORY 목표: categoryId / DOCUMENT 목표: 파일명 |
     * | learning_type      | String | "category"| 학습 방식 — "category" \| "pdf"              |
     * | goal_weeks         | Long   | 2         | 목표 기간(주) — startDate~endDate 근사 계산   |
     * | total_stamps       | Long   | 42        | 완주 시점까지 획득한 전체 누적 스탬프         |
     * | is_first_complete  | String | "true"    | 첫 완주 여부 — "true" \| "false"             |
     *
     * `category_id`는 DOCUMENT(PDF 업로드형) 목표는 category가 null이라 대신 파일명을 전달한다.
     * `goal_weeks`는 API가 주 단위 필드를 제공하지 않아 `ChronoUnit.WEEKS.between(startDate, endDate)`로
     * 클라이언트에서 근사 계산한 값이다.
     *
     * ## 측정 목적
     * - 퀴즈를 한 번이라도 시작한 유저(quiz_start) 대비 완주 달성 유저 비율 산출
     * - category_id·learning_type·goal_weeks·difficulty(User Property)별 완주율 세분화
     *
     * ## 발생 시점
     * - [com.teumteumeat.teumteumeat.ui.screen.b3_quiz_result.QuizResultViewModel.onCourseCompleteScreenEntered]
     *   [com.teumteumeat.teumteumeat.ui.screen.b3_quiz_result.QuizResultNavHost]의 `goEndScreen`에서
     *   `userGoal.isCompleted == true`로 완주 화면(SubjectCompleteScreen)에 진입하기 직전
     */
    object CourseComplete {
        const val NAME = "course_complete"
        const val PARAM_GOAL_ID = "goal_id"
        const val PARAM_CATEGORY_ID = "category_id"     // CATEGORY: categoryId / DOCUMENT: fileName
        const val PARAM_LEARNING_TYPE = "learning_type" // "category" | "pdf"
        const val PARAM_GOAL_WEEKS = "goal_weeks"       // Long, ChronoUnit.WEEKS.between 근사값
        const val PARAM_TOTAL_STAMPS = "total_stamps"   // Long, stamp_earned와 동일 기준(getCalendarHistory)
        const val PARAM_IS_FIRST_COMPLETE = "is_first_complete" // "true" | "false"
    }

    /**
     * GOAL-002 · 완주 후 재학습 시작 이벤트
     *
     * | 파라미터           | 타입   | 예시      | 목적                                    |
     * |-------------------|--------|-----------|------------------------------------------|
     * | prev_goal_id       | String | "42"      | 직전에 완주한 목표 식별 ID               |
     * | prev_category_id   | String | "17"      | 직전 완주 목표 category_id (course_complete와 동일 기준) |
     * | prev_learning_type | String | "category"| 직전 완주 목표 학습 방식 — "category" \| "pdf" |
     * | next_learning_type | String | "pdf"     | 새로 선택한 목표 학습 방식 — "category" \| "pdf" |
     * | is_first_complete  | String | "true"    | 직전 완주가 첫 완주였는지 — course_complete 계산값 재사용 |
     *
     * `prev_*`/`is_first_complete`는 [com.teumteumeat.teumteumeat.data.datastore.GoalTrackingDataStore]에
     * `course_complete` 발화 시점에 저장해둔 스냅샷([com.teumteumeat.teumteumeat.data.datastore.LastCompletedGoal])을
     * 그대로 재사용한다 — `AddGoalActivity` 진입 경로(완주 화면 / Home "+" / GuideExpiredGoalActivity 등)에
     * 관계없이 동일하게 동작한다.
     *
     * ## 측정 목적
     * - `course_complete` 대비 `next_course_start` 발생 유저 비율로 완주 후 재학습 전환율 측정
     * - prev → next learning_type 전환 패턴 분석
     * - is_first_complete별 재학습 전환율 비교
     *
     * ## 발생 시점
     * - [com.teumteumeat.teumteumeat.ui.screen.a4_main.a4_5_add_goal.AddGoalViewModel.initNextCourseStartTracking]
     *   목표 타입이 사전 지정되어 `SelectInputMethodScreen`을 건너뛰는 진입(Home "+", GuideExpiredGoalActivity)에서
     *   `AddGoalActivity` 진입 직후 즉시, 또는
     * - [com.teumteumeat.teumteumeat.ui.screen.a4_main.a4_5_add_goal.AddGoalViewModel.logNextCourseStartIfEligible]
     *   `SelectInputMethodScreen` "다음" 버튼 탭 시
     *
     *   두 경우 모두 `GoalTrackingDataStore`에 직전 완주 스냅샷이 남아있을 때만 발화한다.
     *
     * ## 이탈 분석
     * 별도 이탈 이벤트를 수집하지 않고, BigQuery에서 `course_complete` 발생 후
     * `next_course_start`가 발생하지 않은 유저를 이탈로 정의해 분석한다.
     */
    object NextCourseStart {
        const val NAME = "next_course_start"
        const val PARAM_PREV_GOAL_ID = "prev_goal_id"
        const val PARAM_PREV_CATEGORY_ID = "prev_category_id"     // CATEGORY: categoryId / DOCUMENT: fileName
        const val PARAM_PREV_LEARNING_TYPE = "prev_learning_type" // "category" | "pdf"
        const val PARAM_NEXT_LEARNING_TYPE = "next_learning_type" // "category" | "pdf"
        const val PARAM_IS_FIRST_COMPLETE = "is_first_complete"   // "true" | "false" — course_complete 계산값 재사용
    }

    /**
     * LIB-001 · 히스토리 탭(캘린더) 진입 이벤트
     *
     * | 파라미터           | 타입   | 예시         | 목적                                        |
     * |-------------------|--------|--------------|---------------------------------------------|
     * | month             | String | "2025-05"    | 진입 시 표시 중인 캘린더 월                 |
     * | date              | String | "2025-05-01" | 진입 시점의 날짜                            |
     * | month_stamp_count | Long   | 3            | 표시 월에 획득한 스탬프 수. 로드 실패 시 -1 |
     * | has_month_stamp   | String | "true"       | "true" / "false" / "unknown"(로드 실패)     |
     * | total_stamps      | Long   | 27           | 누적 스탬프 수. 로드 실패 시 -1             |
     *
     * 다른 이벤트와 달리 최초 1회로 제한하지 않는다 — 히스토리 탭에 진입할 때마다 매번 발화한다.
     * 단, 화면 회전·다크모드 전환 등 Activity 재생성으로 인한 컴포지션 재구성 시에는 발화하지 않는다.
     *
     * ## 측정 목적
     * - 일별/월별 히스토리 탭 이용률 측정
     * - `stamp_earned` 발생일과 `calendar_view` 발생일 비교로 스트릭/스탬프 기능의
     *   히스토리 탭 유입 효과 검증
     * - has_month_stamp = "true" 진입 유저 중 `calendar_date_tap`(has_stamp="true") 발생
     *   유저 비율로 스탬프 확인형 vs 단순 조회형 유저 구분 (BigQuery 조인 분석,
     *   GA4 맞춤 측정기준 등록 불필요)
     *
     * ## 발생 시점
     * - [com.teumteumeat.teumteumeat.ui.screen.a4_main.a4_2_library.LibraryViewModel.onLibraryScreenEntered]
     *   [com.teumteumeat.teumteumeat.ui.screen.a4_main.a4_2_library.LibraryScreen]의
     *   `DisposableEffect(Unit)`에서 진입할 때마다 호출 (하단 탭 재방문 포함)
     * - 첫 진입 시에는 캘린더 데이터 로드 완료 후 발화한다 — 스탬프 파라미터에 정확한 값을
     *   싣기 위함. 로드 실패 시에도 unknown/-1로 발화해 탭 이용률 모수는 보존한다.
     *   세션 만료 시에는 발화하지 않는다.
     * - Activity 재생성(config change)으로 인한 재진입 시에는 ViewModel의 발화 플래그로 중복 발화 차단
     *   — 탭을 실제로 떠날 때(`onDispose && !isChangingConfigurations`)만 플래그 리셋
     */
    object CalendarView {
        const val NAME = "calendar_view"
        const val PARAM_MONTH = "month" // "yyyy-MM", 예: "2025-05"
        const val PARAM_DATE = "date"   // "yyyy-MM-dd", 예: "2025-05-01"
        const val PARAM_MONTH_STAMP_COUNT = "month_stamp_count" // Long, 로드 실패 시 -1
        const val PARAM_HAS_MONTH_STAMP = "has_month_stamp"     // "true" | "false" | "unknown"
        const val PARAM_TOTAL_STAMPS = "total_stamps"           // Long, 로드 실패 시 -1
    }

    /**
     * LIB-002 · 캘린더 날짜 탭 이벤트
     *
     * | 파라미터   | 타입   | 예시         | 목적                          |
     * |-----------|--------|--------------|-------------------------------|
     * | date      | String | "2025-05-19" | 탭한 날짜                     |
     * | has_stamp | String | "true"       | 탭한 날짜의 스탬프 존재 여부  |
     *
     * 발화 횟수를 제한하지 않는다 — 날짜 셀을 탭할 때마다 매번 발화한다.
     *
     * ## has_stamp 판단 기준
     * - 클라이언트가 캘린더 UI 렌더링과 동일한 소스(서버 응답 기반 `solvedDates`)로 판단
     * - 미스탬프 날짜는 이벤트만 발화되고 화면 동작(선택/일별 상세 조회)은 없다
     *
     * ## 측정 목적
     * - `calendar_view` 대비 `calendar_date_tap` 발생 비율로 히스토리 탭 이용 방식 구분
     *   (단순 조회 vs 날짜 상세 탐색)
     * - has_stamp = "true" 날짜를 탭하는 유저 비율 측정 (스탬프 기록 확인 행동 유저 식별)
     * - GA4 맞춤 측정기준 등록 불필요 — has_stamp 분석은 BigQuery로 처리
     *
     * ## 발생 시점
     * - [com.teumteumeat.teumteumeat.ui.screen.a4_main.a4_2_library.LibraryViewModel.onCalendarDateTapped]
     *   캘린더 날짜 셀 탭 시 호출. 히스토리 탭 진입 시 오늘 날짜 자동 선택
     *   (`onCalendarDateSelected` 직접 호출) 경로에서는 발화하지 않는다.
     */
    object CalendarDateTap {
        const val NAME = "calendar_date_tap"
        const val PARAM_DATE = "date"           // "yyyy-MM-dd", 예: "2025-05-19"
        const val PARAM_HAS_STAMP = "has_stamp" // "true" | "false"
    }

    /**
     * LIB-003 · 일별 학습 기록 카드 탭 이벤트
     *
     * | 파라미터 | 타입   | 예시              | 목적                          |
     * |---------|--------|-------------------|-------------------------------|
     * | date    | String | "2025-05-19"      | 조회한 캘린더 선택 날짜       |
     * | topic   | String | "관세사 관세법"    | 주제명 or PDF 파일명          |
     *
     * 발화 횟수를 제한하지 않는다 — 학습 기록 카드를 탭할 때마다 매번 발화한다.
     *
     * ## 측정 목적
     * - 일자별 과거 학습 내용 재조회 빈도 측정
     * - GA4 맞춤 측정기준 등록 불필요 — 분석은 BigQuery로 처리
     *
     * ## 발생 시점
     * - [com.teumteumeat.teumteumeat.ui.screen.a4_main.a4_2_library.LibraryViewModel.onDailyLearningRecordTapped]
     *   히스토리 탭 진입 → 캘린더 날짜 클릭 → 하단 학습 기록 카드 탭(상세 화면 진입) 시 호출
     * - 주제별 탭의 동일 카드(카테고리 펼침 목록)는 "날짜 클릭" 맥락이 없으므로 발화하지 않는다
     *
     * ## 비고
     * - 스펙 원문의 이벤트명 `learing_record_by_date_tap`은 오타로 확인되어
     *   `learning_record_by_date_tap`으로 교정해 구현 (스펙 시트 측 수정 필요)
     */
    object LearningRecordByDateTap {
        const val NAME = "learning_record_by_date_tap"
        const val PARAM_DATE = "date"   // "yyyy-MM-dd", 예: "2025-05-19"
        const val PARAM_TOPIC = "topic" // 주제명 or PDF 파일명
    }

    /**
     * LIB-004 · 도서관(히스토리 주제별 탭) 진입 이벤트
     *
     * 파라미터 없음.
     *
     * ## 측정 목적
     * - 도서관(주제별 탭) 이용률 측정 — `calendar_view` 대비 발생 비율로
     *   히스토리 화면 내 주제별 탭 이용 비중 파악
     *
     * ## 발생 시점
     * - [com.teumteumeat.teumteumeat.ui.screen.a4_main.a4_2_library.LibraryViewModel.selectLibraryTab]
     *   날짜별 → 주제별 탭 버튼 전환 시 발화 (주제별 탭이 이미 선택된 상태의 재탭은 미발화)
     * - [com.teumteumeat.teumteumeat.ui.screen.a4_main.a4_2_library.LibraryViewModel.onLibraryScreenEntered]
     *   주제별 탭이 유지된 상태로 히스토리 화면에 재진입한 경우도 도서관 진입으로 발화
     *   (Activity 스코프 ViewModel이라 하단 탭 이동 후 복귀 시 탭 상태가 유지됨)
     * - Activity 재생성(화면 회전 등)으로 인한 재진입 시에는 `calendar_view`와 동일한
     *   발화 플래그 + `isChangingConfigurations` 패턴으로 중복 발화 차단
     */
    object LibraryView {
        const val NAME = "library_view"
    }

    /**
     * LIB-005 · 주제별 학습 기록 재조회 이벤트
     *
     * | 파라미터 | 타입   | 예시           | 목적                                  |
     * |---------|--------|----------------|---------------------------------------|
     * | topic   | String | "React Native" | 카드가 속한 카테고리명 or PDF 파일명  |
     * | date    | String | "2025-05-10"   | 클릭한 학습 카드의 학습 날짜          |
     *
     * 발화 횟수를 제한하지 않는다 — 학습 카드를 탭할 때마다 매번 발화한다.
     *
     * ## 측정 목적
     * - 주제별 과거 학습 내용 재조회 빈도 측정
     * - GA4 맞춤 측정기준 등록 불필요 — 분석은 BigQuery로 처리
     *
     * ## 발생 시점
     * - [com.teumteumeat.teumteumeat.ui.screen.a4_main.a4_2_library.LibraryViewModel.onTopicLearningRecordTapped]
     *   주제별 탭 → 카테고리 펼침 → 학습 카드 탭(상세 화면 진입) 시 호출
     * - 날짜별 탭의 동일 카드는 [LearningRecordByDateTap](LIB-003)이 담당하므로 발화하지 않는다
     *
     * ## 비고
     * - 이벤트명의 "modal"은 스펙 시트 유래 — Android 구현은 모달이 아닌
     *   상세 화면(DailySummaryActivity) 이동이지만 스펙과의 정합을 위해 이름을 유지
     */
    object HistoryModalOpen {
        const val NAME = "history_modal_open"
        const val PARAM_TOPIC = "topic" // 카테고리명 or PDF 파일명
        const val PARAM_DATE = "date"   // "yyyy-MM-dd", 예: "2025-05-10"
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

    /**
     * 마이페이지 진입 이벤트
     *
     * | 파라미터 | 타입   | 예시         | 목적           |
     * |---------|--------|--------------|----------------|
     * | date    | String | "2025-01-01" | 방문 날짜 기록 |
     *
     * ## 측정 목적
     * - 마이페이지 이용률 측정
     *
     * ## 발생 시점
     * - [com.teumteumeat.teumteumeat.ui.screen.c1_mypage.MyPageViewModel] init 블록 —
     *   마이페이지 진입(Activity 생성) 1회당 1번 발화
     * - 화면 회전 등 구성 변경 시에는 ViewModel이 유지되므로 재발화하지 않는다
     */
    object MyPageView {
        const val NAME = "mypage_view"
        const val PARAM_DATE = "date" // "yyyy-MM-dd", 예: "2025-01-01"
    }

    /**
     * 학습 주제 변경 완료 이벤트
     *
     * | 파라미터  | 타입   | 예시               | 목적              |
     * |-----------|--------|--------------------|-------------------|
     * | from_type | String | "category" / "pdf" | 변경 전 학습 유형 |
     * | to_type   | String | "category" / "pdf" | 변경 후 학습 유형 |
     *
     * ## 측정 목적
     * - 주제 변경 패턴 파악 (category ↔ pdf 전환 흐름)
     * - 변경 후 학습 미진행으로 인한 이탈 신호 탐지 (GA4 퍼널: topic_change → summary/quiz 이벤트)
     *
     * ## 발생 시점
     * - [com.teumteumeat.teumteumeat.ui.screen.c2_goal_list.GoalListViewModel] onConfirmChangeGoal —
     *   updateGoal 서버 성공 응답 시 1회
     * - 현재 선택된 목표를 재선택한 경우(변경 아님)에는 발화하지 않는다
     */
    object TopicChange {
        const val NAME = "topic_change"
        const val PARAM_FROM_TYPE = "from_type" // "category" or "pdf"
        const val PARAM_TO_TYPE = "to_type"     // "category" or "pdf"
    }
}
