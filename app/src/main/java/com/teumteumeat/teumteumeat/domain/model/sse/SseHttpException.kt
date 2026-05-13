package com.teumteumeat.teumteumeat.domain.model.sse

/**
 * HTTP 오류 응답(4xx/5xx)을 나타내는 SSE 전용 예외.
 *
 * [SseEventSourceListener]가 [code] >= 400 응답 수신 시 생성하며,
 * [SseClient]의 `retryWhen`은 이 예외를 감지해 재시도를 즉시 중단한다.
 * (HTTP 오류는 재연결로 해결되지 않기 때문)
 *
 * @param code HTTP 상태 코드 (예: 400, 401, 403, 404)
 */
class SseHttpException(val code: Int) : Exception("SSE HTTP error: $code")