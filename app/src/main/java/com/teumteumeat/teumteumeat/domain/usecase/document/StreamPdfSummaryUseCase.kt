package com.teumteumeat.teumteumeat.domain.usecase.document

import com.teumteumeat.teumteumeat.domain.model.sse.SseBusinessException
import com.teumteumeat.teumteumeat.domain.model.sse.SseEvent
import com.teumteumeat.teumteumeat.domain.model.sse.SseHttpException
import com.teumteumeat.teumteumeat.domain.repository.document.PdfSummaryStreamRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject

/**
 * PDF 문서 요약글 SSE 스트리밍을 시작하고,
 * 네트워크/HTTP 오류를 사용자 메시지가 담긴 [SseEvent.StreamError]로 변환한다.
 *
 * [SseBusinessException]은 ViewModel 이 에러 코드 기준으로 분기하므로 변환 없이 보존한다.
 *
 * ### HTTP 상태 코드 매핑
 * | 예외 유형                    | 사용자 메시지                    |
 * |-----------------------------|-------------------------------|
 * | [SseHttpException] 401      | 인증이 필요합니다.               |
 * | [SseHttpException] 403      | 접근 권한이 없습니다.             |
 * | [SseHttpException] 404      | 문서를 찾을 수 없습니다.           |
 * | [IOException]               | 네트워크 연결을 확인해주세요.      |
 * | 그 외                        | 알 수 없는 오류가 발생했습니다.    |
 */
class StreamPdfSummaryUseCase @Inject constructor(
    private val repository: PdfSummaryStreamRepository
) {

    operator fun invoke(goalId: Long, documentId: Long): Flow<SseEvent> =
        repository.streamPdfSummary(goalId, documentId)
            .map { event ->
                if (event is SseEvent.StreamError) event.toUserFacingError() else event
            }
}

private fun SseEvent.StreamError.toUserFacingError(): SseEvent.StreamError {
    if (throwable is SseBusinessException) return this

    val message = when (val cause = throwable) {
        is SseHttpException -> when (cause.code) {
            401  -> "인증이 필요합니다."
            403  -> "접근 권한이 없습니다."
            404  -> "문서를 찾을 수 없습니다."
            else -> "알 수 없는 오류가 발생했습니다."
        }
        is IOException -> "네트워크 연결을 확인해주세요."
        else           -> "알 수 없는 오류가 발생했습니다."
    }
    return SseEvent.StreamError(Exception(message, throwable))
}
