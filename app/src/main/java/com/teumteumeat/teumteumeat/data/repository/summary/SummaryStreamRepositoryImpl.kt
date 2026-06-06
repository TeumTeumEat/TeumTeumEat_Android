package com.teumteumeat.teumteumeat.data.repository.summary

import com.teumteumeat.teumteumeat.data.network.retrofit.NetworkConfig
import com.teumteumeat.teumteumeat.data.remote.sse.SseClient
import com.teumteumeat.teumteumeat.data.remote.sse.SseEvent as DataSseEvent
import com.teumteumeat.teumteumeat.domain.model.sse.SseEvent
import com.teumteumeat.teumteumeat.domain.model.sse.SseHttpException
import com.teumteumeat.teumteumeat.domain.repository.summary.SummaryStreamRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.transformWhile
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject
import javax.inject.Singleton

/**
 * [SummaryStreamRepository] Data Layer 구현체.
 *
 * `POST api/v1/categories/{categoryId}/documents/daily/stream` 엔드포인트에
 * SSE 연결을 열고, Data Layer raw 이벤트([DataSseEvent])를
 * Domain 이벤트([SseEvent])로 변환하여 방출한다.
 *
 * ### 이벤트 매핑 규칙
 * | Data 이벤트 (`event:` 필드)       | Domain 이벤트              |
 * |----------------------------------|--------------------------|
 * | `Message(type = "CONNECT")`      | [SseEvent.Connected]     |
 * | `Message(type = "message")`      | [SseEvent.Chunk]         |
 * | `Message(type = "title")`        | [SseEvent.TitleReceived] |
 * | `Opened`                         | 무시 (수명 주기 이벤트)     |
 * | `Closed` (title 미수신)           | [SseEvent.StreamError]   |
 * | `Closed` (title 수신 후)          | Flow 정상 완료             |
 * | `Failure` (HTTP 4xx/5xx)         | [SseEvent.StreamError]   |
 * | `Failure` (재시도 진행 중)         | 무시 ([SseClient] 재연결)  |
 *
 * ### 종료 조건
 * [SseEvent.TitleReceived] 방출 후 또는 [DataSseEvent.Closed] 수신 후 Flow가 완료된다.
 * HTTP 오류/재연결 소진 시 [SseEvent.StreamError]를 방출 후 완료된다.
 */
@Singleton
class SummaryStreamRepositoryImpl @Inject constructor(
    private val sseClient: SseClient
) : SummaryStreamRepository {

    override fun streamDailySummary(categoryId: Long): Flow<SseEvent> {
        val request = Request.Builder()
            .url("${NetworkConfig.BASE_URL}api/v1/categories/$categoryId/documents/daily/stream")
            .post("".toRequestBody())
            .header("Accept", "text/event-stream")
            .header("Cache-Control", "no-cache")
            .build()

        var titleReceived = false

        return sseClient.connect(request)
            .transformWhile { rawEvent ->
                when (rawEvent) {
                    is DataSseEvent.Closed -> {
                        // 서버가 title 이벤트 없이 연결을 종료한 경우 — 비정상 종료
                        if (!titleReceived) {
                            emit(SseEvent.StreamError(Exception("서버가 요약 스트리밍을 완료하지 않고 연결을 종료했습니다.")))
                        }
                        false
                    }
                    is DataSseEvent.Failure -> {
                        if (rawEvent.httpCode != null && rawEvent.httpCode >= 400) {
                            emit(SseEvent.StreamError(SseHttpException(rawEvent.httpCode)))
                            false
                        } else {
                            // 재연결 진행 중인 일시적 실패 — SseClient가 retry 처리
                            true
                        }
                    }
                    else -> {
                        val domainEvent = rawEvent.toDomainEvent()
                        if (domainEvent != null) {
                            if (domainEvent is SseEvent.TitleReceived) titleReceived = true
                            emit(domainEvent)
                            // TitleReceived 방출 후 upstream 수집 중단 → Flow 정상 완료
                            domainEvent !is SseEvent.TitleReceived
                        } else {
                            true
                        }
                    }
                }
            }
            .catch { throwable ->
                // SseClient 재연결 소진 후 전파된 예외를 StreamError로 래핑
                emit(SseEvent.StreamError(throwable))
            }
    }

    private fun DataSseEvent.toDomainEvent(): SseEvent? = when (this) {
        is DataSseEvent.Message -> when (type) {
            EVENT_TYPE_CONNECT -> SseEvent.Connected
            EVENT_TYPE_MESSAGE -> SseEvent.Chunk(data)
            EVENT_TYPE_TITLE   -> SseEvent.TitleReceived(data)
            else               -> null
        }
        else -> null
    }

    companion object {
        private const val EVENT_TYPE_CONNECT = "CONNECT"
        private const val EVENT_TYPE_MESSAGE = "message"
        private const val EVENT_TYPE_TITLE   = "title"
    }
}