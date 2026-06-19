package com.teumteumeat.teumteumeat.data.repository.document

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.teumteumeat.teumteumeat.data.network.retrofit.NetworkConfig
import com.teumteumeat.teumteumeat.data.remote.sse.SseClient
import com.teumteumeat.teumteumeat.data.remote.sse.SseEvent as DataSseEvent
import com.teumteumeat.teumteumeat.domain.model.sse.DocumentProcessingEvent
import com.teumteumeat.teumteumeat.domain.model.sse.DocumentProcessingEvent.FailureReason
import com.teumteumeat.teumteumeat.domain.repository.document.DocumentProcessingStreamRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.transformWhile
import okhttp3.Request
import javax.inject.Inject
import javax.inject.Singleton

/**
 * [DocumentProcessingStreamRepository] Data Layer 구현체.
 *
 * `GET api/v1/goals/{goalId}/documents/{documentId}/sse` 엔드포인트에
 * SSE 연결을 열고, Data Layer raw 이벤트([DataSseEvent])를
 * Domain 이벤트([DocumentProcessingEvent])로 변환하여 방출한다.
 *
 * ### 이벤트 매핑 규칙
 * | `event:` 필드                     | `data.status`  | Domain 이벤트                      |
 * |----------------------------------|----------------|-----------------------------------|
 * | `connect`                        | CONNECTED      | [DocumentProcessingEvent.Connected] |
 * | `document_processing_status`     | PENDING        | [DocumentProcessingEvent.Pending]   |
 * | `document_processing_status`     | PROCESSING     | [DocumentProcessingEvent.Processing] |
 * | `document_processing_status`     | COMPLETED      | [DocumentProcessingEvent.Completed] |
 * | `document_processing_status`     | FAILED         | [DocumentProcessingEvent.Failed]    |
 * | `Opened` / `Closed` / `Failure`  | —              | 무시                                |
 *
 * ### 종료 조건
 * [DocumentProcessingEvent.Completed] 또는 [DocumentProcessingEvent.Failed] 방출 후 Flow 완료.
 * 재연결 소진 시 [DocumentProcessingEvent.StreamError] 방출 후 완료.
 */
@Singleton
class DocumentProcessingStreamRepositoryImpl @Inject constructor(
    private val sseClient: SseClient
) : DocumentProcessingStreamRepository {

    private val gson = Gson()

    override fun streamDocumentProcessing(goalId: Long, documentId: Long): Flow<DocumentProcessingEvent> {
        val request = Request.Builder()
            .url("${NetworkConfig.BASE_URL}api/v1/goals/$goalId/documents/$documentId/sse")
            .get()
            .header("Accept", "text/event-stream")
            .header("Cache-Control", "no-cache")
            .build()

        return sseClient.connect(request)
            .mapNotNull { it.toDomainEvent() }
            .transformWhile { event ->
                emit(event)
                event !is DocumentProcessingEvent.Completed &&
                    event !is DocumentProcessingEvent.Failed &&
                    event !is DocumentProcessingEvent.StreamError
            }
            .catch { throwable ->
                // 예상치 못한 예외에 대한 안전망 (HTTP/네트워크 오류는 Failure 이벤트로 전달됨)
                emit(DocumentProcessingEvent.StreamError(throwable))
            }
    }

    private fun DataSseEvent.toDomainEvent(): DocumentProcessingEvent? = when (this) {
        is DataSseEvent.Message -> when (type) {
            EVENT_CONNECT -> DocumentProcessingEvent.Connected
            EVENT_STATUS   -> parseStatusEvent(data)
            else           -> null
        }
        is DataSseEvent.Opened,
        is DataSseEvent.Closed -> null
        // SseClient가 재시도를 소진한 뒤 방출하는 종단 실패 이벤트 → StreamError로 전달.
        is DataSseEvent.Failure -> DocumentProcessingEvent.StreamError(
            cause ?: Exception(httpMessage ?: "문서 처리 스트리밍 연결에 실패했습니다.")
        )
    }

    private fun parseStatusEvent(data: String): DocumentProcessingEvent? =
        runCatching {
            val payload = gson.fromJson(data, StatusPayload::class.java)
            when (payload.status) {
                STATUS_PENDING    -> DocumentProcessingEvent.Pending
                STATUS_PROCESSING -> DocumentProcessingEvent.Processing(payload.remainMs ?: 0L)
                STATUS_COMPLETED  -> DocumentProcessingEvent.Completed
                STATUS_FAILED     -> DocumentProcessingEvent.Failed(payload.reason.toFailureReason())
                else              -> null
            }
        }.getOrNull()

    private fun String?.toFailureReason(): FailureReason = when (this) {
        REASON_TIMEOUT        -> FailureReason.TIMEOUT
        REASON_SERVER_ERROR   -> FailureReason.SERVER_ERROR
        REASON_ENCRYPTED_FILE -> FailureReason.ENCRYPTED_FILE
        else                  -> FailureReason.UNKNOWN
    }

    private data class StatusPayload(
        val status: String,
        @SerializedName("remain_ms") val remainMs: Long? = null,
        val reason: String? = null
    )

    companion object {
        private const val EVENT_CONNECT = "connect"
        private const val EVENT_STATUS  = "document_processing_status"

        private const val STATUS_PENDING    = "PENDING"
        private const val STATUS_PROCESSING = "PROCESSING"
        private const val STATUS_COMPLETED  = "COMPLETED"
        private const val STATUS_FAILED     = "FAILED"

        private const val REASON_TIMEOUT        = "TIMEOUT"
        private const val REASON_SERVER_ERROR   = "SERVER_ERROR"
        private const val REASON_ENCRYPTED_FILE = "ENCRYPTED_FILE"
    }
}