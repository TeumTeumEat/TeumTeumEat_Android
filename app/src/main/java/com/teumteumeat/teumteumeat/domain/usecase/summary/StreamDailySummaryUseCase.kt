package com.teumteumeat.teumteumeat.domain.usecase.summary

import com.teumteumeat.teumteumeat.domain.model.sse.SseEvent
import com.teumteumeat.teumteumeat.domain.model.sse.SseHttpException
import com.teumteumeat.teumteumeat.domain.repository.summary.SummaryStreamRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject

/**
 * 지정된 카테고리의 일일 요약 SSE 스트리밍을 시작하고,
 * 네트워크/HTTP 오류를 사용자 메시지가 담긴 [SseEvent.StreamError]로 변환한다.
 *
 * ### HTTP 상태 코드 매핑
 * | 예외 유형                    | 사용자 메시지                    |
 * |-----------------------------|-------------------------------|
 * | [SseHttpException] 400      | 잘못된 요청입니다.               |
 * | [SseHttpException] 401      | 인증이 필요합니다.               |
 * | [SseHttpException] 403      | 접근 권한이 없습니다.             |
 * | [SseHttpException] 404      | 카테고리를 찾을 수 없습니다.       |
 * | [IOException]               | 네트워크 연결을 확인해주세요.      |
 * | 그 외                        | 알 수 없는 오류가 발생했습니다.    |
 */
class StreamDailySummaryUseCase @Inject constructor(
    private val summaryStreamRepository: SummaryStreamRepository
) {

    operator fun invoke(categoryId: Long): Flow<SseEvent> =
        summaryStreamRepository.streamDailySummary(categoryId)
            .map { event ->
                if (event is SseEvent.StreamError) event.toUserFacingError() else event
            }
}

private fun SseEvent.StreamError.toUserFacingError(): SseEvent.StreamError {
    val message = when (val cause = throwable) {
        is SseHttpException -> when (cause.code) {
            400 -> "잘못된 요청입니다."
            401 -> "인증이 필요합니다."
            403 -> "접근 권한이 없습니다."
            404 -> "카테고리를 찾을 수 없습니다."
            else -> "알 수 없는 오류가 발생했습니다."
        }
        is IOException -> "네트워크 연결을 확인해주세요."
        else -> "알 수 없는 오류가 발생했습니다."
    }
    return SseEvent.StreamError(Exception(message, throwable))
}