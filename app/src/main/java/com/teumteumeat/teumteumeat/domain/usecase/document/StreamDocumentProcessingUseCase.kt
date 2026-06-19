package com.teumteumeat.teumteumeat.domain.usecase.document

import com.teumteumeat.teumteumeat.domain.model.sse.DocumentProcessingEvent
import com.teumteumeat.teumteumeat.domain.model.sse.SseHttpException
import com.teumteumeat.teumteumeat.domain.repository.document.DocumentProcessingStreamRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject

/**
 * 문서 처리 상태 SSE 스트리밍을 시작하고,
 * 네트워크/HTTP 오류를 사용자 메시지가 담긴 [DocumentProcessingEvent.StreamError] 로 변환한다.
 *
 * ### HTTP 상태 코드 매핑
 * | 예외                   | 사용자 메시지                 |
 * |-----------------------|-----------------------------|
 * | [SseHttpException] 400 | 잘못된 요청입니다.            |
 * | [SseHttpException] 401 | 인증이 필요합니다.            |
 * | [SseHttpException] 403 | 접근 권한이 없습니다.          |
 * | [SseHttpException] 404 | 문서를 찾을 수 없습니다.       |
 * | [IOException]          | 네트워크 연결을 확인해주세요.  |
 * | 그 외                  | 알 수 없는 오류가 발생했습니다. |
 */
class StreamDocumentProcessingUseCase @Inject constructor(
    private val repository: DocumentProcessingStreamRepository
) {

    operator fun invoke(goalId: Long, documentId: Long): Flow<DocumentProcessingEvent> =
        repository.streamDocumentProcessing(goalId, documentId)
            .map { event ->
                if (event is DocumentProcessingEvent.StreamError) event.toUserFacingError() else event
            }
}

private fun DocumentProcessingEvent.StreamError.toUserFacingError(): DocumentProcessingEvent.StreamError {
    val message = when (val cause = throwable) {
        is SseHttpException -> when (cause.code) {
            400  -> "잘못된 요청입니다."
            401  -> "인증이 필요합니다."
            403  -> "접근 권한이 없습니다."
            404  -> "문서를 찾을 수 없습니다."
            else -> "알 수 없는 오류가 발생했습니다."
        }
        is IOException -> "네트워크 연결을 확인해주세요."
        else           -> "알 수 없는 오류가 발생했습니다."
    }
    return DocumentProcessingEvent.StreamError(Exception(message, throwable))
}