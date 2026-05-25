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
     * | 파라미터    | 타입   | 예시     | 목적                  |
     * |------------|--------|----------|-----------------------|
     * | method     | String | kakao    | 로그인 방식별 전환율  |
     */
    object LoginComplete {
        const val NAME = "login_complete"
        const val PARAM_METHOD = "method"   // "kakao" | "google"
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
}