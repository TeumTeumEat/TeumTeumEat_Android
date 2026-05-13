package com.teumteumeat.teumteumeat.data.remote.sse

import com.teumteumeat.teumteumeat.domain.model.sse.SseHttpException
import kotlinx.coroutines.channels.SendChannel
import okhttp3.Response
import okhttp3.sse.EventSource
import okhttp3.sse.EventSourceListener
import java.io.IOException

/**
 * OkHttp [EventSourceListener] 콜백을 [SendChannel]<[SseEvent]>로 중계하는 내부 클래스.
 *
 * - [onOpen]/[onEvent]/[onClosed]는 정상 수명 주기이며 [SendChannel.trySend]로 비블로킹 전송.
 * - [onFailure] 발생 시 [SseEvent.Failure]를 전송한 뒤 채널을 예외와 함께 닫아
 *   [SseClient]의 `retryWhen` 연산자가 재연결을 판단하도록 한다.
 * - [onClosed] 직후 [onFailure]가 연속 호출되는 OkHttp 엣지 케이스에 대비해
 *   채널이 이미 닫힌 경우 [onFailure] 처리를 조기 반환한다.
 */
internal class SseEventSourceListener(
    private val channel: SendChannel<SseEvent>
) : EventSourceListener() {

    override fun onOpen(eventSource: EventSource, response: Response) {
        channel.trySend(SseEvent.Opened)
    }

    override fun onEvent(
        eventSource: EventSource,
        id: String?,
        type: String?,
        data: String
    ) {
        channel.trySend(SseEvent.Message(id = id, type = type, data = data))
    }

    override fun onClosed(eventSource: EventSource) {
        channel.trySend(SseEvent.Closed)
        channel.close() // 정상 종료 → retryWhen 미트리거
    }

    override fun onFailure(
        eventSource: EventSource,
        t: Throwable?,
        response: Response?
    ) {
        // onClosed 직후 onFailure가 연속 호출되는 엣지 케이스를 방어한다.
        // trySend / close 모두 이미 닫힌 채널에서 예외 없이 no-op으로 동작하므로 안전하다.

        // Response 원본은 즉시 해제하고 필요한 HTTP 메타데이터만 추출
        val httpCode = response?.code
        channel.trySend(
            SseEvent.Failure(
                cause = t,
                httpCode = httpCode,
                httpMessage = response?.message
            )
        )

        // HTTP 오류(4xx/5xx)는 SseHttpException으로 래핑 → retryWhen에서 재시도 제외.
        // 네트워크 오류(t != null, response == null)는 t를 그대로 전파 → retryWhen 재시도.
        val channelCause = when {
            httpCode != null && httpCode >= 400 -> SseHttpException(httpCode)
            t != null -> t
            else -> IOException("SSE connection failed (no cause provided)")
        }

        channel.close(channelCause)
    }
}