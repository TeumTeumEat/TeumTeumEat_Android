package com.teumteumeat.teumteumeat.data.repository.document

import android.util.Log
import com.teumteumeat.teumteumeat.data.network.retrofit.NetworkConfig
import com.teumteumeat.teumteumeat.data.remote.sse.SseClient
import com.teumteumeat.teumteumeat.data.remote.sse.SseEvent as DataSseEvent
import com.teumteumeat.teumteumeat.domain.model.sse.SseBusinessException
import com.teumteumeat.teumteumeat.domain.model.sse.SseEvent
import com.teumteumeat.teumteumeat.domain.model.sse.SseHttpException
import com.teumteumeat.teumteumeat.domain.repository.document.PdfSummaryStreamRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.transformWhile
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject
import javax.inject.Singleton

/**
 * [PdfSummaryStreamRepository] Data Layer 구현체.
 *
 * `POST api/v1/goals/{goalId}/documents/{documentId}/summary/stream` 엔드포인트에
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
 * | `Failure`                        | [SseEvent.StreamError]   |
 */
@Singleton
class PdfSummaryStreamRepositoryImpl @Inject constructor(
    private val sseClient: SseClient
) : PdfSummaryStreamRepository {

    override fun streamPdfSummary(goalId: Long, documentId: Long): Flow<SseEvent> {
        val request = Request.Builder()
            .url("${NetworkConfig.BASE_URL}api/v1/goals/$goalId/documents/$documentId/summary/stream")
            .post("".toRequestBody())
            .header("Accept", "text/event-stream")
            .header("Cache-Control", "no-cache")
            .build()

        var titleReceived = false

        return sseClient.connect(request)
            .transformWhile { rawEvent ->
                when (rawEvent) {
                    is DataSseEvent.Closed -> {
                        if (!titleReceived) {
                            emit(SseEvent.StreamError(Exception("서버가 PDF 요약글 스트리밍을 완료하지 않고 연결을 종료했습니다.")))
                        }
                        false
                    }
                    is DataSseEvent.Failure -> {
                        emit(SseEvent.StreamError(rawEvent.toStreamThrowable()))
                        false
                    }
                    else -> {
                        val domainEvent = rawEvent.toDomainEvent()
                        if (domainEvent != null) {
                            if (domainEvent is SseEvent.TitleReceived) titleReceived = true
                            emit(domainEvent)
                            domainEvent !is SseEvent.TitleReceived
                        } else {
                            true
                        }
                    }
                }
            }
            .catch { throwable ->
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

    private fun DataSseEvent.Failure.toStreamThrowable(): Throwable {
        val httpCause = cause as? SseHttpException
        val result = when {
            httpCause?.errorCode != null ->
                SseBusinessException(httpCause.errorCode, httpCause.errorMessage ?: httpMessage)
            httpCode != null ->
                SseHttpException(httpCode)
            else ->
                cause ?: Exception("PDF 요약글 스트리밍 연결에 실패했습니다.")
        }
        Log.e("SSE_ERROR", "PdfSummaryStream Failure → 도메인 예외: ${result.javaClass.simpleName}(${result.message}), httpCode=$httpCode")
        return result
    }

    companion object {
        private const val EVENT_TYPE_CONNECT = "CONNECT"
        private const val EVENT_TYPE_MESSAGE = "message"
        private const val EVENT_TYPE_TITLE   = "title"
    }
}
