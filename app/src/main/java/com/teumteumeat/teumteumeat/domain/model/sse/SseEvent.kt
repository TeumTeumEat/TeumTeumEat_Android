package com.teumteumeat.teumteumeat.domain.model.sse

/**
 * 서버 SSE 스트리밍의 비즈니스 이벤트를 나타내는 Domain 모델.
 *
 * ### 서버 이벤트 흐름 (순서 보장)
 * ```
 * CONNECT  →  message (0~N회)  →  title
 * ```
 * - `title` 수신이 스트리밍 종료 조건
 *
 * ### Data Layer 매핑 기준 (`event:` 필드)
 * | 서버 `event:` 값 | Domain 이벤트          |
 * |-----------------|----------------------|
 * | `CONNECT`       | [Connected]          |
 * | `message`       | [Chunk]              |
 * | `title`         | [TitleReceived]      |
 * | 연결 오류        | [StreamError]        |
 *
 * Android 프레임워크 및 OkHttp 의존성 없음 — 순수 Kotlin 모델.
 */
sealed class SseEvent {

    /**
     * 서버가 SSE 스트리밍 연결을 수락했음을 알리는 신호.
     * `event: CONNECT` 수신 시 방출된다.
     */
    data object Connected : SseEvent()

    /**
     * 요약 본문의 텍스트 청크.
     * `event: message` 수신 시 방출되며, 한 글자 단위부터 수 글자 단위까지 가변적이다.
     *
     * @param text 청크 단위 텍스트. 소비자는 누적하여 전체 본문을 조합한다.
     */
    data class Chunk(val text: String) : SseEvent()

    /**
     * 요약 제목 완성본 수신. 이 이벤트가 스트리밍 종료를 의미한다.
     * `event: title` 수신 시 방출된다.
     *
     * @param title 서버가 생성한 요약 제목 전문.
     */
    data class TitleReceived(val title: String) : SseEvent()

    /**
     * 스트리밍 중 네트워크 오류 또는 파싱 실패 발생.
     * 재연결 정책 소진 후 최종 실패 시 방출된다.
     *
     * @param throwable 원인 예외.
     */
    data class StreamError(val throwable: Throwable) : SseEvent() {
        // Throwable은 equals()를 오버라이드하지 않아 참조 동일성으로 비교된다.
        // 메시지 기준 동등성으로 재정의하여 StateFlow distinctUntilChanged 등에서 의도한 동작 보장.
        override fun equals(other: Any?): Boolean =
            other is StreamError && throwable.message == other.throwable.message

        override fun hashCode(): Int = throwable.message.hashCode()
    }
}