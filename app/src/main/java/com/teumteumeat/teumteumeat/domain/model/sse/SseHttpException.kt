package com.teumteumeat.teumteumeat.domain.model.sse

/**
 * HTTP 오류 응답(4xx/5xx)을 나타내는 SSE 전용 예외.
 *
 * [SseEventSourceListener]가 [code] >= 400 응답 수신 시 생성하며,
 * [SseClient]의 `retryWhen`은 이 예외를 감지해 재시도를 즉시 중단한다.
 * (HTTP 오류는 재연결로 해결되지 않기 때문)
 *
 * 응답 바디가 `{ "code": ..., "message": ... }` 형태이면 [errorCode]·[errorMessage]에
 * 파싱 결과를 담는다. 비즈니스 분기(예: `GOAL-003`)는 이 코드를 기준으로 처리한다.
 *
 * @param code         HTTP 상태 코드 (예: 400, 401, 403, 404)
 * @param errorCode    응답 바디의 비즈니스 에러 코드 (예: `GOAL-002`). 없으면 null.
 * @param errorMessage 응답 바디의 사용자 메시지. 없으면 null.
 */
class SseHttpException(
    val code: Int,
    val errorCode: String? = null,
    val errorMessage: String? = null
) : Exception("SSE HTTP error: $code (errorCode=$errorCode)")
