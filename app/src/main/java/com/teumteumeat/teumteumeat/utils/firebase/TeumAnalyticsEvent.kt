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
     * Firebase Analytics User Property 키 목록
     *
     * User Property는 최대 25개, 키 최대 24자, 값 최대 36자 제한.
     */
    object UserProperties {
        /** 선호 하루 퀴즈 수 — 온보딩 SetRoutineScreen에서 설정 */
        const val QUIZ_COUNT = "quiz_count"  // "3" | "5" | "7" | "10"
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
