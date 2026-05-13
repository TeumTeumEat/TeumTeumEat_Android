package com.teumteumeat.teumteumeat.data.repository.summary

import com.teumteumeat.teumteumeat.data.network.retrofit.NetworkConfig
import com.teumteumeat.teumteumeat.data.remote.sse.SseClient
import com.teumteumeat.teumteumeat.data.remote.sse.SseEvent as DataSseEvent
import com.teumteumeat.teumteumeat.domain.model.sse.SseEvent
import com.teumteumeat.teumteumeat.domain.repository.summary.SummaryStreamRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.mapNotNull
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
 * | `Opened` / `Closed`              | 무시 (수명 주기 이벤트)     |
 * | `Failure` (재시도 진행 중)         | 무시 ([SseClient] 재연결)  |
 * | `Failure` (재시도 3회 소진)        | [SseEvent.StreamError]   |
 *
 * ### 종료 조건
 * [SseEvent.TitleReceived] 방출 후 Flow가 정상 완료된다.
 * 재연결 소진 시 [SseEvent.StreamError]를 방출 후 완료된다.
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

        return sseClient.connect(request)
            .mapNotNull { it.toDomainEvent() }
            .transformWhile { event ->
                emit(event)
                // TitleReceived 방출 후 upstream 수집 중단 → Flow 정상 완료
                event !is SseEvent.TitleReceived
            }
            .catch { throwable ->
                // SseClient 재연결 소진 후 전파된 예외를 StreamError로 래핑
                emit(SseEvent.StreamError(throwable))
            }
    }

    /**
     * [DataSseEvent]를 [SseEvent]로 변환한다.
     *
     * [DataSseEvent.Opened], [DataSseEvent.Closed], [DataSseEvent.Failure]는
     * null을 반환하여 [mapNotNull]에서 필터링된다.
     * - `Opened`·`Closed`는 OkHttp 수명 주기 이벤트이며 도메인 관심사가 아니다.
     * - `Failure`는 재시도 중 방출되며, 최종 실패는 예외로 전파되어 [catch]에서 처리된다.
     */
    private fun DataSseEvent.toDomainEvent(): SseEvent? = when (this) {
        is DataSseEvent.Message -> when (type) {
            EVENT_TYPE_CONNECT -> SseEvent.Connected
            EVENT_TYPE_MESSAGE -> SseEvent.Chunk(data)
            EVENT_TYPE_TITLE   -> SseEvent.TitleReceived(data)
            else               -> null
        }
        is DataSseEvent.Opened,
        is DataSseEvent.Closed,
        is DataSseEvent.Failure -> null
    }

    companion object {
        private const val EVENT_TYPE_CONNECT = "CONNECT"
        private const val EVENT_TYPE_MESSAGE = "message"
        private const val EVENT_TYPE_TITLE   = "title"
    }
}