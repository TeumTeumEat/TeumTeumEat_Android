package com.teumteumeat.teumteumeat.data.remote.sse

import android.util.Log
import com.google.gson.Gson
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
 * - [onFailure] 발생 시 채널을 **예외와 함께** 닫는다. 이후 [SseClient]가
 *   해당 예외를 종단 [SseEvent.Failure] 이벤트로 변환하여 수집자에게 전달한다.
 *   (SharedFlow는 예외를 수집자에게 전달하지 못하므로 예외를 직접 흘려보내지 않는다.)
 * - [onClosed] 직후 [onFailure]가 연속 호출되는 OkHttp 엣지 케이스에 대비해
 *   `trySend`/`close`는 이미 닫힌 채널에서 no-op으로 안전하게 동작한다.
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
        val httpCode = response?.code

        // HTTP 오류(4xx/5xx)는 응답 바디의 비즈니스 코드까지 파싱하여 SseHttpException으로 래핑.
        //  → retryWhen에서 재시도 제외 + Presentation 레이어 코드별 분기에 사용.
        // 네트워크 오류(t != null, response == null)는 t를 그대로 전파 → retryWhen 재시도.
        val channelCause = when {
            httpCode != null && httpCode >= 400 -> {
                val errorBody = response.parseErrorBody()
                Log.e("SSE_ERROR", "HTTP 오류: code=$httpCode, errorCode=${errorBody?.code}, message=${errorBody?.message}")
                SseHttpException(
                    code = httpCode,
                    errorCode = errorBody?.code,
                    errorMessage = errorBody?.message
                )
            }
            t != null -> {
                Log.e("SSE_ERROR", "네트워크 오류: ${t.javaClass.simpleName}(${t.message})")
                t
            }
            else -> {
                Log.e("SSE_ERROR", "SSE 연결 실패: 원인 불명 (httpCode=$httpCode)")
                IOException("SSE connection failed (no cause provided)")
            }
        }

        channel.close(channelCause)
    }

    /** 4xx/5xx 응답 바디 `{ "code", "message" }`를 파싱한다. 실패 시 null. */
    private fun Response.parseErrorBody(): SseErrorBody? =
        runCatching {
            val raw = body?.string().orEmpty()
            if (raw.isBlank()) null
            else gson.fromJson(raw, SseErrorBody::class.java)
        }.getOrNull()

    private data class SseErrorBody(
        val code: String?,
        val message: String?
    )

    private companion object {
        private val gson = Gson()
    }
}
