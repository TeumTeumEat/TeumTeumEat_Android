package com.teumteumeat.teumteumeat.domain.repository.document

import com.teumteumeat.teumteumeat.domain.model.sse.SseEvent
import kotlinx.coroutines.flow.Flow

/**
 * PDF 문서 요약글 SSE 스트리밍을 담당하는 Repository 인터페이스.
 *
 * ### 이벤트 순서 계약
 * ```
 * SseEvent.Connected
 *     → SseEvent.Chunk (0~N회)
 *         → SseEvent.TitleReceived  ← Flow 정상 완료
 *
 * 오류 발생 시 (재연결 소진 후):
 *     → SseEvent.StreamError        ← Flow 정상 완료
 * ```
 * `SseEvent.TitleReceived` 또는 `SseEvent.StreamError` 수신 후
 * Flow 는 추가 이벤트 없이 완료(complete)된다.
 */
interface PdfSummaryStreamRepository {

    /**
     * 지정된 목표/문서의 PDF 요약글 SSE 스트리밍을 시작한다.
     *
     * Cold Flow 로 반환되므로, 수집(collect)을 시작해야 실제 연결이 열린다.
     * Flow 수집 취소 시 서버 연결이 즉시 해제된다.
     *
     * @param goalId     목표 ID.
     * @param documentId 문서 ID.
     * @return [SseEvent]를 방출하는 Cold [Flow].
     */
    fun streamPdfSummary(goalId: Long, documentId: Long): Flow<SseEvent>
}
