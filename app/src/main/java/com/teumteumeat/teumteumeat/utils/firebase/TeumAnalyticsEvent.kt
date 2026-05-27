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
        const val PARAM_ERROR_CODE = "error_code"       // 서버 코드 or "NETWORK_ERROR" / "UNKNOWN_ERROR"
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
     * Firebase Analytics User Property 키 상수
     *
     * User Property는 이벤트 파라미터와 달리 세션 전반에 유지되어
     * GA4 Audience · Exploration 보고서에서 **사용자 기준 세분화**에 활용됩니다.
     *
     * 규칙:
     * - 키 최대 24자 (snake_case)
     * - 값 최대 36자
     * - 프로젝트당 최대 25개 등록 가능
     *
     * ## GA4 필터링 방법
     * GA4 콘솔 → Audience 빌더 → 조건: `app_version_code` >= 17
     * Exploration → 사용자 속성 필터: `app_version_code`
     *
     * ## 버전코드 정책
     * - `app_version_code`: versionCode (Long → String 변환하여 저장)
     *   예) "17", "18", "100"
     * - `app_version_name`: versionName (예: "1.0.17", "2.0.0")
     * - 앱 시작 시 [TeumAnalyticsLogger] init 블록에서 자동 설정됨
     */
    object UserProperty {
        /** 버전코드 (Long → String) — GA4 필터링 기준 값 */
        const val APP_VERSION_CODE = "app_version_code"  // 예: "17"

        /** 버전명 — 사람이 읽기 쉬운 릴리즈 식별자 */
        const val APP_VERSION_NAME = "app_version_name"  // 예: "1.0.17"
    }
}