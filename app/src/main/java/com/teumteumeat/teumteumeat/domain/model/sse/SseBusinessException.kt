package com.teumteumeat.teumteumeat.domain.model.sse

/**
 * SSE 스트리밍 중 서버가 반환한 비즈니스 에러를 나타내는 Domain 예외.
 *
 * HTTP 4xx 응답 바디의 `{ "code", "message" }`를 그대로 보존하여
 * Presentation 레이어가 [errorCode]에 따라 분기 처리하도록 한다.
 *
 * ### 주요 코드
 * | code        | 의미                                  | UI 처리                         |
 * |-------------|--------------------------------------|--------------------------------|
 * | `GOAL-002`  | 목표 학습 기간 종료                    | 홈 이동 → 새 목표 안내 다이얼로그 |
 * | `GOAL-003`  | 목표 학습 횟수 완료                    | 홈 이동 → 새 목표 안내 다이얼로그 |
 * | `QUIZ-002`  | 퀴즈 풀이 횟수 소진(요약 재생성 불가)   | 기존 요약글 GET 조회             |
 *
 * Android 프레임워크 의존성 없음 — 순수 Kotlin 모델.
 *
 * @param errorCode 서버 비즈니스 에러 코드.
 * @param message   서버 사용자 메시지.
 */
class SseBusinessException(
    val errorCode: String?,
    override val message: String?
) : Exception(message)
