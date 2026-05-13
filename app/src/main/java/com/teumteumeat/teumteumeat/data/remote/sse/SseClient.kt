package com.teumteumeat.teumteumeat.data.remote.sse

import com.teumteumeat.teumteumeat.domain.model.sse.SseHttpException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.retryWhen
import kotlinx.coroutines.flow.shareIn
import okhttp3.Request
import okhttp3.sse.EventSource
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

private const val MAX_RETRY_COUNT = 3L

/** 지수 백오프 기저 딜레이(ms). 시도 n 회 차: base * 2^n → 1 s, 2 s, 4 s */
private const val BASE_DELAY_MS = 1_000L

/**
 * OkHttp [EventSource]를 Kotlin [Flow]로 래핑하는 SSE 클라이언트.
 *
 * ### 단일 활성 연결 보장
 * 동일 URL에 대해 [connect]가 여러 번 호출되어도 실제 HTTP 연결(EventSource)은
 * 하나만 유지된다. 내부적으로 [ConcurrentHashMap]에 URL → [SharedFlow] 를 캐싱하고
 * [SharingStarted.WhileSubscribed]로 수집자가 없으면 EventSource를 자동 해제한다.
 *
 * ```
 * collector A ─┐
 *              ├─→ SharedFlow ─→ EventSource (1개)
 * collector B ─┘
 *
 * A, B 모두 취소 → EventSource.cancel() 자동 호출
 * 새 collector 진입 → EventSource 재연결
 * ```
 *
 * ### 재연결 정책
 * [onFailure] 발생 시 지수 백오프로 최대 [MAX_RETRY_COUNT]회 재연결을 시도한다.
 * ```
 * 1회차 실패 → 1 s 대기 → 재연결
 * 2회차 실패 → 2 s 대기 → 재연결
 * 3회차 실패 → 4 s 대기 → 재연결
 * 4회차 실패 → Flow 종료 (예외 전파)
 * ```
 * 서버가 정상 종료([SseEvent.Closed])하면 재연결 없이 Flow가 완료된다.
 */
@Singleton
class SseClient @Inject constructor(
    private val eventSourceFactory: EventSource.Factory
) {
    // Singleton 생명주기와 동일. shareIn의 업스트림 수집 컨텍스트.
    private val clientScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // URL → SharedFlow 캐시. computeIfAbsent는 원자적으로 단일 SharedFlow 생성을 보장한다.
    private val connections = ConcurrentHashMap<String, SharedFlow<SseEvent>>()

    /**
     * 주어진 [request]로 SSE 스트림을 열고 이벤트를 [SseEvent]로 방출하는 Flow를 반환한다.
     *
     * 동일 URL에 대한 중복 호출은 **하나의 EventSource 연결을 공유**한다.
     * 모든 수집자(collector)가 취소되면 EventSource가 자동 종료된다.
     *
     * @param request OkHttp [Request]. [EventSources.createFactory]가 내부적으로
     *                `Accept: text/event-stream` 헤더를 자동 추가하므로 별도 설정 불필요.
     */
    fun connect(request: Request): Flow<SseEvent> =
        connections.computeIfAbsent(request.url.toString()) {
            rawFlow(request).shareIn(
                scope = clientScope,
                started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 0L),
                replay = 0
            )
        }

    private fun rawFlow(request: Request): Flow<SseEvent> =
        callbackFlow {
            val listener = SseEventSourceListener(channel)
            val source = eventSourceFactory.newEventSource(request, listener)

            // Flow 취소 시 EventSource 정리
            awaitClose { source.cancel() }
        }.retryWhen { cause, attempt ->
            // SseHttpException(4xx/5xx)은 재시도해도 동일한 오류가 반환되므로 즉시 전파.
            val shouldRetry = attempt < MAX_RETRY_COUNT && cause !is SseHttpException
            if (shouldRetry) {
                // 지수 백오프: 1000 * 2^attempt ms
                delay(BASE_DELAY_MS * (1L shl attempt.toInt()))
            }
            shouldRetry
        }
}