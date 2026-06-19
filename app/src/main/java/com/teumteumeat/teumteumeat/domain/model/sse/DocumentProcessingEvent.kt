package com.teumteumeat.teumteumeat.domain.model.sse

/**
 * 문서 처리 SSE 스트리밍의 비즈니스 이벤트 Domain 모델.
 *
 * ### 서버 이벤트 흐름 (`event:` 필드 기준)
 * | `event:` 값                    | Domain 이벤트                     |
 * |-------------------------------|----------------------------------|
 * | `connect`                     | [Connected]                      |
 * | `document_processing_status`  | [Pending] / [Processing] / [Completed] / [Failed] |
 * | 연결 오류                      | [StreamError]                    |
 *
 * [Completed] 또는 [Failed] 수신 후 Flow 는 추가 이벤트 없이 완료된다.
 */
sealed class DocumentProcessingEvent {

    /** SSE 연결 수락 신호 (`event: connect`, `status: CONNECTED`). */
    data object Connected : DocumentProcessingEvent()

    /** 문서 처리 대기 중 (`status: PENDING`). */
    data object Pending : DocumentProcessingEvent()

    /**
     * 문서 처리 중 (`status: PROCESSING`).
     *
     * @param remainMs 처리 완료까지 남은 예상 시간(ms). 로딩 바 애니메이션 기준값으로 사용.
     */
    data class Processing(val remainMs: Long) : DocumentProcessingEvent()

    /** 문서 처리 완료 (`status: COMPLETED`). 이 이벤트 수신 후 요약글 API 를 호출한다. */
    data object Completed : DocumentProcessingEvent()

    /**
     * 문서 처리 실패 (`status: FAILED`).
     *
     * @param reason 실패 원인.
     */
    data class Failed(val reason: FailureReason) : DocumentProcessingEvent()

    /**
     * SSE 스트리밍 중 네트워크/파싱 오류.
     * [SseClient] 재연결 소진 후 최종 실패 시 방출된다.
     */
    data class StreamError(val throwable: Throwable) : DocumentProcessingEvent() {
        override fun equals(other: Any?) =
            other is StreamError && throwable.message == other.throwable.message

        override fun hashCode() = throwable.message.hashCode()
    }

    enum class FailureReason {
        TIMEOUT,
        SERVER_ERROR,
        ENCRYPTED_FILE,
        UNKNOWN
    }
}