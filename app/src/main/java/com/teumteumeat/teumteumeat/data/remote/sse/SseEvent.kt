package com.teumteumeat.teumteumeat.data.remote.sse

/**
 * SSE 연결 수명 주기와 스트림 데이터를 나타내는 sealed class.
 *
 * - [Opened] : 서버 핸드셰이크 완료
 * - [Message] : `text/event-stream` 단일 이벤트 수신
 * - [Closed] : 서버가 정상적으로 스트림 종료
 * - [Failure] : 네트워크/프로토콜 오류 발생 (재연결 대상)
 */
sealed class SseEvent {

    /** 서버와의 SSE 연결이 성공적으로 수립되었을 때 */
    data object Opened : SseEvent()

    /**
     * `text/event-stream` 규격에 따른 단일 이벤트.
     *
     * @param id   이벤트 식별자 (`id:` 필드). 서버가 생략하면 null.
     * @param type 이벤트 타입 (`event:` 필드). 서버가 생략하면 null → 클라이언트는 "message"로 처리.
     * @param data 이벤트 본문 (`data:` 필드).
     */
    data class Message(
        val id: String?,
        val type: String?,
        val data: String
    ) : SseEvent()

    /** 서버가 스트림을 정상 종료했을 때. 재연결 없이 Flow 완료. */
    data object Closed : SseEvent()

    /**
     * 연결 오류 발생. [SseClient] 내부에서 최대 3회까지 재연결을 시도한다.
     *
     * [okhttp3.Response] 원본 대신 필요한 HTTP 메타데이터만 추출하여 보관한다.
     * → 소비자가 `response.close()`를 누락해도 커넥션 풀 누수 없음.
     *
     * @param cause       원인 예외. OkHttp가 전달하지 않으면 null.
     * @param httpCode    오류 시점의 HTTP 상태 코드. 응답이 없으면 null.
     * @param httpMessage 오류 시점의 HTTP 상태 메시지. 응답이 없으면 null.
     */
    data class Failure(
        val cause: Throwable?,
        val httpCode: Int?,
        val httpMessage: String?
    ) : SseEvent()
}